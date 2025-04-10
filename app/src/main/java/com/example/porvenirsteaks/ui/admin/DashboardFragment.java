package com.example.porvenirsteaks.ui.admin;

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

import com.example.porvenirsteaks.databinding.FragmentDashboardBinding;
import com.example.porvenirsteaks.ui.admin.adapters.RepartidorAdapter;
import com.example.porvenirsteaks.ui.pedidos.adapters.PedidosAdapter;
import com.example.porvenirsteaks.utils.Resource;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private PedidosAdapter pedidosAdapter;
    private RepartidorAdapter repartidorAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerViews();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView para pedidos recientes
        pedidosAdapter = new PedidosAdapter(pedido -> {
            // Navegar al detalle del pedido
            // Aquí puedes usar Navigation para navegar al detalle
        });
        binding.recyclerViewPedidosRecientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPedidosRecientes.setAdapter(pedidosAdapter);

        // Configurar RecyclerView para repartidores disponibles
        repartidorAdapter = new RepartidorAdapter((repartidor, pedidoId) -> {
            // Asignar pedido al repartidor
            if (pedidoId != null) {
                viewModel.asignarPedidoARepartidor(pedidoId, repartidor.getId()).observe(getViewLifecycleOwner(), result -> {
                    if (result.status == Resource.Status.SUCCESS) {
                        Toast.makeText(requireContext(), "Pedido asignado con éxito", Toast.LENGTH_SHORT).show();
                        // Recargar datos
                        cargarDatos();
                    } else if (result.status == Resource.Status.ERROR) {
                        Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Seleccione un pedido para asignar", Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerViewRepartidoresDisponibles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewRepartidoresDisponibles.setAdapter(repartidorAdapter);
    }

    private void setupButtons() {
        binding.btnAgregarProducto.setOnClickListener(v -> {
            // Navegar a la pantalla de agregar producto
            // Implementa un Dialog o Fragment para esta funcionalidad
            Toast.makeText(requireContext(), "Funcionalidad no implementada", Toast.LENGTH_SHORT).show();
        });

        binding.btnAgregarRepartidor.setOnClickListener(v -> {
            // Navegar a la pantalla de agregar repartidor
            // Implementa un Dialog o Fragment para esta funcionalidad
            Toast.makeText(requireContext(), "Funcionalidad no implementada", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        // Observar contadores
        viewModel.getPedidosPendientesCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvPedidosPendientesCount.setText(String.valueOf(count));
        });

        viewModel.getPedidosHoyCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvPedidosHoyCount.setText(String.valueOf(count));
        });

        // Observar pedidos recientes
        viewModel.getPedidosRecientes().observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                pedidosAdapter.submitList(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observar repartidores disponibles
        viewModel.getRepartidoresDisponibles().observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                repartidorAdapter.submitList(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatos() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Recargar todos los datos
        viewModel.getPedidosPendientes();
        viewModel.getPedidosRecientes();
        viewModel.getRepartidoresDisponibles();

        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cargar datos cada vez que el fragmento se muestra
        cargarDatos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}