package com.example.porvenirsteaks.ui.repartidor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.databinding.FragmentEntregasBinding;
import com.example.porvenirsteaks.utils.Resource;

public class EntregasFragment extends Fragment {
    private FragmentEntregasBinding binding;
    private EntregasViewModel viewModel;
    private EntregasAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntregasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EntregasViewModel.class);

        setupRecyclerView();
        observeViewModel();

        // Iniciar la ubicación del repartidor
        viewModel.iniciarActualizacionUbicacion(requireContext());

        // Asegurar que se carguen los datos inicialmente
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.cargarEntregas();
    }

    private void setupRecyclerView() {
        adapter = new EntregasAdapter(pedido -> {
            // Navegar al detalle de la entrega
            Bundle args = new Bundle();
            args.putInt("pedido_id", pedido.getId());

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_nav_repartidor_entregas_to_detalleEntregaFragment, args);
        });

        binding.recyclerViewEntregas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEntregas.setAdapter(adapter);

        // Configurar swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.cargarEntregas();
        });
    }

    private void observeViewModel() {
        viewModel.getEntregas().observe(getViewLifecycleOwner(), result -> {
            binding.swipeRefreshLayout.setRefreshing(false);

            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                adapter.submitList(result.data);

                // Mostrar mensaje de vacío si no hay entregas
                binding.tvEmptyState.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                binding.lottieEmptyState.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                binding.recyclerViewEntregas.setVisibility(result.data.isEmpty() ? View.GONE : View.VISIBLE);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.lottieEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewEntregas.setVisibility(View.GONE);

                // Mostrar mensaje de error específico
                binding.tvEmptyState.setText("No se pudieron cargar las entregas: " + result.message);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar entregas al volver al fragmento
        viewModel.cargarEntregas();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Parar actualizaciones de ubicación cuando no está visible
        viewModel.detenerActualizacionUbicacion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}