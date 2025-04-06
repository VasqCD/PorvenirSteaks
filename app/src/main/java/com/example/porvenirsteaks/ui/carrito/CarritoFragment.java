package com.example.porvenirsteaks.ui.carrito;

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

import com.example.porvenirsteaks.data.model.CartItem;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.FragmentCarritoBinding;
import com.example.porvenirsteaks.ui.carrito.adapters.CarritoAdapter;
import com.example.porvenirsteaks.ui.ubicaciones.UbicacionesDialogFragment;
import com.example.porvenirsteaks.utils.Resource;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CarritoFragment extends Fragment implements UbicacionesDialogFragment.UbicacionSeleccionadaListener {
    private FragmentCarritoBinding binding;
    private CarritoViewModel viewModel;
    private CarritoAdapter adapter;
    private NumberFormat currencyFormatter;

    private Ubicacion ubicacionSeleccionada;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarritoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CarritoViewModel.class);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));

        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new CarritoAdapter(
                new CarritoAdapter.OnItemUpdateListener() {
                    @Override
                    public void onQuantityChanged(int productoId, int newCantidad) {
                        viewModel.updateCantidad(productoId, newCantidad);
                    }

                    @Override
                    public void onItemRemoved(int productoId) {
                        viewModel.removeFromCart(productoId);
                    }
                },
                currencyFormatter
        );

        binding.recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCarrito.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnSeleccionarUbicacion.setOnClickListener(v -> {
            UbicacionesDialogFragment dialog = new UbicacionesDialogFragment();
            dialog.setUbicacionSeleccionadaListener(this);
            dialog.show(getChildFragmentManager(), "ubicaciones_dialog");
        });

        binding.btnRealizarPedido.setOnClickListener(v -> {
            if (ubicacionSeleccionada == null) {
                Toast.makeText(requireContext(), "Debe seleccionar una dirección de entrega", Toast.LENGTH_SHORT).show();
                return;
            }

            realizarPedido();
        });
    }

    private void observeViewModel() {
        viewModel.getCarritoItems().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCartContent.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCartContent.setVisibility(View.VISIBLE);

            adapter.submitList(items);

            // Actualizar total
            double total = viewModel.getTotal();
            binding.tvTotal.setText(currencyFormatter.format(total));
        }
    }

    private void realizarPedido() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRealizarPedido.setEnabled(false);

        viewModel.realizarPedido(ubicacionSeleccionada.getId()).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRealizarPedido.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "¡Pedido realizado con éxito!", Toast.LENGTH_LONG).show();

                // Navegar al detalle del pedido
                // navController.navigate(R.id.action_carritoFragment_to_detallePedidoFragment, args);

            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onUbicacionSeleccionada(Ubicacion ubicacion) {
        this.ubicacionSeleccionada = ubicacion;
        binding.tvUbicacionSeleccionada.setText(ubicacion.getDireccionCompleta());
        binding.tvUbicacionSeleccionada.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}