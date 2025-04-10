package com.example.porvenirsteaks.ui.repartidor;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.data.repository.RepartidorRepository;
import com.example.porvenirsteaks.utils.LocationUtils;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class EntregasViewModel extends AndroidViewModel {
    private PedidoRepository pedidoRepository;
    private RepartidorRepository repartidorRepository;
    private MutableLiveData<Resource<List<Pedido>>> entregas = new MutableLiveData<>();
    private Pedido pedidoActual;

    // Para la actualización de ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean actualizandoUbicacion = false;

    public EntregasViewModel(@NonNull Application application) {
        super(application);
        pedidoRepository = new PedidoRepository(application);
        repartidorRepository = new RepartidorRepository(application);

        // Configurar solicitud de ubicación
        locationRequest = LocationRequest.create()
                .setInterval(10000)  // 10 segundos
                .setFastestInterval(5000)  // 5 segundos
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Enviar la ubicación al servidor
                    enviarUbicacion(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    public LiveData<Resource<List<Pedido>>> getEntregas() {
        return entregas;
    }

    public void cargarEntregas() {
        entregas.setValue(Resource.loading(null));

        // Obtiene los pedidos pendientes para el repartidor
        pedidoRepository.getPedidosPendientes().observeForever(result -> {
            entregas.setValue(result);
        });
    }

    public LiveData<Resource<Pedido>> getPedidoById(int pedidoId) {
        return pedidoRepository.getPedidoById(pedidoId);
    }

    public Pedido getPedidoActual() {
        return pedidoActual;
    }

    public void setPedidoActual(Pedido pedido) {
        this.pedidoActual = pedido;
    }

    public void iniciarActualizacionUbicacion(Context context) {
        if (actualizandoUbicacion) return;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Verificar permisos antes de iniciar
        if (LocationUtils.checkLocationPermission(context)) {
            try {
                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
                actualizandoUbicacion = true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void detenerActualizacionUbicacion() {
        if (fusedLocationClient != null && actualizandoUbicacion) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            actualizandoUbicacion = false;
        }
    }

    private void enviarUbicacion(double latitud, double longitud) {
        repartidorRepository.actualizarUbicacion(latitud, longitud).observeForever(result -> {
            // No necesitamos manejar la respuesta en la UI
        });
    }

    public LiveData<Resource<Pedido>> actualizarEstadoPedido(int pedidoId, String nuevoEstado) {
        return pedidoRepository.actualizarEstadoPedido(pedidoId, nuevoEstado);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        detenerActualizacionUbicacion();
    }
}