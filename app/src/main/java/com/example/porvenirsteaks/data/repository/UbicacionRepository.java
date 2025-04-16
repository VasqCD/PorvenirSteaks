package com.example.porvenirsteaks.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionRepository {
    private ApiService apiService;
    private MutableLiveData<Resource<List<Ubicacion>>> ubicacionesData;

    public UbicacionRepository(Context context) {
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
        // Inicializa la propiedad
        ubicacionesData = new MutableLiveData<>();
    }

    public LiveData<Resource<List<Ubicacion>>> getUbicaciones() {
        if (ubicacionesData.getValue() == null) {
            fetchUbicaciones();
        }
        return ubicacionesData;
    }

    public void fetchUbicaciones() {
        ubicacionesData.setValue(Resource.loading(null));

        apiService.getUbicaciones().enqueue(new Callback<List<Ubicacion>>() {
            @Override
            public void onResponse(Call<List<Ubicacion>> call, Response<List<Ubicacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ubicacionesData.setValue(Resource.success(response.body()));
                } else {
                    ubicacionesData.setValue(Resource.error("Error al obtener ubicaciones", null));
                }
            }

            @Override
            public void onFailure(Call<List<Ubicacion>> call, Throwable t) {
                ubicacionesData.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });
    }

    public LiveData<Resource<Ubicacion>> createUbicacion(UbicacionRequest request) {
        MutableLiveData<Resource<Ubicacion>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.createUbicacion(request).enqueue(new Callback<Ubicacion>() {
            @Override
            public void onResponse(Call<Ubicacion> call, Response<Ubicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    // Después de crear, actualiza la lista de ubicaciones
                    fetchUbicaciones();
                } else {
                    result.setValue(Resource.error("Error al crear ubicación", null));
                }
            }

            @Override
            public void onFailure(Call<Ubicacion> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Ubicacion>> updateUbicacion(int id, UbicacionRequest request) {
        MutableLiveData<Resource<Ubicacion>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.updateUbicacion(id, request).enqueue(new Callback<Ubicacion>() {
            @Override
            public void onResponse(Call<Ubicacion> call, Response<Ubicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    // Después de actualizar, actualiza la lista de ubicaciones
                    fetchUbicaciones();
                } else {
                    result.setValue(Resource.error("Error al actualizar ubicación", null));
                }
            }

            @Override
            public void onFailure(Call<Ubicacion> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> deleteUbicacion(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.deleteUbicacion(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                    // Después de eliminar, actualiza la lista de ubicaciones
                    fetchUbicaciones();
                } else {
                    result.setValue(Resource.error("Error al eliminar ubicación", false));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), false));
            }
        });

        return result;
    }
}