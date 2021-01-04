package com.example.lab04.config;

import com.example.lab04.handlers.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class RouteFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){
        //TODO: Cambiar URL
        return route(GET("/productos").or(GET("/apis/productos")),handler::list)
                //.andRoute(GET("/productos/{id}").and(contentType(MediaType.APPLICATION_JSON)),handler::getById)
                .andRoute(GET("/productos/{id}"),handler::getById)
                .andRoute(POST("/productos"),handler::create)
                .andRoute(PUT("/productos/{id}"),handler::edit)
                .andRoute(DELETE("/productos/{id}"),handler::delete)
                .andRoute(POST("/productos/{id}/images"),handler::upload)
                .andRoute(POST("/productosv2"),handler::createv2)
                .andRoute(POST("/productosv3"),handler::create);

    }















}


