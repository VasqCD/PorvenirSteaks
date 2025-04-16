package com.example.porvenirsteaks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Clase utilitaria para gestionar el mapa de entregas para repartidores
 */
public class MapaManager implements OnMapReadyCallback {
    private static final String TAG = "MapaManager";
    private static final float DEFAULT_ZOOM = 14f;
    private static final int BOUNDS_PADDING = 150;

    private Context context;
    private MapView mapView;
    private GoogleMap mMap;
    private LatLng currentLocation;
    private List<Pedido> pedidosList;
    private boolean isMapReady = false;

    public MapaManager(Context context, FrameLayout container) {
        this.context = context;

        // Inflar el layout del mapa
        View mapLayout = LayoutInflater.from(context).inflate(R.layout.layout_map_preview, container, true);
        mapView = mapLayout.findViewById(R.id.mapView);

        // Inicializar mapa
        initializeMap();
    }

    private void initializeMap() {
        try {
            mapView.onCreate(null);
            mapView.onResume();
            MapsInitializer.initialize(context);
            mapView.getMapAsync(this);
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar el mapa: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;

        // Configurar mapa
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Actualizar mapa si ya tenemos datos
        updateMap();
    }

    /**
     * Establece la ubicación actual del repartidor
     */
    public void setCurrentLocation(double latitude, double longitude) {
        this.currentLocation = new LatLng(latitude, longitude);
        updateMap();
    }

    /**
     * Establece la lista de pedidos para mostrar en el mapa
     */
    public void setPedidos(List<Pedido> pedidos) {
        this.pedidosList = pedidos;
        updateMap();
    }

    /**
     * Actualiza el mapa con los marcadores
     */
    private void updateMap() {
        if (!isMapReady || mMap == null) {
            return;
        }

        mMap.clear();

        // Si no hay ubicación actual, no podemos mostrar nada
        if (currentLocation == null) {
            return;
        }

        // Añadir marcador para la ubicación actual
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Mi ubicación")
                .icon(getBitmapFromVector(R.drawable.ic_my_location)));

        // Iniciar el constructor de límites para ajustar el zoom
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(currentLocation);

        // Añadir marcadores para cada pedido con ubicación válida
        if (pedidosList != null && !pedidosList.isEmpty()) {
            for (Pedido pedido : pedidosList) {
                Ubicacion ubicacion = pedido.getUbicacion();
                if (ubicacion != null && ubicacion.getLatitud() != 0 && ubicacion.getLongitud() != 0) {
                    LatLng pedidoLatLng = new LatLng(ubicacion.getLatitud(), ubicacion.getLongitud());
                    mMap.addMarker(new MarkerOptions()
                            .position(pedidoLatLng)
                            .title("Pedido #" + pedido.getId())
                            .icon(getBitmapFromVector(R.drawable.ic_location)));
                    boundsBuilder.include(pedidoLatLng);
                }
            }
        }

        try {
            // Si hay múltiples marcadores, ajustar la cámara para mostrar todos
            if (pedidosList != null && !pedidosList.isEmpty()) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), BOUNDS_PADDING));
            } else {
                // Si solo está el marcador de ubicación actual, centrar en él
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLocation, DEFAULT_ZOOM));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ajustar cámara: " + e.getMessage());
            // En caso de error, simplemente centrar en la ubicación actual
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    currentLocation, DEFAULT_ZOOM));
        }
    }

    /**
     * Convierte un recurso vectorial a un BitmapDescriptor para usarlo como marcador
     */
    private BitmapDescriptor getBitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();

        // Si el tamaño es cero o negativo, usar un valor predeterminado
        if (width <= 0 || height <= 0) {
            width = 40;
            height = 40;
        }

        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Métodos para manejar el ciclo de vida del mapa
     */
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    public void onLowMemory() {
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }
}