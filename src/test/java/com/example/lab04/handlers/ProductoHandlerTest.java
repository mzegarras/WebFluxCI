package com.example.lab04.handlers;


import com.example.lab04.config.MicroserviceProperties;
import com.example.lab04.config.RouteFunctionConfig;
import com.example.lab04.exceptions.UnauthorizedException;
import com.example.lab04.models.documents.Categoria;
import com.example.lab04.models.documents.CustomFieldError;
import com.example.lab04.models.documents.Producto;
import com.example.lab04.models.services.ProductoService;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebFluxTest()
@Import({RouteFunctionConfig.class, ProductoHandler.class})
@ContextConfiguration(classes = {ProductoHandlerTest.TestConfiguration.class})
public class ProductoHandlerTest {

    public static class TestConfiguration{
        @Bean
        public MicroserviceProperties filesProperties(){
            MicroserviceProperties microserviceProperties = new MicroserviceProperties();
            String pathFiles = System.getenv("GITHUB_WORKSPACE");
            microserviceProperties.setPhotos(new MicroserviceProperties.ConfigDirectory());

           if(StringUtils.isNotBlank(pathFiles))
               microserviceProperties.getPhotos().setPath(pathFiles);
           else
               microserviceProperties.getPhotos().setPath("./target");


            microserviceProperties.setJwt(new MicroserviceProperties.JWTConfig());
            microserviceProperties.getJwt().setKey("12345678901234567890123456789012");

            return microserviceProperties;
        }
    }

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private MicroserviceProperties microserviceProperties;

    @BeforeEach
    void resetMocksAndStubs() {
        reset(productoService);
        microserviceProperties.setInPanic(false);
    }

    @Test
    public void sanity() {
        assertThat(client).isNotNull();
        assertThat(microserviceProperties).isNotNull();
    }

    @Test
    public void delete_byId_notFound(){
        // Preparing data
        Producto productoToDelete =  new Producto();
        productoToDelete.setId("abc123");

        // Mocks & Stubs configuration

        when(productoService.findById("abc123")).thenReturn(Mono.empty());

        // Business logic execution
        client.delete().uri("/productos/abc123")
                .exchange()
                .expectStatus().isNotFound();

        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");

        verifyNoMoreInteractions(productoService);

        // Validating results
    }

    @Test
    public void delete_byId_ok(){
        // Preparing data
        Producto productoToDelete =  new Producto();
        productoToDelete.setId("abc123");

        // Mocks & Stubs configuration

        when(productoService.findById("abc123")).thenReturn(Mono.just(productoToDelete));
        when(productoService.delete(productoToDelete)).thenReturn(Mono.empty());

        // Business logic execution
        client.delete().uri("/productos/abc123")
                .exchange()
                .expectStatus().isNoContent();

        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        verify(productoService,times(1)).delete(productoToDelete);
        verifyNoMoreInteractions(productoService);

        // Validating results
    }


    @Test
    public void delete_byId_InPanic_exception(){
        // Preparing data
        microserviceProperties.setInPanic(true);

        // Mocks & Stubs configuration

        // Business logic execution
        client.delete().uri("/productos/abc123")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // Validating mocks behaviour


        // Validating results
    }

    @Test
    public void delete_byId_UnauthorizedException_exception(){
        // Preparing data

        // Mocks & Stubs configuration

        when(productoService.findById("abc123")).thenReturn(Mono.error(new UnauthorizedException()));
        //when(productoService.delete(productoToDelete)).thenReturn(Mono.empty());

        // Business logic execution
        client.delete().uri("/productos/abc123")
                .exchange()
                .expectStatus().isUnauthorized();

        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        //verify(productoService,times(1)).delete(productoToDelete);
        verifyNoMoreInteractions(productoService);

        // Validating results
    }


