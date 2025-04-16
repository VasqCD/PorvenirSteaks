package com.example.porvenirsteaks.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.NetworkUtils;
import com.example.porvenirsteaks.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepartidorRepository {
    private Context context;
    private ApiService apiService;
    private static final String TAG = "RepartidorRepository";

    public RepartidorRepository(Context context) {
        this.context = context;
        refreshApiClient();
    }

    /**
     * Actualiza el cliente API con el token actual
     */
    private void refreshApiClient() {
        String token = TokenManager.getToken(context);
        this.apiService = RetrofitClient.getClient(token).create(ApiService.class);
    }

    /**
     * Obtiene la lista de repartidores
     */
    public LiveData<Resource<List<Repartidor>>> getRepartidores() {
        MutableLiveData<Resource<List<Repartidor>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        apiService.getRepartidores().enqueue(new Callback<List<Repartidor>>() {
            @Override
            public void onResponse(Call<List<Repartidor>> call, Response<List<Repartidor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<List<Repartidor>> call, Throwable t) {
                Log.e(TAG, "Error al obtener repartidores", t);
                result.setValue(Resource.error("Error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Obtiene los repartidores disponibles para asignación
     */
    public LiveData<Resource<List<Repartidor>>> getRepartidoresDisponibles() {
        MutableLiveData<Resource<List<Repartidor>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        // Este endpoint debe ser creado en el backend
        // Por ahora, filtraremos los repartidores disponibles desde el cliente
        apiService.getRepartidores().enqueue(new Callback<List<Repartidor>>() {
            @Override
            public void onResponse(Call<List<Repartidor>> call, Response<List<Repartidor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filtrar solo los disponibles
                    List<Repartidor> repartidores = response.body();
                    repartidores.removeIf(repartidor -> !repartidor.isDisponible());
                    result.setValue(Resource.success(repartidores));
                } else {
                    result.setValue(Resource.error("Error: " + response.message(), null));
                }
            }

            @Override
            public void onFailure(Call<List<Repartidor>> call, Throwable t) {
                Log.e(TAG, "Error al obtener repartidores disponibles", t);
                result.setValue(Resource.error("Error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Actualiza la ubicación del repartidor
     */
    public LiveData<Resource<Map<String, Object>>> actualizarUbicacion(double latitud, double longitud) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        // Asegurar que el token está actualizado
        refreshApiClient();

        Map<String, Double> request = new HashMap<>();
        request.put("latitud", latitud);
        request.put("longitud", longitud);

        apiService.actualizarUbicacionRepartidor(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    Log.d(TAG, "Ubicación actualizada correctamente");
                } else {
                    String errorMsg = "Error al actualizar ubicación: ";
                    try {
                        errorMsg += response.errorBody() != null ?
                                response.errorBody().string() :
                                response.message();
                    } catch (Exception e) {
                        errorMsg += response.message();
                    }

                    Log.e(TAG, errorMsg + " Código: " + response.code());
                    result.setValue(Resource.error(errorMsg, null));

                    // Si el token expiró (401)
                    if (response.code() == 401) {
                        Log.e(TAG, "Token expirado o inválido");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al actualizar ubicación", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Actualiza la disponibilidad del repartidor
     */
    public LiveData<Resource<Map<String, Object>>> cambiarDisponibilidad(boolean disponible) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        // Asegurar que el token está actualizado
        refreshApiClient();

        Map<String, Boolean> request = new HashMap<>();
        request.put("disponible", disponible);

        apiService.cambiarDisponibilidadRepartidor(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    Log.d(TAG, "Disponibilidad actualizada: " + disponible);
                } else {
                    String errorMsg = "Error al cambiar disponibilidad: ";
                    try {
                        errorMsg += response.errorBody() != null ?
                                response.errorBody().string() :
                                response.message();
                    } catch (Exception e) {
                        errorMsg += response.message();
                    }

                    Log.e(TAG, errorMsg + " Código: " + response.code());
                    result.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al cambiar disponibilidad", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }
}