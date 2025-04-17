package com.example.porvenirsteaks.data.model.requests;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PedidoRequest {
    @SerializedName("ubicacion_id")
    private int ubicacion_id;

    @SerializedName("productos")
    private List<ProductoPedido> productos;

    @SerializedName("estado_anterior")
    private String estado_anterior;

    public String getEstadoAnterior() {
        return estado_anterior;
    }

    public void setEstadoAnterior(String estado_anterior) {
        this.estado_anterior = estado_anterior;
    }

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

    public static class ProductoPedido {
        @SerializedName("producto_id")
        private int producto_id;

        @SerializedName("cantidad")
        private int cantidad;

        public ProductoPedido(int producto_id, int cantidad) {
            this.producto_id = producto_id;
            this.cantidad = cantidad;
        }

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
}
