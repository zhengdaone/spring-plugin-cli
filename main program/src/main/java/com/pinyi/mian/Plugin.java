package com.pinyi.mian;

import java.util.Arrays;
import java.util.List;

public class Plugin {

    private Integer id;

    private String name;

    private String url;

    private String bootClass;

    private String scanPath;

    private String config;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBootClass() {
        return bootClass;
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public List<String> getScanPath() {
        return Arrays.asList(scanPath.split(","));
    }

    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}