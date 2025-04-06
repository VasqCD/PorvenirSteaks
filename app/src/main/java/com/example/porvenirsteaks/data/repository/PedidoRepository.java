package com.example.porvenirsteaks.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.requests.PedidoRequest;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoRepository {
    private ApiService apiService;

    public PedidoRepository(Context context) {
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
    }

    public LiveData<Resource<List<Pedido>>> getPedidos() {
        MutableLiveData<Resource<List<Pedido>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPedidos().enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(Call<List<Pedido>> call, Response<List<Pedido>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener pedidos", null));
                }
            }

            @Override
            public void onFailure(Call<List<Pedido>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Pedido>> getPedidoById(int id) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPedidoById(id).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener pedido", null));
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Pedido>> createPedido(PedidoRequest pedidoRequest) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.createPedido(pedidoRequest).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al crear pedido", null));
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Pedido>> calificarPedido(int pedidoId, int calificacion, String comentario) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> request = new HashMap<>();
        request.put("calificacion", calificacion);
        request.put("comentario_calificacion", comentario);

        apiService.calificarPedido(pedidoId, request).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al calificar pedido", null));
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }
}