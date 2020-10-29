package com.example.lab04.services;

import com.example.lab04.config.MicroserviceProperties;
import com.example.lab04.models.dao.ProductoDao;
import com.example.lab04.models.documents.Producto;
import com.example.lab04.models.services.ProductoService;
import com.example.lab04.models.services.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ProductoServiceImplTest.TestConfiguration.class})
public class ProductoServiceImplTest {

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

            return microserviceProperties;
        }

        @Bean
        public ProductoService productoService(MicroserviceProperties microserviceProperties, ProductoDao productoDao){
            return new ProductoServiceImpl(productoDao, microserviceProperties);
        }

    }

    @MockBean
    private ProductoDao productoDao;

    @Autowired
    private MicroserviceProperties microserviceProperties;

    @Autowired
    private ProductoService productoService;

    @BeforeEach
    void resetMocksAndStubs() {
        reset(productoDao);
        microserviceProperties.setInPanic(false);
    }

    @Test
    public void sanity() {
        assertThat(productoService).isNotNull();
        assertThat(microserviceProperties).isNotNull();
        assertThat(productoDao).isNotNull();
    }

    @Test
    public void findAll_ok(){

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
        when(productoDao.findAll()).thenReturn(Flux.fromIterable(lista));

        // Business logic execution
        Flux<Producto> productoFlux =  productoService.findAll();

        // Validating mocks behaviour

        verify(productoDao,times(1)).findAll();
        verifyNoMoreInteractions(productoDao);

        // Validating results
        StepVerifier.create(productoFlux.log())
                .expectNext(p1,p2)
                .verifyComplete();
    }

    @Test
    public void findAllWithNameUpperCase_ok(){

        //TODO: YP - Verificar la lógica de negocio

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
        when(productoDao.findAll()).thenReturn(Flux.fromIterable(lista));

        // Business logic execution
        Flux<Producto> productoFlux =  productoService.findAllWithNameUpperCase();

        // Validating mocks behaviour

        verify(productoDao,times(1)).findAll();
        verifyNoMoreInteractions(productoDao);

        // Validating results

        StepVerifier.create(productoFlux.log())
                //.expectNext(p1,p2)
                //.expectNextCount(2)
                .consumeNextWith(prouctoExpected -> {
                    assertThat(prouctoExpected.getId()).isEqualTo("1");
                    assertThat(prouctoExpected.getPrecio()).isEqualTo(1.5d);
                    assertThat(prouctoExpected.getNombre()).isEqualTo("PRODUCTO1");
                })
                .consumeNextWith(prouctoExpected -> {
                    assertThat(prouctoExpected.getId()).isEqualTo("2");
                    assertThat(prouctoExpected.getPrecio()).isEqualTo(3.7d);
                    assertThat(prouctoExpected.getNombre()).isEqualTo("PRODUCTO2");
                })
                .verifyComplete();
    }

    /*
    @Test
    public void findAll_error(){

        // Preparing data

        // Mocks & Stubs configuration
        when(productoDao.findAll()).thenReturn(Flux.error(new RuntimeException()));

        // Business logic execution
        Flux<Producto> productoFlux =  productoService.findAll();

        // Validating mocks behaviour

        verify(productoDao,times(1)).findAll();
        verifyNoMoreInteractions(productoDao);

        // Validating results
        StepVerifier.create(productoFlux.log())
                .expectError(RuntimeException.class)
                .verify();
    }*/


}
