package com.example.porvenirsteaks.data.model.requests;

import java.util.List;

public class PedidoRequest {
    private int ubicacion_id;
    private List<ProductoPedido> productos;

    public static class ProductoPedido {
        private int producto_id;
        private int cantidad;

        public ProductoPedido(int producto_id, int cantidad) {
            this.producto_id = producto_id;
            this.cantidad = cantidad;
        }

        // Getters y Setters
        public int getProducto_id() {
            return producto_id;
        }

        public void setProducto_id(int producto_id) {
            this.producto_id = producto_id;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }
    }

    // Getters y Setters
    public int getUbicacion_id() {
        return ubicacion_id;
    }

    public void setUbicacion_id(int ubicacion_id) {
        this.ubicacion_id = ubicacion_id;
    }

    public List<ProductoPedido> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoPedido> productos) {
        this.productos = productos;
    }
}