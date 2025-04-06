package com.example.porvenirsteaks.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.model.requests.LoginRequest;
import com.example.porvenirsteaks.data.model.requests.RegisterRequest;
import com.example.porvenirsteaks.data.model.responses.LoginResponse;
import com.example.porvenirsteaks.data.model.responses.RegisterResponse;
import com.example.porvenirsteaks.data.repository.AuthRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.Map;

public class AuthViewModel extends AndroidViewModel {
    private AuthRepository repository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        return repository.login(request);
    }

    public LiveData<Resource<RegisterResponse>> register(String name, String apellido, String email, String password, String telefono) {
        RegisterRequest request = new RegisterRequest(name, apellido, email, password, telefono);
        return repository.register(request);
    }

    public LiveData<Resource<Map<String, Object>>> verificarCodigo(String email, String codigo) {
        return repository.verificarCodigo(email, codigo);
    }

    public LiveData<Resource<User>> getUserProfile() {
        return repository.getUserProfile();
    }

    public LiveData<Resource<Boolean>> logout() {
        return repository.logout();
    }
}