package com.example.porvenirsteaks.ui.ubicaciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.databinding.FragmentUbicacionesBinding;
import com.example.porvenirsteaks.ui.ubicaciones.adapters.UbicacionesAdapter;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UbicacionesFragment extends Fragment {
    private FragmentUbicacionesBinding binding;
    private UbicacionesViewModel viewModel;
    private UbicacionesAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUbicacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UbicacionesViewModel.class);

        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new UbicacionesAdapter(
                ubicacion -> {
                    // Edit ubicación
                    AgregarUbicacionFragment fragment = AgregarUbicacionFragment.newInstance(ubicacion);
                    fragment.show(getChildFragmentManager(), "edit_ubicacion");
                },
                ubicacion -> {
                    // Confirm delete
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Eliminar ubicación")
                            .setMessage("¿Está seguro que desea eliminar esta ubicación?")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                viewModel.deleteUbicacion(ubicacion.getId()).observe(getViewLifecycleOwner(), result -> {
                                    if (result.status == Resource.Status.SUCCESS && result.data) {
                                        Toast.makeText(requireContext(), "Ubicación eliminada", Toast.LENGTH_SHORT).show();
                                        // Recargar ubicaciones
                                        viewModel.getUbicaciones();
                                    } else if (result.status == Resource.Status.ERROR) {
                                        Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
        );

        binding.recyclerViewUbicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewUbicaciones.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.fabAgregarUbicacion.setOnClickListener(v -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}