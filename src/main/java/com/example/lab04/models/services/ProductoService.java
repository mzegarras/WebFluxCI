package com.example.lab04.models.services;


import com.example.lab04.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

     Flux<Producto> findAll();
     Flux<Producto> findAllWithNameUpperCase();
     Mono<Producto> findById(String id);
     Mono<Producto> save(Producto producto);
     Mono<Void> delete(Producto producto);


     Mono<Producto> findByNombre(String nombre);

}
