package com.example.porvenirsteaks.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Clase utilitaria para manejar los permisos de ubicación en la aplicación.
 */
public class LocationPermissionHandler {
    private static final String TAG = "LocationPermissionHandler";

    // Constantes para los códigos de solicitud de permiso
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1003;
    public static final int REQUEST_LOCATION_SETTINGS = 2001;

    // Permisos de ubicación básicos
    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // Permiso de ubicación en segundo plano (solo para Android 10+)
    public static final String BACKGROUND_LOCATION_PERMISSION =
            Manifest.permission.ACCESS_BACKGROUND_LOCATION;

    // Permiso de notificaciones (solo para Android 13+)
    public static final String NOTIFICATION_PERMISSION =
            Manifest.permission.POST_NOTIFICATIONS;

    private final FragmentActivity activity;
    private Consumer<Boolean> permissionCallback;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> appSettingsLauncher;

    public LocationPermissionHandler(FragmentActivity activity) {
        this.activity = activity;

        // Inicializar launcher para solicitud de permisos
        this.requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult);

        // Inicializar launcher para configuración de la aplicación
        this.appSettingsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> checkLocationPermission(permissionCallback));
    }

    /**
     * Verifica y solicita los permisos de ubicación.
     * @param callback Callback que se ejecutará con el resultado (true si se otorgaron los permisos)
     */
    public void checkLocationPermission(Consumer<Boolean> callback) {
        this.permissionCallback = callback;

        // Verificar permisos de ubicación
        if (hasLocationPermission()) {
            // Verificar si la ubicación está habilitada
            if (isLocationEnabled()) {
                if (callback != null) {
                    callback.accept(true);
                }
            } else {
                // Mostrar diálogo para habilitar ubicación
                showLocationSettingsDialog();
            }
        } else {
            // Solicitar permisos de ubicación
            requestLocationPermissions();
        }
    }

    /**
     * Verifica si la aplicación tiene permisos de ubicación.
     */
    public boolean hasLocationPermission() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si la aplicación tiene permiso de ubicación en segundo plano.
     */
    public boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(activity, BACKGROUND_LOCATION_PERMISSION) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores a Android 10, no se requiere permiso específico
    }

    /**
     * Verifica si la aplicación tiene permiso de notificaciones.
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, NOTIFICATION_PERMISSION) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores a Android 13, no se requiere permiso específico
    }

    /**
     * Verifica si la ubicación está habilitada en el dispositivo.
     */
    public boolean isLocationEnabled() {
        LocationManager locationManager =
                (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    /**
     * Solicita los permisos de ubicación.
     */
    private void requestLocationPermissions() {
        // Usar el launcher para solicitar permisos
        requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
    }

    /**
     * Solicita el permiso de ubicación en segundo plano (solo para Android 10+).
     */
    public void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission()) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{BACKGROUND_LOCATION_PERMISSION},
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Solicita el permiso de notificaciones (solo para Android 13+).
     */
    public void requestNotificationPermission(Consumer<Boolean> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                this.permissionCallback = callback;

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{NOTIFICATION_PERMISSION},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else if (callback != null) {
                callback.accept(true);
            }
        } else if (callback != null) {
            callback.accept(true); // En versiones anteriores a Android 13, no se requiere permiso
        }
    }

    /**
     * Maneja el resultado de la solicitud de permisos.
     */
    private void handlePermissionResult(Map<String, Boolean> result) {
        boolean allGranted = true;

        for (String permission : LOCATION_PERMISSIONS) {
            if (!result.getOrDefault(permission, false)) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            // Verificar si la ubicación está habilitada
            if (isLocationEnabled()) {
                if (permissionCallback != null) {
                    permissionCallback.accept(true);
                }
            } else {
                showLocationSettingsDialog();
            }
        } else {
            if (shouldShowPermissionRationale()) {
                showPermissionRationaleDialog();
            } else {
                showPermissionDeniedDialog();
            }

            if (permissionCallback != null) {
                permissionCallback.accept(false);
            }
        }
    }

    /**
     * Verifica si se debe mostrar la explicación de por qué se necesita el permiso.
     */
    private boolean shouldShowPermissionRationale() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Muestra un diálogo explicando por qué se necesitan los permisos de ubicación.
     */
    private void showPermissionRationaleDialog() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Permiso de ubicación")
                .setMessage("Necesitamos acceso a tu ubicación para poder entregarte tus " +
                        "pedidos correctamente. Sin este permiso, no podrás utilizar " +
                        "algunas funciones de la aplicación.")
                .setPositiveButton("Solicitar de nuevo", (dialog, which) -> {
                    requestLocationPermissions();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (permissionCallback != null) {
                        permissionCallback.accept(false);
                    }
                })
                .show();
    }

    /**
     * Muestra un diálogo cuando el usuario ha denegado los permisos de ubicación permanentemente.
     */
    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Permiso denegado")
                .setMessage("Has denegado el permiso de ubicación. Para utilizar esta función, " +
                        "debes habilitar el permiso en la configuración de la aplicación.")
                .setPositiveButton("Ir a configuración", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (permissionCallback != null) {
                        permissionCallback.accept(false);
                    }
                })
                .show();
    }

    /**
     * Muestra un diálogo para solicitar al usuario que habilite la ubicación.
     */
    private void showLocationSettingsDialog() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Ubicación desactivada")
                .setMessage("La ubicación de tu dispositivo está desactivada. " +
                        "Para utilizar esta función, debes activar la ubicación.")
                .setPositiveButton("Activar ubicación", (dialog, which) -> {
                    openLocationSettings();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (permissionCallback != null) {
                        permissionCallback.accept(false);
                    }
                })
                .show();
    }

    /**
     * Abre la configuración de la aplicación.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        appSettingsLauncher.launch(intent);
    }

    /**
     * Abre la configuración de ubicación del dispositivo.
     */
    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, REQUEST_LOCATION_SETTINGS);
    }

    /**
     * Maneja el resultado cuando el usuario regresa de la configuración de ubicación.
     */
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            // Verificar si la ubicación está habilitada después de regresar de la configuración
            if (isLocationEnabled()) {
                if (permissionCallback != null) {
                    permissionCallback.accept(true);
                }
            } else {
                if (permissionCallback != null) {
                    permissionCallback.accept(false);
                }
            }
        }
    }

    /**
     * Solicita todos los permisos necesarios para la aplicación en un solo flujo.
     * @param callback Callback que se ejecutará con el resultado final
     */
    public void requestAllPermissions(Consumer<Boolean> finalCallback) {
        checkLocationPermission(locationGranted -> {
            if (locationGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermission();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermission(notificationGranted -> {
                        finalCallback.accept(notificationGranted);
                    });
                } else {
                    finalCallback.accept(true);
                }
            } else {
                finalCallback.accept(false);
            }
        });
    }

    /**
     * Procesa el resultado de la solicitud de permisos
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE ||
                requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {

            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (permissionCallback != null) {
                permissionCallback.accept(allGranted);
            }
        }
    }
}