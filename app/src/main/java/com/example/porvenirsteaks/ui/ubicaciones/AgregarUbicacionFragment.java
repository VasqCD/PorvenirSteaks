package com.example.porvenirsteaks.ui.ubicaciones;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.data.model.requests.UbicacionRequest;
import com.example.porvenirsteaks.databinding.FragmentAgregarUbicacionBinding;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AgregarUbicacionFragment extends DialogFragment {
    private FragmentAgregarUbicacionBinding binding;
    private UbicacionesViewModel viewModel;
    private Ubicacion ubicacionEditar;
    private static final String ARG_UBICACION = "ubicacion";

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
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
        } else {
            binding.tvTitleUbicacion.setText("Agregar ubicación");
            binding.btnGuardar.setText("Guardar");
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
            // Aquí podrías abrir un MapFragment o una activity para seleccionar ubicación
            Toast.makeText(requireContext(), "Funcionalidad de mapa no implementada", Toast.LENGTH_SHORT).show();
        });
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

        // Crear la solicitud
        UbicacionRequest request = new UbicacionRequest(
                // Por ahora usaremos coordenadas por defecto
                ubicacionEditar != null ? ubicacionEditar.getLatitud() : 0,
                ubicacionEditar != null ? ubicacionEditar.getLongitud() : 0,
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
            // Como no tenemos implementado el método de actualización en el ViewModel,
            // usaremos createUbicacion por ahora
            viewModel.createUbicacion(request).observe(getViewLifecycleOwner(), result -> {
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
            dismiss();

            // Recargar las ubicaciones en el fragmento principal
            if (getParentFragment() instanceof UbicacionesFragment) {
                ((UbicacionesFragment) getParentFragment()).cargarUbicaciones();
            }
        } else if (result.status == Resource.Status.ERROR) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(result.message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}