    @Test
    public void post_createProductWithCategory_badRequest(){

        //TODO: Validar errores

        // Preparing data
        Producto productoBad =  new Producto();
        productoBad.setCategoria(new Categoria());

        // Mocks & Stubs configuration

        // Business logic execution
        client.post().uri("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                //.body(BodyInserters.fromValue(productoBad))
                .bodyValue("{\"categoria\":{\"id\":null,\"nombre\":null}}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CustomFieldError.class)
                .consumeWith(response -> {
                    List<CustomFieldError> errores =response.getResponseBody();

                    assertThat(errores).isNotEmpty().hasSize(3);
                    assertThat(errores).contains(new CustomFieldError("categoria.id","must not be empty"));
                    assertThat(errores).contains(new CustomFieldError("precio","must not be null"));
                    assertThat(errores).contains(new CustomFieldError("nombre","must not be empty"));
                });


        // Validating mocks behaviour

        verifyNoMoreInteractions(productoService);

        // Validating results

    }

    @Test
    public void post_createProduct_badRequest(){

        // Preparing data
        Producto productoBad =  new Producto();
        // Mocks & Stubs configuration

        // Business logic execution
        client.post().uri("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(productoBad))
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CustomFieldError.class)
                .consumeWith(response -> {
                    List<CustomFieldError> errores =response.getResponseBody();

                    assertThat(errores).isNotEmpty().hasSize(3);
                    assertThat(errores).contains(new CustomFieldError("categoria","must not be null"));
                    assertThat(errores).contains(new CustomFieldError("precio","must not be null"));
                    assertThat(errores).contains(new CustomFieldError("nombre","must not be empty"));
                });


        // Validating mocks behaviour

        verifyNoMoreInteractions(productoService);

        // Validating results

    }
    @Test
    public void post_uploadImage_created(){

        // Preparing data
        Producto producto =  new Producto();
        producto.setId("abc123");
        producto.setPrecio(1.5d);
        producto.setNombre("producto1");


        Producto productoToSave =  new Producto();
        productoToSave.setId("abc123");
        productoToSave.setPrecio(1.5d);
        productoToSave.setNombre("producto1");
        String fileName = "red-traffic-sign-to-print.jpg"
                .replace(" ","")
                .replace("\\","")
                .replace(":","");
        productoToSave.setFoto(fileName);


        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        Resource logo = new ClassPathResource("red-traffic-sign-to-print.jpg");
        bodyBuilder.part("file",logo);

        // Mocks & Stubs configuration
        when(productoService.findById("abc123")).thenReturn(Mono.just(producto));
        when(productoService.save(productoToSave)).thenReturn(Mono.just(productoToSave));

        client.post()
                .uri("/productos/abc123/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .header("Forwarded", "proto=https;host=api.yape.com")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/productos/abc123/images")
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();

                    assertThat(p.getId()).isEqualTo("abc123");
                    assertThat(p.getNombre()).isEqualTo("producto1");
                    assertThat(p.getPrecio()).isEqualTo(1.5d);
                });
    }

