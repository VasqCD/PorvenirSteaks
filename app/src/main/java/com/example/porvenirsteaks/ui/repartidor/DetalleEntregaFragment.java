package com.example.porvenirsteaks.ui.repartidor;

import android.content.Intent;
import android.net.Uri;
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
import com.example.porvenirsteaks.databinding.FragmentDetalleEntregaBinding;
import com.example.porvenirsteaks.ui.pedidos.adapters.DetallePedidoAdapter;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleEntregaFragment extends Fragment {
    private FragmentDetalleEntregaBinding binding;
    private EntregasViewModel viewModel;
    private DetallePedidoAdapter adapter;
    private int pedidoId;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleEntregaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        viewModel = new ViewModelProvider(this).get(EntregasViewModel.class);

        // Obtener pedidoId de los argumentos
        if (getArguments() != null) {
            pedidoId = getArguments().getInt("pedido_id", 0);
            if (pedidoId == 0) {
                Toast.makeText(requireContext(), "Error: ID de pedido no válido", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigateUp();
                return;
            }
        }

        setupRecyclerView();
        setupButtons();
        cargarPedido();
    }

    private void setupRecyclerView() {
        adapter = new DetallePedidoAdapter(currencyFormatter);
        binding.recyclerViewDetalles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewDetalles.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigateUp();
        });

        binding.btnEntregado.setOnClickListener(v -> {
            confirmarEntrega();
        });

        binding.btnLlamarCliente.setOnClickListener(v -> {
            if (viewModel.getPedidoActual() != null && viewModel.getPedidoActual().getUsuario() != null
                    && viewModel.getPedidoActual().getUsuario().getTelefono() != null) {
                String telefono = viewModel.getPedidoActual().getUsuario().getTelefono();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + telefono));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No hay número de teléfono disponible", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnVerMapa.setOnClickListener(v -> {
            if (viewModel.getPedidoActual() != null && viewModel.getPedidoActual().getUbicacion() != null) {
                double latitud = viewModel.getPedidoActual().getUbicacion().getLatitud();
                double longitud = viewModel.getPedidoActual().getUbicacion().getLongitud();

                // Abrir Google Maps con la ubicación
                Uri gmmIntentUri = Uri.parse("geo:" + latitud + "," + longitud + "?q=" + latitud + "," + longitud);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Fallback a navegador si no hay app de mapas
                    Uri webIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitud + "," + longitud);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webIntentUri);
                    startActivity(webIntent);
                }
            } else {
                Toast.makeText(requireContext(), "No hay ubicación disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPedido() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutContent.setVisibility(View.GONE);

        viewModel.getPedidoById(pedidoId).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                viewModel.setPedidoActual(result.data);
                actualizarUI(result.data);
                binding.layoutContent.setVisibility(View.VISIBLE);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(com.example.porvenirsteaks.data.model.Pedido pedido) {
        binding.tvPedidoId.setText("Pedido #" + pedido.getId());
        binding.tvTotal.setText(currencyFormatter.format(pedido.getTotal()));

        // Estado
        binding.tvEstado.setText(getEstadoFormateado(pedido.getEstado()));

        // Fecha
        try {
            Date date = apiDateFormat.parse(pedido.getFechaPedido());
            binding.tvFechaPedido.setText(displayDateFormat.format(date));
        } catch (ParseException e) {
            binding.tvFechaPedido.setText(pedido.getFechaPedido());
        }

        // Datos cliente
        if (pedido.getUsuario() != null) {
            binding.tvClienteNombre.setText(pedido.getUsuario().getName() + " " +
                    (pedido.getUsuario().getApellido() != null ? pedido.getUsuario().getApellido() : ""));
            binding.tvClienteTelefono.setText(pedido.getUsuario().getTelefono());
        }

        // Ubicación
        if (pedido.getUbicacion() != null) {
            binding.tvDireccion.setText(pedido.getUbicacion().getDireccionCompleta());

            if (pedido.getUbicacion().getReferencias() != null && !pedido.getUbicacion().getReferencias().isEmpty()) {
                binding.tvReferencias.setText(pedido.getUbicacion().getReferencias());
                binding.tvReferencias.setVisibility(View.VISIBLE);
                binding.tvReferenciasLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvReferencias.setVisibility(View.GONE);
                binding.tvReferenciasLabel.setVisibility(View.GONE);
            }
        }

        // Detalles del pedido
        if (pedido.getDetalles() != null) {
            adapter.submitList(pedido.getDetalles());
        }

        // Controlar visibilidad del botón de entrega según estado
        binding.btnEntregado.setVisibility(
                pedido.getEstado().equals(Constants.ESTADO_EN_CAMINO) ? View.VISIBLE : View.GONE);
    }

    private String getEstadoFormateado(String estado) {
        switch (estado) {
            case Constants.ESTADO_PENDIENTE: return "Pendiente";
            case Constants.ESTADO_EN_COCINA: return "En cocina";
            case Constants.ESTADO_EN_CAMINO: return "En camino";
            case Constants.ESTADO_ENTREGADO: return "Entregado";
            case Constants.ESTADO_CANCELADO: return "Cancelado";
            default: return estado;
        }
    }

    private void confirmarEntrega() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar entrega")
                .setMessage("¿Está seguro que desea marcar este pedido como entregado?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    actualizarEstadoPedido(Constants.ESTADO_ENTREGADO);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarEstadoPedido(String nuevoEstado) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnEntregado.setEnabled(false);

        viewModel.actualizarEstadoPedido(pedidoId, nuevoEstado).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnEntregado.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                Toast.makeText(requireContext(), "Pedido marcado como entregado", Toast.LENGTH_SHORT).show();
                viewModel.setPedidoActual(result.data);
                actualizarUI(result.data);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}