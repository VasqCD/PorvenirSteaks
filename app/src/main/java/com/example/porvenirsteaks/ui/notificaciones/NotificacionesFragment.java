package com.example.porvenirsteaks.ui.notificaciones;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.databinding.FragmentNotificacionesBinding;
import com.example.porvenirsteaks.ui.notificaciones.adapters.NotificacionesAdapter;
import com.example.porvenirsteaks.utils.Resource;

public class NotificacionesFragment extends Fragment {
    private FragmentNotificacionesBinding binding;
    private NotificacionesViewModel viewModel;
    private NotificacionesAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);

        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new NotificacionesAdapter(notificacion -> {
            viewModel.marcarComoLeida(notificacion.getId()).observe(getViewLifecycleOwner(), result -> {
                if (result.status == Resource.Status.SUCCESS) {
                    // Recargar notificaciones
                    viewModel.getNotificaciones();
                }
            });
        });

        binding.recyclerViewNotificaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewNotificaciones.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnMarcarTodasLeidas.setOnClickListener(v -> {
            viewModel.marcarTodasComoLeidas().observe(getViewLifecycleOwner(), result -> {
                if (result.status == Resource.Status.SUCCESS && result.data) {
                    Toast.makeText(requireContext(), "Todas las notificaciones han sido marcadas como leídas", Toast.LENGTH_SHORT).show();
                    viewModel.getNotificaciones();
                } else if (result.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void observeViewModel() {
        viewModel.getNotificaciones().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            Log.d("NotificacionesFragment", "Estado: " + result.status +
                    ", Datos: " + (result.data != null ? result.data.size() : "null") +
                    ", Mensaje: " + result.message);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                adapter.submitList(result.data);

                // Mostrar mensaje de vacío si no hay notificaciones
                if (result.data.isEmpty()) {
                    Log.d("NotificacionesFragment", "No hay notificaciones para mostrar");
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewNotificaciones.setVisibility(View.GONE);
                    binding.btnMarcarTodasLeidas.setVisibility(View.GONE);
                } else {
                    Log.d("NotificacionesFragment", "Mostrando " + result.data.size() + " notificaciones");
                    binding.tvEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewNotificaciones.setVisibility(View.VISIBLE);
                    binding.btnMarcarTodasLeidas.setVisibility(View.VISIBLE);
                }
            } else if (result.status == Resource.Status.ERROR) {
                Log.e("NotificacionesFragment", "Error al cargar notificaciones: " + result.message);
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewNotificaciones.setVisibility(View.GONE);
                binding.btnMarcarTodasLeidas.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}