package com.example.porvenirsteaks.ui.ubicaciones;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.databinding.FragmentAgregarUbicacionBinding;
import com.example.porvenirsteaks.utils.LocationPermissionHandler;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.porvenirsteaks.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.location.Address;

public class AgregarUbicacionFragment extends BottomSheetDialogFragment {
    private static final String TAG = "AgregarUbicacionFrag";
    private FragmentAgregarUbicacionBinding binding;
    private UbicacionesViewModel viewModel;
    private Ubicacion ubicacionEditar;
    private static final String ARG_UBICACION = "ubicacion";

    // Para obtener ubicación actual
    private LocationPermissionHandler permissionHandler;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    public static AgregarUbicacionFragment newInstance(Ubicacion ubicacion) {
        AgregarUbicacionFragment fragment = new AgregarUbicacionFragment();
        Bundle args = new Bundle();
        if (ubicacion != null) {
            args.putInt("id", ubicacion.getId());
            args.putString("etiqueta", ubicacion.getEtiqueta());
            args.putString("direccion_completa", ubicacion.getDireccionCompleta());
            args.putString("calle", ubicacion.getCalle());
            args.putString("numero", ubicacion.getNumero());
            args.putString("colonia", ubicacion.getColonia());
            args.putString("ciudad", ubicacion.getCiudad());
            args.putString("codigo_postal", ubicacion.getCodigoPostal());
            args.putString("referencias", ubicacion.getReferencias());
            args.putBoolean("es_principal", ubicacion.isEsPrincipal());
            args.putDouble("latitud", ubicacion.getLatitud());
            args.putDouble("longitud", ubicacion.getLongitud());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Usamos una forma más simple de verificar permisos sin el handler
        // permissionHandler = new LocationPermissionHandler(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAgregarUbicacionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UbicacionesViewModel.class);

        // Configurar el título y cargar datos si es edición
        if (getArguments() != null && getArguments().containsKey("id")) {
            cargarDatosUbicacion();
            binding.tvTitleUbicacion.setText("Editar ubicación");
            binding.btnGuardar.setText("Actualizar");

            // Si estamos editando, tomar las coordenadas guardadas como actuales
            currentLatitude = ubicacionEditar.getLatitud();
            currentLongitude = ubicacionEditar.getLongitud();
        } else {
            binding.tvTitleUbicacion.setText("Agregar ubicación");
            binding.btnGuardar.setText("Guardar");

            // Si estamos agregando nueva ubicación, obtener coordenadas actuales
            obtenerUbicacionActual();
        }

        // Configurar botones
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnGuardar.setOnClickListener(v -> {
            if (validarFormulario()) {
                guardarUbicacion();
            }
        });

        // Configurar el botón de seleccionar ubicación en mapa
        binding.btnSelectOnMap.setOnClickListener(v -> {
            abrirSeleccionMapa();
        });
    }

    private void obtenerUbicacionActual() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Verificar permisos de manera más directa
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            // Sin permisos
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show();

            // Solicitar los permisos directamente desde el fragmento
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);

