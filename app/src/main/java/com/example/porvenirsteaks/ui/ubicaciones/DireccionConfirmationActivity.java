package com.example.porvenirsteaks.ui.ubicaciones;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.porvenirsteaks.MainActivity;
import com.example.porvenirsteaks.databinding.ActivityDireccionConfirmationBinding;
import com.example.porvenirsteaks.utils.LocationPermissionHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Actividad para confirmar la ubicación actual del usuario.
 * Se muestra cuando un usuario inicia sesión y no tiene direcciones registradas.
 */
public class DireccionConfirmationActivity extends AppCompatActivity {
    private static final String TAG = "DireccionConfirmation";

    private ActivityDireccionConfirmationBinding binding;
    private LocationPermissionHandler permissionHandler;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private boolean isFirstLogin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDireccionConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar manejador de permisos
        permissionHandler = new LocationPermissionHandler(this);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener extra para determinar si es primer inicio de sesión
        isFirstLogin = getIntent().getBooleanExtra("isFirstLogin", false);

        setupUI();
        checkLocationPermission();
    }

    private void setupUI() {
        // Configurar botón para confirmar ubicación
        binding.btnConfirmar.setOnClickListener(v -> {
            if (currentLatitude != 0 && currentLongitude != 0) {
                // Si tenemos una ubicación, navegamos a la pantalla para completar dirección
                navigateToCompleteAddress();
            } else {
                // Si no tenemos ubicación, mostramos mensaje de error
                Toast.makeText(this, "No se ha podido obtener tu ubicación. Intenta de nuevo.",
                        Toast.LENGTH_SHORT).show();
                getLocation();
            }
        });

        // Configurar botón para usar otra dirección
        binding.btnEnOtroMomento.setOnClickListener(v -> {
            if (isFirstLogin) {
                // Si es primer inicio de sesión, ir directamente a agregar ubicación
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // En otro caso, simplemente cerramos esta actividad
                finish();
            }
        });
    }

    private void checkLocationPermission() {
        binding.progressBar.setVisibility(View.VISIBLE);
        permissionHandler.checkLocationPermission(granted -> {
            if (granted) {
                // Si tenemos permisos, obtenemos la ubicación
                getLocation();
            } else {
                // Si no tenemos permisos, mostramos mensaje
                binding.progressBar.setVisibility(View.GONE);
                binding.tvDireccionActual.setText("No se pudo acceder a tu ubicación. " +
                        "Por favor, concede los permisos necesarios.");
            }
        });
    }

    private void getLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        // Cancelar cualquier solicitud pendiente
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }

        cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (location != null) {
                        // Guardar la ubicación
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        // Mostrar la dirección (por ahora solo coordenadas)
                        binding.tvDireccionActual.setText("Ubicación actual encontrada.\n" +
                                "Latitud: " + currentLatitude + "\n" +
                                "Longitud: " + currentLongitude);

                        // Habilitar botón de confirmar
                        binding.btnConfirmar.setEnabled(true);

                        // Opcionalmente, obtener la dirección real mediante geocodificación inversa
                        // Para simplicidad, esto no está implementado aquí
                    } else {
                        binding.tvDireccionActual.setText("No se pudo obtener tu ubicación. " +
                                "Revisa que los servicios de ubicación estén activados.");
                        binding.btnConfirmar.setEnabled(false);
                    }
                })
                .addOnFailureListener(this, e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error obteniendo ubicación", e);
                    binding.tvDireccionActual.setText("Error al obtener ubicación: " +
                            e.getMessage());
                    binding.btnConfirmar.setEnabled(false);
                });
    }

    private void navigateToCompleteAddress() {
        Intent intent = new Intent(this, CompleteAddressActivity.class);
        intent.putExtra("latitude", currentLatitude);
        intent.putExtra("longitude", currentLongitude);
        intent.putExtra("isFirstLogin", isFirstLogin);
        startActivity(intent);

        // No hacemos finish() aquí para que el usuario pueda retroceder si lo desea
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHandler.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }
}