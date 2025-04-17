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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.CartItem;
import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.FragmentCarritoBinding;
import com.example.porvenirsteaks.ui.carrito.adapters.CarritoAdapter;
import com.example.porvenirsteaks.ui.ubicaciones.UbicacionesDialogFragment;
import com.example.porvenirsteaks.utils.Resource;

import java.text.NumberFormat;
import java.util.ArrayList;
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

        // Check for saved location
        loadUbicaciones();
    }

    private void loadUbicaciones() {
        viewModel.getUbicaciones().observe(getViewLifecycleOwner(), result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null && !result.data.isEmpty()) {
                // Display the first location as default if none is selected
                if (ubicacionSeleccionada == null && !result.data.isEmpty()) {
                    ubicacionSeleccionada = result.data.get(0);
                    onUbicacionSeleccionada(ubicacionSeleccionada);
                }
            }
        });
    }

    private void displayUbicacion(Ubicacion ubicacion) {
        if (ubicacion != null) {
            binding.tvUbicacionSeleccionada.setText(ubicacion.getDireccionCompleta());
            binding.tvUbicacionSeleccionada.setVisibility(View.VISIBLE);
        } else {
            binding.tvUbicacionSeleccionada.setText("No se ha seleccionado dirección");
        }
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

            adapter.submitList(new ArrayList<>(items));

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
                Bundle args = new Bundle();
                args.putInt("pedido_id", result.data.getId());

                // Obtener NavController desde la vista actual
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.detallePedidoFragment, args);

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

        // Optional: You can also update any visual indicators to show this is the selected location
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}