            return;
        }

        // Con permisos, obtener ubicación
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Toast.makeText(requireContext(), "Ubicación actual obtenida", Toast.LENGTH_SHORT).show();

                        geocodificarCoordenadas(currentLatitude, currentLongitude);
                    } else {
                        Toast.makeText(requireContext(),
                                "No se pudo obtener la ubicación actual. Intenta seleccionar en el mapa.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error al obtener ubicación: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                obtenerUbicacionActual();
            } else {
                Toast.makeText(requireContext(),
                        "Necesitas conceder permisos de ubicación para esta funcionalidad",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void obtenerCoordenadas() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
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
                .addOnSuccessListener(location -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (location != null) {
                        // Guardar la ubicación
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        Log.d(TAG, "Ubicación obtenida: " + currentLatitude + ", " + currentLongitude);

                        // Mostrar coordenadas en algún lugar (opcional)
                        Toast.makeText(requireContext(), "Coordenadas actualizadas", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener ubicación actual",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error obteniendo ubicación", e);
                    Toast.makeText(requireContext(), "Error al obtener ubicación: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirSeleccionMapa() {
        // Crear intent para abrir MapaUbicacionActivity
        Intent intent = new Intent(requireContext(), MapaUbicacionActivity.class);

        // Pasar coordenadas actuales si existen
        if (currentLatitude != 0 || currentLongitude != 0) {
            intent.putExtra("latitude", currentLatitude);
            intent.putExtra("longitude", currentLongitude);
        }

        // Iniciar la actividad esperando resultado
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Procesar resultado de selección de mapa
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            // Actualizar coordenadas si se devuelven del mapa
            if (data.hasExtra("latitude") && data.hasExtra("longitude")) {
                currentLatitude = data.getDoubleExtra("latitude", 0);
                currentLongitude = data.getDoubleExtra("longitude", 0);

                Toast.makeText(requireContext(), "Ubicación seleccionada en mapa", Toast.LENGTH_SHORT).show();

                geocodificarCoordenadas(currentLatitude, currentLongitude);
            }
        }
    }

    private void cargarDatosUbicacion() {
        Bundle args = getArguments();
        if (args != null) {
            ubicacionEditar = new Ubicacion();
            ubicacionEditar.setId(args.getInt("id"));
            ubicacionEditar.setEtiqueta(args.getString("etiqueta", ""));
            ubicacionEditar.setDireccionCompleta(args.getString("direccion_completa", ""));
            ubicacionEditar.setCalle(args.getString("calle", ""));
            ubicacionEditar.setNumero(args.getString("numero", ""));
            ubicacionEditar.setColonia(args.getString("colonia", ""));
            ubicacionEditar.setCiudad(args.getString("ciudad", ""));
            ubicacionEditar.setCodigoPostal(args.getString("codigo_postal", ""));
            ubicacionEditar.setReferencias(args.getString("referencias", ""));
            ubicacionEditar.setEsPrincipal(args.getBoolean("es_principal", false));
            ubicacionEditar.setLatitud(args.getDouble("latitud", 0));
            ubicacionEditar.setLongitud(args.getDouble("longitud", 0));

            // Cargar datos en la UI
            binding.etEtiqueta.setText(ubicacionEditar.getEtiqueta());
            binding.etCalle.setText(ubicacionEditar.getCalle());
            binding.etNumero.setText(ubicacionEditar.getNumero());
            binding.etColonia.setText(ubicacionEditar.getColonia());
            binding.etCiudad.setText(ubicacionEditar.getCiudad());
            binding.etCodigoPostal.setText(ubicacionEditar.getCodigoPostal());
            binding.etReferencias.setText(ubicacionEditar.getReferencias());
            binding.switchPrincipal.setChecked(ubicacionEditar.isEsPrincipal());
        }
    }

    private boolean validarFormulario() {
        boolean valido = true;

        // Validar etiqueta
        if (TextUtils.isEmpty(binding.etEtiqueta.getText())) {
            binding.tilEtiqueta.setError("La etiqueta es requerida");
            valido = false;
        } else {
            binding.tilEtiqueta.setError(null);
        }

        // Validar calle
        if (TextUtils.isEmpty(binding.etCalle.getText())) {
            binding.tilCalle.setError("La calle es requerida");
            valido = false;
        } else {
            binding.tilCalle.setError(null);
        }

        // Validar número
        if (TextUtils.isEmpty(binding.etNumero.getText())) {
            binding.tilNumero.setError("El número es requerido");
            valido = false;
        } else {
            binding.tilNumero.setError(null);
        }

        // Validar colonia
        if (TextUtils.isEmpty(binding.etColonia.getText())) {
            binding.tilColonia.setError("La colonia es requerida");
            valido = false;
        } else {
            binding.tilColonia.setError(null);
        }

        // Validar ciudad
        if (TextUtils.isEmpty(binding.etCiudad.getText())) {
            binding.tilCiudad.setError("La ciudad es requerida");
            valido = false;
        } else {
            binding.tilCiudad.setError(null);
        }

        // Validar si tenemos coordenadas
        if (currentLatitude == 0 && currentLongitude == 0) {
            Toast.makeText(requireContext(), "No se han obtenido coordenadas de ubicación",
                    Toast.LENGTH_SHORT).show();
            valido = false;
        }

        return valido;
    }

    private void guardarUbicacion() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnGuardar.setEnabled(false);

        // Construir la dirección completa
        String direccionCompleta = binding.etCalle.getText().toString().trim() + " #" +
                binding.etNumero.getText().toString().trim() + ", " +
                binding.etColonia.getText().toString().trim() + ", " +
                binding.etCiudad.getText().toString().trim();

        // Crear la solicitud con coordenadas actuales
        UbicacionRequest request = new UbicacionRequest(
                currentLatitude, // Usar las coordenadas actuales
                currentLongitude, // Usar las coordenadas actuales
                direccionCompleta,
                binding.etCalle.getText().toString().trim(),
                binding.etNumero.getText().toString().trim(),
                binding.etColonia.getText().toString().trim(),
                binding.etCiudad.getText().toString().trim(),
                binding.etCodigoPostal.getText().toString().trim(),
                binding.etReferencias.getText().toString().trim(),
                binding.etEtiqueta.getText().toString().trim(),
                binding.switchPrincipal.isChecked()
        );

        // Si es edición, llamar al endpoint de actualización
        if (ubicacionEditar != null) {
            viewModel.updateUbicacion(ubicacionEditar.getId(), request).observe(getViewLifecycleOwner(), result -> {
                procesarResultado(result);
            });
        } else {
            // Si es nueva ubicación
            viewModel.createUbicacion(request).observe(getViewLifecycleOwner(), result -> {
                procesarResultado(result);
            });
        }
    }

    private void procesarResultado(Resource<Ubicacion> result) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnGuardar.setEnabled(true);

        if (result.status == Resource.Status.SUCCESS && result.data != null) {
            Toast.makeText(requireContext(),
                    ubicacionEditar != null ? "Ubicación actualizada" : "Ubicación guardada",
                    Toast.LENGTH_SHORT).show();

            // Recargar las ubicaciones en el fragmento principal
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof UbicacionesFragment) {
                ((UbicacionesFragment) parentFragment).cargarUbicaciones();
            }

            dismiss();
        } else if (result.status == Resource.Status.ERROR) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(result.message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void geocodificarCoordenadas(double latitud, double longitud) {
        binding.progressBar.setVisibility(View.VISIBLE);

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);

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

                // Crear sugerencia para etiqueta si está vacía
                if (TextUtils.isEmpty(binding.etEtiqueta.getText())) {
                    binding.etEtiqueta.setText("Casa");
                }

                Toast.makeText(requireContext(), "Dirección obtenida con éxito", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener la dirección para estas coordenadas.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error en geocodificación", e);
            Toast.makeText(requireContext(), "Error al obtener dirección: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
        binding = null;
    }
}