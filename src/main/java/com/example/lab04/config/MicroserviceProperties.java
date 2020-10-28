package com.example.lab04.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "microservice")
@Getter
@Setter
public class MicroserviceProperties {

    private ConfigDirectory photos;
    private JWTConfig jwt;

    private boolean inPanic;


    @Getter
    @Setter
    public static class ConfigDirectory{
        private String path;
    }

    @Getter
    @Setter
    public static class JWTConfig{
        private String key;
    }
}
