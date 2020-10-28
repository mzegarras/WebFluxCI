package com.example.lab04.models.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;


import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "productos")
@Data
public class Producto implements Serializable {

    private static final long serialVersionUID = 5504238253021606174L;

    private String x;

    @Id
    private String id;

    @NotEmpty
    private String nombre;

    @NotNull
    private Double precio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @EqualsAndHashCode.Exclude
    private Date createAt;

    public Producto() {
    }

    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, Double precio, Categoria categoria) {
        this(nombre, precio);
        this.categoria = categoria;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    //TODO: Spotbugs
    public Date getCreateAt() {
        //return this.createAt;
        return this.createAt != null ? (Date) createAt.clone() : null;
    }

    public void setCreateAt(Date createAt) {
        //this.createAt=createAt;
        this.createAt = createAt != null ? (Date) createAt.clone() : null;
    }

    @Valid
    @NotNull
    private Categoria categoria;

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    private String foto;

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
