package com.example.porvenirsteaks.ui.ubicaciones;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.MainActivity;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.databinding.ActivityCompleteAddressBinding;
import com.example.porvenirsteaks.utils.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Actividad para completar los datos de una dirección.
 * Se muestra después de confirmar la ubicación actual del usuario.
 */
public class CompleteAddressActivity extends AppCompatActivity {
    private static final String TAG = "CompleteAddress";

    private ActivityCompleteAddressBinding binding;
    private UbicacionesViewModel viewModel;

    private double latitude = 0;
    private double longitude = 0;
    private boolean isFirstLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UbicacionesViewModel.class);

        // Obtener coordenadas pasadas desde la actividad anterior
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        isFirstLogin = getIntent().getBooleanExtra("isFirstLogin", false);

        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Error: No se recibieron coordenadas de ubicación",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        geocodeLocation();
    }

    private void setupUI() {
        // Mostrar coordenadas actuales (opcional)
        binding.tvCoordenadas.setText("Lat: " + latitude + ", Lng: " + longitude);

        // Configurar botón para guardar la dirección
        binding.btnGuardarDireccion.setOnClickListener(v -> {
            if (validateForm()) {
                saveAddress();
            }
        });

        // Configurar botón para regresar sin guardar
        binding.btnVolver.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Realiza geocodificación inversa para obtener detalles de la dirección
     * a partir de las coordenadas de ubicación.
     */
    private void geocodeLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Llenar formulario con datos obtenidos
                binding.etCalle.setText(address.getThoroughfare() != null ?
                        address.getThoroughfare() : "");
                binding.etNumero.setText(address.getSubThoroughfare() != null ?
                        address.getSubThoroughfare() : "");
                binding.etColonia.setText(address.getSubLocality() != null ?
                        address.getSubLocality() : "");
                binding.etCiudad.setText(address.getLocality() != null ?
                        address.getLocality() : "");
                binding.etCodigoPostal.setText(address.getPostalCode() != null ?
                        address.getPostalCode() : "");

                // Crear dirección completa para mostrar
                StringBuilder direccionCompleta = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) direccionCompleta.append(", ");
                    direccionCompleta.append(address.getAddressLine(i));
                }

                // Mostrar dirección completa
                binding.tvDireccionCompleta.setText(direccionCompleta.toString());

                // Sugerir una etiqueta basada en la dirección
                if (address.getFeatureName() != null) {
                    binding.etEtiqueta.setText("Casa"); // Default a "Casa"
                }
            } else {
                binding.tvDireccionCompleta.setText("No se pudo obtener la dirección para estas coordenadas.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error en geocodificación", e);
            binding.tvDireccionCompleta.setText("Error al obtener dirección: " + e.getMessage());
        } finally {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Valida el formulario antes de guardar la dirección.
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Validar etiqueta
        if (TextUtils.isEmpty(binding.etEtiqueta.getText())) {
            binding.tilEtiqueta.setError("La etiqueta es requerida");
            isValid = false;
        } else {
            binding.tilEtiqueta.setError(null);
        }

        // Validar calle
        if (TextUtils.isEmpty(binding.etCalle.getText())) {
            binding.tilCalle.setError("La calle es requerida");
            isValid = false;
        } else {
            binding.tilCalle.setError(null);
        }

        // Validar número
        if (TextUtils.isEmpty(binding.etNumero.getText())) {
            binding.tilNumero.setError("El número es requerido");
            isValid = false;
        } else {
            binding.tilNumero.setError(null);
        }

        // Validar colonia
        if (TextUtils.isEmpty(binding.etColonia.getText())) {
            binding.tilColonia.setError("La colonia es requerida");
            isValid = false;
        } else {
            binding.tilColonia.setError(null);
        }

        // Validar ciudad
        if (TextUtils.isEmpty(binding.etCiudad.getText())) {
            binding.tilCiudad.setError("La ciudad es requerida");
            isValid = false;
        } else {
            binding.tilCiudad.setError(null);
        }

        return isValid;
    }

    /**
     * Guarda la dirección en la API.
     */
    private void saveAddress() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGuardarDireccion.setEnabled(false);

        // Construir la dirección completa
        String direccionCompleta = binding.etCalle.getText().toString().trim() + " #" +
                binding.etNumero.getText().toString().trim() + ", " +
                binding.etColonia.getText().toString().trim() + ", " +
                binding.etCiudad.getText().toString().trim();

        // Crear objeto de solicitud
        UbicacionRequest ubicacionRequest = new UbicacionRequest(
                latitude, // Latitud
                longitude, // Longitud
                direccionCompleta, // Dirección completa
                binding.etCalle.getText().toString().trim(), // Calle
                binding.etNumero.getText().toString().trim(), // Numero
                binding.etColonia.getText().toString().trim(), // Colonia
                binding.etCiudad.getText().toString().trim(), // Ciudad
                binding.etCodigoPostal.getText().toString().trim(), // Código Postal
                binding.etReferencias.getText().toString().trim(), // Referencias
                binding.etEtiqueta.getText().toString().trim(), // Etiqueta (Casa, Trabajo, etc.)
                binding.switchPrincipal.isChecked() // Es principal
        );

        // Guardar la ubicación
        viewModel.createUbicacion(ubicacionRequest).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnGuardarDireccion.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(this, "Dirección guardada con éxito", Toast.LENGTH_SHORT).show();

                // Si es primer inicio de sesión, ir a MainActivity
                if (isFirstLogin) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                finish();
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}