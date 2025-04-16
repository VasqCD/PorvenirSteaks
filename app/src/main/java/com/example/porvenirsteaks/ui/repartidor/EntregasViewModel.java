package com.example.porvenirsteaks.ui.repartidor;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.porvenirsteaks.api.ApiService;
import com.example.porvenirsteaks.api.RetrofitClient;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.preferences.TokenManager;
import com.example.porvenirsteaks.data.repository.PedidoRepository;
import com.example.porvenirsteaks.data.repository.RepartidorRepository;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.LocationUtils;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntregasViewModel extends AndroidViewModel {
    private static final String TAG = "EntregasViewModel";
    private PedidoRepository pedidoRepository;
    private RepartidorRepository repartidorRepository;
    private MutableLiveData<Resource<List<Pedido>>> entregas = new MutableLiveData<>();
    private Pedido pedidoActual;

    // Para la actualización de ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean actualizandoUbicacion = false;
    private int errorCount = 0;
    private static final int MAX_ERRORS = 3; // Número máximo de errores antes de parar actualizaciones

    public EntregasViewModel(@NonNull Application application) {
        super(application);
        pedidoRepository = new PedidoRepository(application);
        repartidorRepository = new RepartidorRepository(application);

        // Configurar solicitud de ubicación con mayor precisión para Android 12+
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();

        // Callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Enviar la ubicación al servidor
                    Log.d(TAG, "Nueva ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
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

            if (result.status == Resource.Status.ERROR) {
                Log.e(TAG, "Error al cargar entregas: " + result.message);
            } else if (result.status == Resource.Status.SUCCESS) {
                Log.d(TAG, "Entregas cargadas correctamente: " +
                        (result.data != null ? result.data.size() : "0") + " pedidos");
            }
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
        errorCount = 0; // Reiniciar contador de errores

        // Verificar permisos antes de iniciar
        if (LocationUtils.checkLocationPermission(context)) {
            try {
                Log.d(TAG, "Iniciando actualizaciones de ubicación...");
                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
                actualizandoUbicacion = true;
            } catch (SecurityException e) {
                Log.e(TAG, "Error de seguridad al solicitar actualizaciones de ubicación", e);
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "No se tienen permisos de ubicación");
        }
    }

    public void detenerActualizacionUbicacion() {
        if (fusedLocationClient != null && actualizandoUbicacion) {
            Log.d(TAG, "Deteniendo actualizaciones de ubicación");
            fusedLocationClient.removeLocationUpdates(locationCallback);
            actualizandoUbicacion = false;
        }
    }

    private void enviarUbicacion(double latitud, double longitud) {
        repartidorRepository.actualizarUbicacion(latitud, longitud).observeForever(result -> {
            if (result.status == Resource.Status.SUCCESS) {
                Log.d(TAG, "Ubicación enviada al servidor correctamente");
                errorCount = 0; // Reiniciar contador si hay éxito
            } else if (result.status == Resource.Status.ERROR) {
                Log.e(TAG, "Error al enviar ubicación: " + result.message);
                errorCount++;

                // Si hay demasiados errores, parar las actualizaciones
                if (errorCount >= MAX_ERRORS) {
                    Log.e(TAG, "Demasiados errores al enviar ubicación, deteniendo actualizaciones");
                    detenerActualizacionUbicacion();
                }
            }
        });
    }

    public LiveData<Resource<Pedido>> actualizarEstadoPedido(int pedidoId, String nuevoEstado) {
        MutableLiveData<Resource<Pedido>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        try {
            pedidoRepository.actualizarEstadoPedido(pedidoId, nuevoEstado).observeForever(response -> {
                result.setValue(response);
            });
        } catch (Exception e) {
            Log.e("EntregasViewModel", "Error en actualizarEstadoPedido: " + e.getMessage());
            result.setValue(Resource.error("Error: " + e.getMessage(), null));
        }

        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        detenerActualizacionUbicacion();
    }
}