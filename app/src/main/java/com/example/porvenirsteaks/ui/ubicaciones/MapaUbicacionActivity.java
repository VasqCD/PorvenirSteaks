package com.example.porvenirsteaks.ui.ubicaciones;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.databinding.ActivityMapaUbicacionBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaUbicacionActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityMapaUbicacionBinding binding;
    private GoogleMap mMap;
    private Marker currentMarker;
    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapaUbicacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener coordenadas iniciales (si se han pasado)
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupButtons();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configurar el mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Si tenemos coordenadas iniciales, centrar el mapa ahí y añadir un marcador
        if (latitude != 0 && longitude != 0) {
            LatLng initialPosition = new LatLng(latitude, longitude);
            moveMapTo(initialPosition);
            addOrUpdateMarker(initialPosition);
        } else {
            // Ubicación por defecto (Tegucigalpa)
            LatLng defaultLocation = new LatLng(14.0650, -87.1715);
            moveMapTo(defaultLocation);
        }

        // Configurar listener para clicks en el mapa
        mMap.setOnMapClickListener(this::addOrUpdateMarker);
    }

    private void moveMapTo(LatLng position) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
        }
    }

    private void addOrUpdateMarker(LatLng position) {
        latitude = position.latitude;
        longitude = position.longitude;

        if (currentMarker != null) {
            currentMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title("Ubicación seleccionada")
                .draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        // Configurar arrastre del marcador
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                latitude = position.latitude;
                longitude = position.longitude;
                Toast.makeText(MapaUbicacionActivity.this,
                        "Nueva posición: " + latitude + ", " + longitude,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        binding.btnGuardarUbicacion.setOnClickListener(v -> {
            if (latitude != 0 && longitude != 0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", latitude);
                resultIntent.putExtra("longitude", longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, selecciona una ubicación",
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCancelar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}