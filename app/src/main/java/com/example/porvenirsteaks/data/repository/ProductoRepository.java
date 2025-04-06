package com.example.porvenirsteaks.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoRepository {
    private ApiService apiService;

    public ProductoRepository(Context context) {
        apiService = RetrofitClient.getClient(TokenManager.getToken(context))
                .create(ApiService.class);
    }

    public LiveData<Resource<List<Producto>>> getProductos() {
        MutableLiveData<Resource<List<Producto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener productos", null));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<Producto>>> getProductosByCategoria(int categoriaId) {
        MutableLiveData<Resource<List<Producto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getProductosByCategoria(categoriaId).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener productos por categoría", null));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Producto>> getProductoById(int id) {
        MutableLiveData<Resource<Producto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getProductoById(id).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al obtener producto", null));
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<Producto>>> searchProductos(String query) {
        MutableLiveData<Resource<List<Producto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.searchProductos(query).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error en la búsqueda", null));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }
}