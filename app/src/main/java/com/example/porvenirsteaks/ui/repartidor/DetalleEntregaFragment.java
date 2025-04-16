package com.example.porvenirsteaks.ui.repartidor;

import android.content.Intent;
import android.net.Uri;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.databinding.FragmentDetalleEntregaBinding;
import com.example.porvenirsteaks.ui.pedidos.adapters.DetallePedidoAdapter;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.DateUtils;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleEntregaFragment extends Fragment {
    private static final String TAG = "DetalleEntregaFragment";
    private FragmentDetalleEntregaBinding binding;
    private EntregasViewModel viewModel;
    private DetallePedidoAdapter adapter;
    private int pedidoId;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;
    private boolean isLoading = false;

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
                mostrarError("Error: ID de pedido no válido");
                return;
            }
        } else {
            mostrarError("Error: No se proporcionó un ID de pedido");
            return;
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
            llamarCliente();
        });

        binding.btnVerMapa.setOnClickListener(v -> {
            abrirMapa();
        });
    }

    private void cargarPedido() {
        mostrarCargando(true);

        viewModel.getPedidoById(pedidoId).observe(getViewLifecycleOwner(), result -> {
            mostrarCargando(false);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                viewModel.setPedidoActual(result.data);
                actualizarUI(result.data);
                binding.layoutContent.setVisibility(View.VISIBLE);
                Log.d(TAG, "Pedido cargado correctamente: #" + result.data.getId());
            } else if (result.status == Resource.Status.ERROR) {
                mostrarError("Error al cargar el pedido: " + result.message);
            }
        });
    }

    private void actualizarUI(com.example.porvenirsteaks.data.model.Pedido pedido) {
        binding.tvPedidoId.setText("Pedido #" + pedido.getId());
        binding.tvTotal.setText(currencyFormatter.format(pedido.getTotal()));

        // Datos cliente con manejo seguro de nulos
        if (pedido.getUsuario() != null) {
            // Manejo seguro de nombre y apellido
            StringBuilder nombreCompleto = new StringBuilder();

            if (pedido.getUsuario().getName() != null && !pedido.getUsuario().getName().isEmpty()) {
                nombreCompleto.append(pedido.getUsuario().getName());
            }

            if (pedido.getUsuario().getApellido() != null && !pedido.getUsuario().getApellido().isEmpty()) {
                if (nombreCompleto.length() > 0) {
                    nombreCompleto.append(" ");
                }
                nombreCompleto.append(pedido.getUsuario().getApellido());
            }

            if (nombreCompleto.length() == 0) {
                nombreCompleto.append("Cliente #").append(pedido.getUsuarioId());
            }

            binding.tvClienteNombre.setText(nombreCompleto.toString());

            // Manejo seguro del teléfono
            String telefono = pedido.getUsuario().getTelefono();
            if (telefono != null && !telefono.isEmpty()) {
                binding.tvClienteTelefono.setText(telefono);
                binding.btnLlamarCliente.setEnabled(true);
            } else {
                binding.tvClienteTelefono.setText("Sin teléfono");
                binding.btnLlamarCliente.setEnabled(false);
            }
        } else {
            binding.tvClienteNombre.setText("Cliente no disponible");
            binding.tvClienteTelefono.setText("Sin teléfono");
            binding.btnLlamarCliente.setEnabled(false);
        }

        // Estado
        binding.tvEstado.setText(getEstadoFormateado(pedido.getEstado()));

        // Color según estado
        int colorRes;
        switch (pedido.getEstado()) {
            case Constants.ESTADO_PENDIENTE:
                colorRes = R.color.estado_pendiente;
                break;
            case Constants.ESTADO_EN_COCINA:
                colorRes = R.color.estado_en_cocina;
                break;
            case Constants.ESTADO_EN_CAMINO:
                colorRes = R.color.estado_en_camino;
                break;
            case Constants.ESTADO_ENTREGADO:
                colorRes = R.color.estado_entregado;
                break;
            case Constants.ESTADO_CANCELADO:
                colorRes = R.color.estado_cancelado;
                break;
            default:
                colorRes = R.color.estado_pendiente;
                break;
        }

        binding.tvEstado.setTextColor(getResources().getColor(colorRes, null));

        // Fecha usando utilidad DateUtils para mejor manejo
        String fechaFormateada = DateUtils.formatDateString(pedido.getFechaPedido(), "dd/MM/yyyy HH:mm");
        binding.tvFechaPedido.setText(fechaFormateada);

        // Datos cliente
        if (pedido.getUsuario() != null) {
            String nombreCompleto = pedido.getUsuario().getName();
            if (pedido.getUsuario().getApellido() != null && !pedido.getUsuario().getApellido().isEmpty()) {
                nombreCompleto += " " + pedido.getUsuario().getApellido();
            }
            binding.tvClienteNombre.setText(nombreCompleto);
            binding.tvClienteTelefono.setText(pedido.getUsuario().getTelefono());
        } else {
            binding.tvClienteNombre.setText("No disponible");
            binding.tvClienteTelefono.setText("No disponible");
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
        } else {
            binding.tvDireccion.setText("No disponible");
            binding.tvReferencias.setVisibility(View.GONE);
            binding.tvReferenciasLabel.setVisibility(View.GONE);
        }

        // Detalles del pedido
        if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            adapter.submitList(pedido.getDetalles());
        } else {
            // Mostrar mensaje si no hay detalles
            Toast.makeText(requireContext(), "No hay detalles disponibles para este pedido", Toast.LENGTH_SHORT).show();
        }

        // Controlar visibilidad del botón de entrega según estado
        binding.btnEntregado.setVisibility(
                pedido.getEstado().equals(Constants.ESTADO_EN_CAMINO) ? View.VISIBLE : View.GONE);
    }

    private String getEstadoFormateado(String estado) {
        switch (estado) {
            case Constants.ESTADO_PENDIENTE:
                return "Pendiente";
            case Constants.ESTADO_EN_COCINA:
                return "En cocina";
            case Constants.ESTADO_EN_CAMINO:
                return "En camino";
            case Constants.ESTADO_ENTREGADO:
                return "Entregado";
            case Constants.ESTADO_CANCELADO:
                return "Cancelado";
            default:
                return estado;
        }
    }

    private void llamarCliente() {
        if (viewModel.getPedidoActual() != null &&
                viewModel.getPedidoActual().getUsuario() != null &&
                viewModel.getPedidoActual().getUsuario().getTelefono() != null &&
                !viewModel.getPedidoActual().getUsuario().getTelefono().isEmpty()) {

            String telefono = viewModel.getPedidoActual().getUsuario().getTelefono();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + telefono));

            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error al iniciar la llamada", e);
                Toast.makeText(requireContext(), "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No hay número de teléfono disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirMapa() {
        if (viewModel.getPedidoActual() != null &&
                viewModel.getPedidoActual().getUbicacion() != null &&
                viewModel.getPedidoActual().getUbicacion().getLatitud() != 0 &&
                viewModel.getPedidoActual().getUbicacion().getLongitud() != 0) {

            double latitud = viewModel.getPedidoActual().getUbicacion().getLatitud();
            double longitud = viewModel.getPedidoActual().getUbicacion().getLongitud();

            // Intentar abrir Google Maps
            try {
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
            } catch (Exception e) {
                Log.e(TAG, "Error al abrir el mapa", e);
                Toast.makeText(requireContext(), "No se pudo abrir el mapa", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No hay ubicación disponible", Toast.LENGTH_SHORT).show();
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

        try {
            viewModel.actualizarEstadoPedido(pedidoId, nuevoEstado).observe(getViewLifecycleOwner(), result -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnEntregado.setEnabled(true);

                if (result.status == Resource.Status.SUCCESS && result.data != null) {

                    Toast.makeText(requireContext(), "Pedido marcado como entregado", Toast.LENGTH_SHORT).show();

                    // Actualizar datos locales
                    viewModel.setPedidoActual(result.data);

                    // Navegar de vuelta a la pantalla de entregas
                    try {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                        navController.navigateUp();
                    } catch (Exception e) {
                        Log.e("DetalleEntrega", "Error al navegar: " + e.getMessage());
                    }
                } else if (result.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnEntregado.setEnabled(true);
            Log.e("DetalleEntrega", "Error al actualizar estado: " + e.getMessage());
            Toast.makeText(requireContext(), "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarCargando(boolean mostrar) {
        isLoading = mostrar;
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.layoutContent.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        Log.e(TAG, mensaje);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        navController.navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}