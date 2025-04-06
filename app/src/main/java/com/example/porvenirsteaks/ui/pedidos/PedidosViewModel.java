package com.example.porvenirsteaks.ui.pedidos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.data.model.DetallePedido;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidosViewModel extends AndroidViewModel {
    private PedidoRepository repository;
    private MutableLiveData<String> filtroEstado = new MutableLiveData<>();

    public PedidosViewModel(@NonNull Application application) {
        super(application);
        repository = new PedidoRepository(application);
    }

    public LiveData<Resource<List<Pedido>>> getPedidos() {
        return repository.getPedidos();
    }

    public LiveData<Resource<Pedido>> getPedidoById(int id) {
        return repository.getPedidoById(id);
    }

    public LiveData<Resource<Pedido>> calificarPedido(int pedidoId, int calificacion, String comentario) {
        return repository.calificarPedido(pedidoId, calificacion, comentario);
    }

    public void setFiltroEstado(String estado) {
        filtroEstado.setValue(estado);
    }

    public LiveData<String> getFiltroEstado() {
        return filtroEstado;
    }
}