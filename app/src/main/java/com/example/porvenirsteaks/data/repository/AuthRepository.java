package com.example.porvenirsteaks.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.model.requests.LoginRequest;
import com.example.porvenirsteaks.data.model.requests.RegisterRequest;
import com.example.porvenirsteaks.data.model.responses.LoginResponse;
import com.example.porvenirsteaks.data.model.responses.RegisterResponse;
import com.example.porvenirsteaks.data.model.responses.UserResponse;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.NetworkUtils;
import com.example.porvenirsteaks.utils.Resource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private Context context;
    private static final String TAG = "AuthRepository";

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
                    try {
                        // Verificar si la respuesta es HTML
                        String contentType = response.headers().get("Content-Type");
                        if (contentType != null && contentType.contains("text/html")) {
                            Log.e("AuthRepository", "El servidor devolvió HTML en lugar de JSON. Verifique la URL y la configuración del servidor.");
                            loginResult.setValue(Resource.error("Error de servidor: Formato de respuesta incorrecto", null));
                            return;
                        }

                        if (response.code() == 403) {
                            // Intentar extraer el JSON de error
                            String errorString = response.errorBody().string();
                            Log.d("AuthRepository", "Error 403: " + errorString);
                            JSONObject errorBody = new JSONObject(errorString);
                            if (errorBody.has("verification_required") && errorBody.getBoolean("verification_required")) {
                                // Crear un error especial para verificación requerida
                                loginResult.setValue(Resource.verificationRequired(
                                        errorBody.getString("message"),
                                        errorBody.getString("email")
                                ));
                                return;
                            }
                        }

                        // Para otros casos de error, mostrar el mensaje adecuado
                        if (response.errorBody() != null) {
                            String errorString = response.errorBody().string();
                            Log.d("AuthRepository", "Error body: " + errorString);
                            try {
                                JSONObject errorBody = new JSONObject(errorString);
                                if (errorBody.has("message")) {
                                    loginResult.setValue(Resource.error(errorBody.getString("message"), null));
                                } else {
                                    loginResult.setValue(Resource.error("Credenciales incorrectas", null));
                                }
                            } catch (Exception e) {
                                loginResult.setValue(Resource.error("Credenciales incorrectas", null));
                            }
                        } else {
                            loginResult.setValue(Resource.error("Error de conexión", null));
                        }
                    } catch (Exception e) {
                        Log.e("AuthRepository", "Error al procesar respuesta: " + e.getMessage(), e);
                        loginResult.setValue(Resource.error("Error al procesar la respuesta: " + e.getMessage(), null));
                    }
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
                    try {
                        JSONObject errorBody = new JSONObject(response.errorBody().string());
                        if (errorBody.has("errors")) {
                            JSONObject errors = errorBody.getJSONObject("errors");

                            if (errors.has("email")) {
                                // Error de email
                                String emailError = errors.getJSONArray("email").getString(0);
                                registerResult.setValue(Resource.error(emailError, null));
                            } else if (errors.has("telefono")) {
                                // Error de teléfono
                                String telefonoError = errors.getJSONArray("telefono").getString(0);
                                registerResult.setValue(Resource.error(telefonoError, null));
                            } else {
                                // Otro error de validación
                                registerResult.setValue(Resource.error("Error en el registro: datos inválidos", null));
                            }
                        } else {
                            registerResult.setValue(Resource.error("Error en el registro", null));
                        }
                    } catch (Exception e) {
                        registerResult.setValue(Resource.error("Error en el registro: " + e.getMessage(), null));
                    }
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

        apiService.getUserProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    User user = response.body().getUser();
                    result.setValue(Resource.success(user));
                } else {
                    // Manejo de errores...
                    result.setValue(Resource.error("Error al obtener perfil", null));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error al obtener perfil", t);
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