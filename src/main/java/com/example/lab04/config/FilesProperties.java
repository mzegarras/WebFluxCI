package com.example.lab04.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "files")
public class FilesProperties {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private boolean inPanic;


    public boolean isInPanic() {
        return inPanic;
    }

    public void setInPanic(boolean inPanic) {
        this.inPanic = inPanic;
    }
}
