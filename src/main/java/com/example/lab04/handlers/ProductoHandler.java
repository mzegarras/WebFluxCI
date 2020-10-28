package com.example.lab04.handlers;

import com.example.lab04.config.MicroserviceProperties;
import com.example.lab04.exceptions.UnauthorizedException;
import com.example.lab04.models.documents.Categoria;
import com.example.lab04.models.documents.CustomFieldError;
import com.example.lab04.models.documents.Producto;
import com.example.lab04.models.services.ProductoService;

import com.nimbusds.jwt.JWTParser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;

import static org.springframework.web.reactive.function.BodyInserters.*;

import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;

@Component
public class ProductoHandler {

    private final ProductoService productoService;
    private final MicroserviceProperties microserviceProperties;
    private final Validator validator;

    private static final Logger log = LoggerFactory.getLogger(ProductoHandler.class);

    public ProductoHandler(ProductoService productoService, MicroserviceProperties microserviceProperties, Validator validator) {
        this.productoService = productoService;
        this.microserviceProperties = microserviceProperties;
        this.validator = validator;
    }

    public Mono<ServerResponse> list(ServerRequest rq) {

        log.debug("list");
        // TODO: PMD
        //int valor = 0;


        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> getById(ServerRequest rq) {

        String id = rq.pathVariable("id");

        return productoService.findById(id).flatMap(p -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(p))
        ).switchIfEmpty(ServerResponse.notFound().build());


    }

    public Mono<ServerResponse> create(ServerRequest rq) {

        Mono<Producto> producto = rq.bodyToMono(Producto.class);

        return producto.flatMap(p -> {

            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            this.validator.validate(p, errors);

            if (errors.hasErrors()) {

                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> new CustomFieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                        .collectList()
                        .flatMap(lista -> ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(lista)));

            } else {
                String token = rq.headers().firstHeader("Authorization");

                if (!StringUtils.isEmpty(token)) {
                    token = token.replace("Bearer ", "");

                    try {
                        p.setAppId(JWTParser.parse(token).getJWTClaimsSet().getClaim("client_id").toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }


                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }


                // TODO: YP - BEHAVIOR
               //productoService.save(p);

                return productoService.save(p)
                        // TODO: YP OUTPUT DATA
                        /*.map(p1->{
                            p1.setAppId("001");
                            return p1;
                        })*/
                        .flatMap(pdb -> ServerResponse
                                .created(URI.create("/productos/" + pdb.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(pdb)));

            }
        });
    }

    public Mono<ServerResponse> edit(ServerRequest rq) {
        String id = rq.pathVariable("id");
        Mono<Producto> producto = rq.bodyToMono(Producto.class);
        Mono<Producto> productodb = productoService.findById(id);


        return productodb.zipWith(producto, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setCategoria(req.getCategoria());
            db.setPrecio(req.getPrecio());
            return db;
        }).flatMap(p -> ServerResponse
                .created(URI.create("/productos/" + p.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest rq) {
        String id = rq.pathVariable("id");

        //return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();

        if (microserviceProperties.isInPanic()) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }


        Mono<Producto> productodb = productoService.findById(id);

        return productodb.flatMap(p -> productoService.delete(p)
                .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error -> error instanceof UnauthorizedException ?
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).build() :
                        ServerResponse.status(500).build());

    }


    private File getFileTemporary(Producto producto){

        File fileTemporary = new File( this.microserviceProperties.getPhotos().getPath(),FilenameUtils.normalize(FilenameUtils.getName(producto.getFoto())));

        return fileTemporary;

    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");


        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoService.findById(id)
                        .flatMap(producto -> {
                            producto.setFoto(file.filename());
                            return file.transferTo(getFileTemporary(producto)).then(productoService.save(producto)).onErrorResume(e->{
                                e.printStackTrace();
                                return Mono.error(e);
                            });

                        })).flatMap(producto -> ServerResponse.created(UriComponentsBuilder.newInstance().pathSegment("productos", producto.getId(), "images").build().toUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createv2(ServerRequest request) {


        Mono<Producto> productoMono = request.multipartData().map(multipart -> {
            FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });


        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoMono
                        .flatMap(producto -> {
                            producto.setFoto(file.filename());
                            producto.setCreateAt(new Date());

                            return file. transferTo(getFileTemporary(producto)).then(productoService.save(producto)).onErrorResume(e->{
                                e.printStackTrace();
                                return Mono.error(e);
                            });



                        })).flatMap(productoDB -> ServerResponse.created(UriComponentsBuilder.newInstance().pathSegment("productos", productoDB.getId()).build().toUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(productoDB)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}

