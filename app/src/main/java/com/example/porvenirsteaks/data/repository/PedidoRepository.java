package com.example.porvenirsteaks.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.requests.PedidoRequest;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.utils.NetworkUtils;
import com.example.porvenirsteaks.utils.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoRepository {
    private ApiService apiService;
    private Context context;
    private static final String TAG = "PedidoRepository";
    private String estado_anterior = "creado"; // Valor por defecto para nuevos pedidos



    public PedidoRepository(Context context) {
        this.context = context;
        String token = TokenManager.getToken(context);
        this.apiService = RetrofitClient.getClient(token).create(ApiService.class);
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

    /**
     * Obtiene los pedidos pendientes para repartidores
     */
    public LiveData<Resource<List<Pedido>>> getPedidosPendientes() {
        MutableLiveData<Resource<List<Pedido>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        apiService.getPedidosPendientes().enqueue(new Callback<List<Pedido>>() {
            @Override
            public void onResponse(Call<List<Pedido>> call, Response<List<Pedido>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                    Log.d(TAG, "Pedidos pendientes obtenidos: " + response.body().size());
                } else {
                    String errorMsg = "Error al obtener pedidos pendientes: ";
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
            public void onFailure(Call<List<Pedido>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al obtener pedidos pendientes", t);
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Pedido>> actualizarEstadoPedido(int pedidoId, String nuevoEstado) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, String> request = new HashMap<>();
        request.put("estado", nuevoEstado);

        apiService.actualizarEstadoPedido(pedidoId, request).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Error al actualizar estado", null));
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                result.setValue(Resource.error("Error de conexión: " + t.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Asigna un repartidor a un pedido
     */
    public LiveData<Resource<Pedido>> asignarRepartidor(int pedidoId, int repartidorId) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (!NetworkUtils.isNetworkAvailable(context)) {
            result.setValue(Resource.error("No hay conexión a internet", null));
            return result;
        }

        Map<String, Integer> request = new HashMap<>();
        request.put("repartidor_id", repartidorId);

        apiService.asignarRepartidor(pedidoId, request).enqueue(new Callback<Pedido>() {
            @Override
            public void onResponse(Call<Pedido> call, Response<Pedido> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    String errorMessage = "Error: ";
                    try {
                        errorMessage += response.errorBody() != null ?
                                response.errorBody().string() : response.message();
                    } catch (IOException e) {
                        errorMessage += response.message();
                    }
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<Pedido> call, Throwable t) {
                Log.e(TAG, "Error al asignar repartidor", t);
                result.setValue(Resource.error("Error: " + t.getMessage(), null));
            }
        });

        return result;
    }
}