    @Test
    public void post_createProduct_created(){

        // Preparing data
        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setNombre("Categoria1");

        Date dateSystem = new Date();

        Producto productoToCreated =  new Producto();
        productoToCreated.setPrecio(1.5d);
        productoToCreated.setNombre("producto1");
        productoToCreated.setCategoria(categoria);

        Producto productoCreated =  new Producto();
        productoCreated.setId("abc123");
        productoCreated.setPrecio(1.5d);
        productoCreated.setNombre("producto1");
        productoCreated.setCategoria(categoria);
        productoCreated.setCreateAt(dateSystem);

        // Mocks & Stubs configuration
        when(productoService.save(productoToCreated)).thenReturn(Mono.just(productoCreated));

        // Business logic execution
        client.post().uri("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(productoToCreated))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/productos/abc123")
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    assertThat(p.getId()).isEqualTo("abc123");
                    assertThat(p.getNombre()).isEqualTo("producto1");
                    assertThat(p.getPrecio()).isEqualTo(1.5d);
                    assertThat(p.getCreateAt()).isEqualTo(dateSystem);
                    assertThat(p.getCategoria()).isEqualTo(categoria);
                    assertThat(p.getAppId()).isNullOrEmpty();
                });
        // Validating mocks behaviour
        verify(productoService,times(1)).save(productoToCreated);
        verifyNoMoreInteractions(productoService);

        // Validating results

    }

    @Test
    public void post_createProductWithApp_created() throws JOSEException {

        // Preparing data
        //TODO: DATA claro
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
                new Payload("{\"scope\":[\"token:application\",\"application:app2\"]," +
                        "\"exp\":1556040529," +
                        "\"authorities\":[\"ROLE_TRUSTED_CLIENT\",\"ROLE_CLIENT\"]," +
                        "\"jti\":\"1\"," +
                        "\"client_id\":\"app2\"}"));

        jwsObject.sign(new MACSigner(microserviceProperties.getJwt().getKey()));
        String JWT = jwsObject.serialize();

        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setNombre("Categoria1");

        Date dateSystem = new Date();

        Producto productoToCreated =  new Producto();
        productoToCreated.setPrecio(1.5d);
        productoToCreated.setNombre("producto1");
        productoToCreated.setCategoria(categoria);
        productoToCreated.setAppId("app2");

        Producto productoCreated =  new Producto();
        productoCreated.setId("abc123");
        productoCreated.setPrecio(1.5d);
        productoCreated.setNombre("producto1");
        productoCreated.setCategoria(categoria);
        productoCreated.setCreateAt(dateSystem);
        productoCreated.setAppId("app2");

        // Mocks & Stubs configuration
        when(productoService.save(productoToCreated)).thenReturn(Mono.just(productoCreated));

        // Business logic execution
        client.post().uri("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(productoToCreated))
                .headers(p->{
                    p.addIfAbsent("Authorization","Bearer " + JWT);
                })
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/productos/abc123")
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    assertThat(p.getId()).isEqualTo("abc123");
                    assertThat(p.getNombre()).isEqualTo("producto1");
                    assertThat(p.getPrecio()).isEqualTo(1.5d);
                    assertThat(p.getCreateAt()).isEqualTo(dateSystem);
                    assertThat(p.getCategoria()).isEqualTo(categoria);
                    assertThat(p.getAppId()).isEqualTo("app2");
                });
        // Validating mocks behaviour
        verify(productoService,times(1)).save(productoToCreated);
        verifyNoMoreInteractions(productoService);

        // Validating results
    }

    @Test
    public void get_productById_ok(){
        // Preparing data
        Producto p1 =  new Producto();
        p1.setId("abc123");
        p1.setPrecio(1.5d);
        p1.setNombre("producto1");

        // Mocks & Stubs configuration
        when(productoService.findById("abc123")).thenReturn(Mono.just(p1));

        // Business logic execution
        client.get().uri("/productos/{id}", Collections.singletonMap("id","abc123")).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    assertThat(p.getId()).isEqualTo("abc123");
                    assertThat(p.getNombre()).isEqualTo("producto1");
                    assertThat(p.getPrecio()).isEqualTo(1.5d);
                });
        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        verifyNoMoreInteractions(productoService);

        // Validating results
    }

    @Test
    public void get_productById2_ok(){
        // Preparing data
        Producto p1 =  new Producto();
        p1.setId("abc123");
        p1.setPrecio(1.5d);
        p1.setNombre("producto1");

        // Mocks & Stubs configuration
        when(productoService.findById("abc123")).thenReturn(Mono.just(p1));

        // Business logic execution
        client.get().uri("/productos/{id}", Collections.singletonMap("id","abc123")).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.id").isEqualTo("abc123")
                .jsonPath("$.nombre").isEqualTo("producto1")
                .jsonPath("$.precio").isEqualTo(1.5d);

        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        verifyNoMoreInteractions(productoService);

        // Validating results
    }

    @Test
    public void get_productById_notFound(){
        // Preparing data

        // Mocks & Stubs configuration
        when(productoService.findById("abc123")).thenReturn(Mono.empty());

        // Business logic execution
        client.get().uri("/productos/abc123").exchange()
                .expectStatus().isNotFound();
        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        verifyNoMoreInteractions(productoService);

        // Validating results

    }

    @Test
    public void get_products_ok(){
        // Preparing data
        Producto p1 =  new Producto();
        p1.setId("1");
        p1.setPrecio(1.5d);
        p1.setNombre("producto1");

        Producto p2 =  new Producto();
        p2.setId("2");
        p2.setNombre("producto2");
        p2.setPrecio(3.7d);

        List<Producto> lista = List.of(p1,p2);

        // Mocks & Stubs configuration
        when(productoService.findAll()).thenReturn(Flux.fromIterable(lista));

        // Business logic execution
        client.get().uri("/productos").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();


                    assertThat(productos.get(0).getId()).isEqualTo("1");
                    assertThat(productos.get(0).getNombre()).isEqualTo("producto1");
                    assertThat(productos.get(0).getPrecio()).isEqualTo(1.5d);

                    assertThat(productos.get(1).getId()).isEqualTo("2");
                    assertThat(productos.get(1).getNombre()).isEqualTo("producto2");
                    assertThat(productos.get(1).getPrecio()).isEqualTo(3.7d);

                })
                .hasSize(2);

        // Validating mocks behaviour
        verify(productoService,times(1)).findAll();
        verifyNoMoreInteractions(productoService);

        // Validating results

    }


    @Test
    public void get_products_StepVerifier_ok(){
        // Preparing data
        Producto p1 =  new Producto();
        p1.setId("1");
        p1.setPrecio(1.5d);
        p1.setNombre("producto1");

        Producto p2 =  new Producto();
        p2.setId("2");
        p2.setNombre("producto2");
        p2.setPrecio(3.7d);

        List<Producto> lista = List.of(p1,p2);

        // Mocks & Stubs configuration
        when(productoService.findAll()).thenReturn(Flux.fromIterable(lista));

        // Business logic execution
        Flux<Producto> productoFlux = client.get().uri("/productos")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(Producto.class)
                .getResponseBody();

        // Validating mocks behaviour
        verify(productoService,times(1)).findAll();
        verifyNoMoreInteractions(productoService);

        // Validating results
        StepVerifier.create(productoFlux)
                .expectNext(p1,p2)
                .verifyComplete();

    }


    @Test
    public void put_updateProduct_created(){
        // Preparing data

        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setNombre("Categoria1");

        Producto producToFromDB =  new Producto();
        producToFromDB.setId("abc123");
        producToFromDB.setPrecio(1.5d);
        producToFromDB.setNombre("producto1");
        producToFromDB.setCategoria(categoria);

        Categoria categoria2 = new Categoria();
        categoria2.setId("2");
        categoria2.setNombre("Categoria2");

        Producto producToUpdate =  new Producto();
        producToUpdate.setId("abc123");
        producToUpdate.setPrecio(2.5d);
        producToUpdate.setNombre("producto1_2");
        producToUpdate.setCategoria(categoria2);


        // Mocks & Stubs configuration
        when(productoService.findById("abc123")).thenReturn(Mono.just(producToFromDB));
        when(productoService.save(producToUpdate)).thenReturn(Mono.just(producToUpdate));

        // Business logic execution
        client.put().uri("/productos/abc123")
                .body(BodyInserters.fromValue(producToUpdate))
                .exchange()
                .expectStatus().isCreated();

        // Validating mocks behaviour
        verify(productoService,times(1)).findById("abc123");
        verify(productoService,times(1)).save(producToUpdate);

        verifyNoMoreInteractions(productoService);

        // Validating results
    }




    @Test
    public void post_createProductv2_created(){

        // Preparing data
        String fileName = "red-traffic-sign-to-print.jpg"
                .replace(" ","")
                .replace("\\","")
                .replace(":","");

        Categoria categoria = new Categoria();
        categoria.setId("1");
        categoria.setNombre("Categoria1");

        Producto productoToSave =  new Producto();
        productoToSave.setNombre("producto1");
        productoToSave.setPrecio(1.5d);
        productoToSave.setCategoria(categoria);
        productoToSave.setFoto(fileName);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();


        Resource logo = new ClassPathResource("red-traffic-sign-to-print.jpg");
        bodyBuilder.part("file", logo);
        bodyBuilder.part("nombre","producto1");
        bodyBuilder.part("precio","1.5");
        bodyBuilder.part("categoria.id","1");
        bodyBuilder.part("categoria.nombre","Categoria1");

        Producto productoSaved =  new Producto();
        productoSaved.setId("abc123");
        productoSaved.setNombre("producto1");
        productoSaved.setPrecio(1.5d);
        productoSaved.setCategoria(categoria);
        productoSaved.setFoto(fileName);


        // Mocks & Stubs configuration
        when(productoService.save(productoToSave)).thenReturn(Mono.just(productoSaved));

        client.post()
                .uri("/productosv2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .header("Forwarded", "proto=https;host=api.yape.com")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/productos/abc123")
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();

                    assertThat(p.getId()).isEqualTo("abc123");
                    assertThat(p.getNombre()).isEqualTo("producto1");
                    assertThat(p.getPrecio()).isEqualTo(1.5d);
                });

    }



}
