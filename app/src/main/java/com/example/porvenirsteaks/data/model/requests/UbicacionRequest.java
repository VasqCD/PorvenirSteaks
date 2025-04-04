package com.example.porvenirsteaks.data.model.requests;

public class UbicacionRequest {
    private double latitud;
    private double longitud;
    private String direccion_completa;
    private String calle;
    private String numero;
    private String colonia;
    private String ciudad;
    private String codigo_postal;
    private String referencias;
    private String etiqueta;
    private boolean es_principal;

    // Constructor
    public UbicacionRequest(double latitud, double longitud, String direccion_completa,
                            String calle, String numero, String colonia, String ciudad,
                            String codigo_postal, String referencias, String etiqueta,
                            boolean es_principal) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion_completa = direccion_completa;
        this.calle = calle;
        this.numero = numero;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.codigo_postal = codigo_postal;
        this.referencias = referencias;
        this.etiqueta = etiqueta;
        this.es_principal = es_principal;
    }

    // Getters y Setters
    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getDireccion_completa() {
        return direccion_completa;
    }

    public void setDireccion_completa(String direccion_completa) {
        this.direccion_completa = direccion_completa;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigo_postal() {
        return codigo_postal;
    }

    public void setCodigo_postal(String codigo_postal) {
        this.codigo_postal = codigo_postal;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public boolean isEs_principal() {
        return es_principal;
    }

    public void setEs_principal(boolean es_principal) {
        this.es_principal = es_principal;
    }
}