package com.pinyi.mian;

import org.springframework.context.ConfigurableApplicationContext;

public interface PluginBootStrap {
    void boot(ConfigurableApplicationContext context);
}