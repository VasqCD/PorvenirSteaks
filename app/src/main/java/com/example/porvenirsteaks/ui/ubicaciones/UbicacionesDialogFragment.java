package com.example.porvenirsteaks.ui.ubicaciones;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.DialogUbicacionesBinding;
import com.example.porvenirsteaks.ui.ubicaciones.adapters.UbicacionSeleccionAdapter;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UbicacionesDialogFragment extends DialogFragment {
    private DialogUbicacionesBinding binding;
    private UbicacionesViewModel viewModel;
    private UbicacionSeleccionAdapter adapter;

    public interface UbicacionSeleccionadaListener {
        void onUbicacionSeleccionada(Ubicacion ubicacion);
    }

    private UbicacionSeleccionadaListener listener;

    public void setUbicacionSeleccionadaListener(UbicacionSeleccionadaListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogUbicacionesBinding.inflate(getLayoutInflater());
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar ubicación")
                .setView(binding.getRoot())
                .setPositiveButton("Cancelar", (dialog, which) -> dismiss())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UbicacionesViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new UbicacionSeleccionAdapter(ubicacion -> {
            if (listener != null) {
                listener.onUbicacionSeleccionada(ubicacion);
                dismiss();
            }
        });

        binding.recyclerViewUbicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewUbicaciones.setAdapter(adapter);

        binding.btnAgregarUbicacion.setOnClickListener(v -> {
            AgregarUbicacionFragment fragment = AgregarUbicacionFragment.newInstance(null);
            fragment.show(getChildFragmentManager(), "agregar_ubicacion");
        });
    }

    private void observeViewModel() {
        viewModel.getUbicaciones().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                adapter.submitList(result.data);

                // Mostrar mensaje de vacío si no hay ubicaciones
                if (result.data.isEmpty()) {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewUbicaciones.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewUbicaciones.setVisibility(View.VISIBLE);
                }
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewUbicaciones.setVisibility(View.GONE);
            }
        });
    }
}