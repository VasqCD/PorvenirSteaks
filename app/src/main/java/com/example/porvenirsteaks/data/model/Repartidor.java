package com.example.porvenirsteaks.data.model;

import com.google.gson.annotations.SerializedName;

public class Repartidor {
    private int id;

    @SerializedName("usuario_id")
    private int usuarioId;

    private boolean disponible;

    @SerializedName("ultima_ubicacion_lat")
    private Double ultimaUbicacionLat;

    @SerializedName("ultima_ubicacion_lng")
    private Double ultimaUbicacionLng;

    @SerializedName("ultima_actualizacion")
    private String ultimaActualizacion;

    private User usuario;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public Double getUltimaUbicacionLat() {
        return ultimaUbicacionLat;
    }

    public void setUltimaUbicacionLat(Double ultimaUbicacionLat) {
        this.ultimaUbicacionLat = ultimaUbicacionLat;
    }

    public Double getUltimaUbicacionLng() {
        return ultimaUbicacionLng;
    }

    public void setUltimaUbicacionLng(Double ultimaUbicacionLng) {
        this.ultimaUbicacionLng = ultimaUbicacionLng;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(String ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}