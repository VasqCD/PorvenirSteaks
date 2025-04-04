package com.example.porvenirsteaks.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Categoria {
    private int id;
    private String nombre;
    private String descripcion;

    @SerializedName("productos_count")
    private int productosCount;

    private List<Producto> productos;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getProductosCount() {
        return productosCount;
    }

    public void setProductosCount(int productosCount) {
        this.productosCount = productosCount;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}