package com.example.porvenirsteaks.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.model.requests.LoginRequest;
import com.example.porvenirsteaks.data.model.requests.RegisterRequest;
import com.example.porvenirsteaks.data.model.responses.LoginResponse;
import com.example.porvenirsteaks.data.model.responses.RegisterResponse;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.NetworkUtils;
import com.example.porvenirsteaks.utils.Resource;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private Context context;

    public AuthRepository(Context context) {
        this.context = context;
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
    }

    public LiveData<Resource<LoginResponse>> login(LoginRequest loginRequest) {
        MutableLiveData<Resource<LoginResponse>> loginResult = new MutableLiveData<>();
        loginResult.setValue(Resource.loading(null));

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResult.setValue(Resource.success(response.body()));
                } else {
                    loginResult.setValue(Resource.error("Credenciales incorrectas", null));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginResult.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return loginResult;
    }

    public LiveData<Resource<RegisterResponse>> register(RegisterRequest registerRequest) {
        MutableLiveData<Resource<RegisterResponse>> registerResult = new MutableLiveData<>();
        registerResult.setValue(Resource.loading(null));

        apiService.register(registerRequest).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    registerResult.setValue(Resource.success(response.body()));
                } else {
                    registerResult.setValue(Resource.error("Error en el registro", null));
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                registerResult.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return registerResult;
    }

    public LiveData<Resource<Map<String, Object>>> verificarCodigo(String email, String codigo) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("codigo", codigo);

        apiService.verificarCodigo(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Código inválido", null));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> getUserProfile() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener perfil", null));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> logout() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.logout().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    // Limpiar token localmente
                    TokenManager.clearToken(context);
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error("Error al cerrar sesión", false));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Incluso si falla la API, limpiamos el token
                TokenManager.clearToken(context);
                result.setValue(Resource.success(true));
            }
        });

        return result;
    }

    public LiveData<Resource<Map<String, Object>>> reenviarCodigo(String email) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        apiService.reenviarCodigo(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error al reenviar el código";
                        result.setValue(Resource.error(errorBody, null));
                    } catch (Exception e) {
                        result.setValue(Resource.error("Error al reenviar el código: " + e.getMessage(), null));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Map<String, Object>>> recuperarPassword(String email) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        apiService.recuperarPassword(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error al solicitar recuperación de contraseña";
                        result.setValue(Resource.error(errorBody, null));
                    } catch (Exception e) {
                        result.setValue(Resource.error("Error al solicitar recuperación: " + e.getMessage(), null));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Map<String, Object>>> cambiarPassword(String email, String codigo, String password, String passwordConfirmation) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("codigo", codigo);
        request.put("password", password);
        request.put("password_confirmation", passwordConfirmation);

        apiService.cambiarPassword(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error al cambiar la contraseña";
                        result.setValue(Resource.error(errorBody, null));
                    } catch (Exception e) {
                        result.setValue(Resource.error("Error al cambiar contraseña: " + e.getMessage(), null));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }
}