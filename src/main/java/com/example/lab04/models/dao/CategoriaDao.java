package com.example.lab04.models.dao;


import com.example.lab04.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria,String> {

    Mono<Categoria> findByNombre(String nombre);
}
