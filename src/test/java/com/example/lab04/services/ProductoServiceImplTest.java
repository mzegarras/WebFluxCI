package com.example.lab04.services;

import com.example.lab04.config.FilesProperties;
import com.example.lab04.models.dao.ProductoDao;
import com.example.lab04.models.documents.Producto;
import com.example.lab04.models.services.ProductoService;
import com.example.lab04.models.services.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        public FilesProperties filesProperties(){
            FilesProperties filesProperties = new FilesProperties();
            filesProperties.setPath("./");
            return filesProperties;
        }

        @Bean
        public ProductoService productoService(FilesProperties filesProperties,ProductoDao productoDao){
            return new ProductoServiceImpl(productoDao,filesProperties);
        }

    }

    @MockBean
    private ProductoDao productoDao;

    @Autowired
    private FilesProperties filesProperties;

    @Autowired
    private ProductoService productoService;

    @BeforeEach
    void resetMocksAndStubs() {
        reset(productoDao);
        filesProperties.setInPanic(false);
    }

    @Test
    public void sanity() {
        assertThat(productoService).isNotNull();
        assertThat(filesProperties).isNotNull();
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

                .verifyComplete();
    }*/


}
