package com.example.porvenirsteaks.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Notificacion;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacionRepository {
    private ApiService apiService;

    public NotificacionRepository(Context context) {
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
    }

    public LiveData<Resource<List<Notificacion>>> getNotificaciones() {
        MutableLiveData<Resource<List<Notificacion>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getNotificaciones().enqueue(new Callback<List<Notificacion>>() {
            @Override
            public void onResponse(Call<List<Notificacion>> call, Response<List<Notificacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener notificaciones", null));
                }
            }

            @Override
            public void onFailure(Call<List<Notificacion>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Notificacion>> marcarComoLeida(int id) {
        MutableLiveData<Resource<Notificacion>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.marcarNotificacionLeida(id).enqueue(new Callback<Notificacion>() {
            @Override
            public void onResponse(Call<Notificacion> call, Response<Notificacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al marcar notificación", null));
                }
            }

            @Override
            public void onFailure(Call<Notificacion> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> marcarTodasComoLeidas() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.marcarTodasNotificacionesLeidas().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error("Error al marcar notificaciones", false));
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