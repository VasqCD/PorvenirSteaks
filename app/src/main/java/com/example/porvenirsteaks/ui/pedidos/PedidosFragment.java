package com.example.porvenirsteaks.ui.pedidos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.databinding.FragmentPedidosBinding;
import com.example.porvenirsteaks.ui.pedidos.adapters.PedidosAdapter;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class PedidosFragment extends Fragment {
    private FragmentPedidosBinding binding;
    private PedidosViewModel viewModel;
    private PedidosAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PedidosViewModel.class);

        setupRecyclerView();
        setupSpinner();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new PedidosAdapter(pedido -> {
            // Navegar al detalle del pedido
            Bundle args = new Bundle();
            args.putInt("pedido_id", pedido.getId());

            // Obtener NavController desde la vista actual
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.detallePedidoFragment, args);
        });

        binding.recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPedidos.setAdapter(adapter);
    }

    private void setupSpinner() {
        List<String> estados = new ArrayList<>();
        estados.add("Todos");
        estados.add("Pendiente");
        estados.add("En cocina");
        estados.add("En camino");
        estados.add("Entregado");
        estados.add("Cancelado");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, estados);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFiltro.setAdapter(spinnerAdapter);

        binding.spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setFiltroEstado(null);
                } else {
                    String estado = estados.get(position).toLowerCase().replace(" ", "_");
                    viewModel.setFiltroEstado(estado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setFiltroEstado(null);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getPedidos().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(
                    result.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                List<Pedido> filteredList = applyFilter(result.data);
                adapter.submitList(filteredList);

                updateEmptyState(filteredList.isEmpty());
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });

        viewModel.getFiltroEstado().observe(getViewLifecycleOwner(), filtro -> {
            if (adapter.getCurrentList() != null) {
                List<Pedido> filteredList = applyFilter(adapter.getCurrentList());
                adapter.submitList(filteredList);
                updateEmptyState(filteredList.isEmpty());
            }
        });
    }

    private List<Pedido> applyFilter(List<Pedido> pedidos) {
        String filtro = viewModel.getFiltroEstado().getValue();
        if (filtro == null || filtro.isEmpty()) {
            return pedidos;
        }

        List<Pedido> filtered = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado().equals(filtro)) {
                filtered.add(pedido);
            }
        }
        return filtered;
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewPedidos.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}