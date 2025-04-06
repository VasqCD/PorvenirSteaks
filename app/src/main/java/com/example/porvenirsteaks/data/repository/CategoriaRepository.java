package com.example.porvenirsteaks.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriaRepository {
    private ApiService apiService;

    public CategoriaRepository(Context context) {
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
    }

    public LiveData<Resource<List<Categoria>>> getCategorias() {
        MutableLiveData<Resource<List<Categoria>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener categorías", null));
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Categoria>> getCategoriaById(int id) {
        MutableLiveData<Resource<Categoria>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getCategoriaById(id).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener categoría", null));
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }
}