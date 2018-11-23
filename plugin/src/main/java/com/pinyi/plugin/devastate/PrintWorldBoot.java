package com.pinyi.plugin.devastate;

import com.pinyi.mian.PluginBootStrap;
import org.springframework.context.ConfigurableApplicationContext;

public class PrintWorldBoot implements PluginBootStrap {

    @Override
    public void boot(ConfigurableApplicationContext context) {
        System.out.println("world");
    }
}
