package com.pinyi.mian;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginManager {

    private URLClassLoader classLoader;

    private static List<Class> springIOCAnnotations = new ArrayList<>();

    private static List<PluginBootStrap> bootClasses = new ArrayList<>();

    //可以放进spring容器的类的注解，待优化
    static {
        springIOCAnnotations.add(Controller.class);
        springIOCAnnotations.add(Service.class);
        springIOCAnnotations.add(Repository.class);
        springIOCAnnotations.add(Component.class);
    }

    public void setClassLoader(URLClassLoader urlClassLoader) {
        this.classLoader = urlClassLoader;
    }

    public void register(ConfigurableApplicationContext context) {
        getPlugins().forEach(plugin -> {
            addJarToClasspath(plugin);

            JarFile jarFile = readJarFile(plugin);
            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();

            fetchBootClass(plugin);

            if (plugin.getScanPath() != null) {
                //遍历jar包，将指定路径下的类添加到spring容器
                traverseJar(jarEntryEnumeration, context, plugin);
            }
        });
    }

    public void boot(ConfigurableApplicationContext context) {
        bootClasses.forEach(bootClass -> bootClass.boot(context));
    }

    private List<Plugin> getPlugins() {
        Connection connection = JDBCManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Plugin> pluginList = new ArrayList<>();
        try {
            String sql = "select * from plugin";
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Plugin plugin = new Plugin();
                plugin.setId(resultSet.getInt("id"));
                plugin.setBootClass(resultSet.getString("boot_class"));
                plugin.setName(resultSet.getString("name"));
                plugin.setUrl(resultSet.getString("url"));
                plugin.setScanPath(resultSet.getString("scan_path"));
                pluginList.add(plugin);
            }
        } catch (SQLException e) {
            throw new RuntimeException("从数据库获取插件配置失败");
        } finally {
            JDBCManager.closeConnection(resultSet, preparedStatement, connection);
        }

        return pluginList;
    }

    private void fetchBootClass(Plugin plugin) {
        if (plugin.getBootClass() == null || plugin.getBootClass().trim().isEmpty()) {
            return;
        }

        try {
            PluginBootStrap pluginBootStrap = (PluginBootStrap) classLoader.loadClass(plugin.getBootClass()).newInstance();
            bootClasses.add(pluginBootStrap);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("获取插件boot函数失败，插件名称：" + plugin.getName());
        }
    }

    private void traverseJar(Enumeration<JarEntry> jarEntryEnumeration, ConfigurableApplicationContext context,
                             Plugin plugin) {
        while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry jarEntry = jarEntryEnumeration.nextElement();
            if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")
                    || plugin.getScanPath().stream().noneMatch(scanPath -> jarEntry.getName().startsWith(scanPath))) {
                continue;
            }

            String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
            className = className.replace('/', '.');

            try {
                Class c = classLoader.loadClass(className);
                BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) context.getBeanFactory();
                for (Class springIOCAnnotationClass : springIOCAnnotations) {
                    if (c.getAnnotation(springIOCAnnotationClass) != null) {
                        definitionRegistry.registerBeanDefinition(className,
                                BeanDefinitionBuilder.genericBeanDefinition(c).getBeanDefinition());
                        break;
                    }
                }
            } catch (NoClassDefFoundError | ClassNotFoundException e1) {
                traverseJar(jarEntryEnumeration, context, plugin);
            }
        }
    }

    private void addJarToClasspath(Plugin plugin) {
        try {
            File file = new File(plugin.getUrl());
            URL url = file.toURI().toURL();

            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (IllegalAccessException | MalformedURLException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("添加插件jar包到classpath失败，插件名称：" + plugin.getName());
        }
    }

    private JarFile readJarFile(Plugin plugin) {
        try {
            return new JarFile(plugin.getUrl());
        } catch (IOException e) {
            throw new RuntimeException("获取插件中的文件信息失败，插件名称：" + plugin.getName());
        }
    }
}
