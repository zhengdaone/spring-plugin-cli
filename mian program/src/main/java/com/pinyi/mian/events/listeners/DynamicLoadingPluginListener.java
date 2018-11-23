package com.pinyi.mian.events.listeners;

import com.pinyi.mian.JDBCManager;
import com.pinyi.mian.PluginManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.URLClassLoader;


public class DynamicLoadingPluginListener implements SpringApplicationRunListener {

    private PluginManager pluginManager;

    public DynamicLoadingPluginListener(SpringApplication application, String[] args) {
        pluginManager = new PluginManager();
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment configurableEnvironment) {
        JDBCManager.url = configurableEnvironment.getProperty("spring.datasource.url");
        JDBCManager.username = configurableEnvironment.getProperty("spring.datasource.username");
        JDBCManager.password = configurableEnvironment.getProperty("spring.datasource.password");

        pluginManager.setClassLoader((URLClassLoader) configurableEnvironment.getClass().getClassLoader());
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext configurableApplicationContext) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext configurableApplicationContext) {
        pluginManager.register(configurableApplicationContext);
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        pluginManager.boot(context);
    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }
}
