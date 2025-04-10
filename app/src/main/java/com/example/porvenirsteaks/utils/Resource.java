package com.example.porvenirsteaks.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Clase para manejar estados de la carga de datos
public class Resource<T> {
    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;
    public String email;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public static <T> Resource<T> verificationRequired(String msg, String email) {
        Resource<T> resource = new Resource<>(Status.VERIFICATION_REQUIRED, null, msg);
        resource.email = email;
        return resource;
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING,
        VERIFICATION_REQUIRED
    }
}