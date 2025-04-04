package com.example.porvenirsteaks.data.model.responses;

import com.example.porvenirsteaks.data.model.User;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;
    private User user;
    private String token;

    // Getters y Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}