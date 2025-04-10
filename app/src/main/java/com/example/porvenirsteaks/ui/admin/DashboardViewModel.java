package com.example.porvenirsteaks.ui.admin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.data.repository.RepartidorRepository;
import com.example.porvenirsteaks.utils.Resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel extends AndroidViewModel {
    private PedidoRepository pedidoRepository;
    private RepartidorRepository repartidorRepository;

    private MutableLiveData<Integer> pedidosPendientesCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> pedidosHoyCount = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        pedidoRepository = new PedidoRepository(application);
        repartidorRepository = new RepartidorRepository(application);

        // Inicializar datos
        getPedidosPendientes();
        getPedidosHoy();
    }

    // Getters para contadores
    public LiveData<Integer> getPedidosPendientesCount() {
        return pedidosPendientesCount;
    }

    public LiveData<Integer> getPedidosHoyCount() {
        return pedidosHoyCount;
    }

    // Métodos para obtener datos del servidor
    public void getPedidosPendientes() {
        pedidoRepository.getPedidosPendientes().observeForever(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                pedidosPendientesCount.setValue(result.data.size());
            }
        });
    }

    private void getPedidosHoy() {
        // Obtener la fecha de hoy en formato ISO
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());

        // Aquí podrías tener un método específico para obtener pedidos por fecha
        // Por ahora, usaremos getPedidos() y filtraremos los resultados
        pedidoRepository.getPedidos().observeForever(result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                // Contar pedidos que sean de hoy
                int count = 0;
                for (Pedido pedido : result.data) {
                    if (pedido.getFechaPedido().startsWith(today)) {
                        count++;
                    }
                }
                pedidosHoyCount.setValue(count);
            }
        });
    }

    public LiveData<Resource<List<Pedido>>> getPedidosRecientes() {
        return pedidoRepository.getPedidos();
    }

    public LiveData<Resource<List<Repartidor>>> getRepartidoresDisponibles() {
        return repartidorRepository.getRepartidoresDisponibles();
    }

    public LiveData<Resource<Pedido>> asignarPedidoARepartidor(int pedidoId, int repartidorId) {
        return pedidoRepository.asignarRepartidor(pedidoId, repartidorId);
    }
}