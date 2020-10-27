package com.example.lab04.models.services;



import com.example.lab04.config.FilesProperties;
import com.example.lab04.models.dao.ProductoDao;
import com.example.lab04.models.documents.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDao productoDao;
    private final FilesProperties filesProperties;


    public ProductoServiceImpl(ProductoDao productoDao,FilesProperties filesProperties) {
        this.productoDao = productoDao;
        this.filesProperties=filesProperties;
    }

    @Override
    public Flux<Producto> findAll() {
        log.debug("filesProperties.isInPanic{}",filesProperties.isInPanic());

        return productoDao.findAll();
    }

    @Override
    public Flux<Producto> findAllWithNameUpperCase() {
        log.debug("filesProperties.isInPanic{}",filesProperties.isInPanic());

       return productoDao.findAll()
                .map(p->{
                    p.setNombre(p.getNombre().toUpperCase());
                    return p;
                });
    }

    @Override
    public Mono<Producto> findById(String id) {
        return productoDao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return productoDao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return productoDao.delete(producto);
    }

    @Override
    public Mono<Producto> findByNombre(String nombre) {
        return productoDao.findByNombre(nombre);
    }


}
