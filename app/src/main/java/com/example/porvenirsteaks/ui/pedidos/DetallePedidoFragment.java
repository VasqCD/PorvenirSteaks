package com.example.porvenirsteaks.ui.pedidos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.databinding.FragmentDetallePedidoBinding;
import com.example.porvenirsteaks.ui.pedidos.adapters.DetallePedidoAdapter;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.Resource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetallePedidoFragment extends Fragment {
    private FragmentDetallePedidoBinding binding;
    private PedidosViewModel viewModel;
    private DetallePedidoAdapter adapter;
    private int pedidoId;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;
    private LiveData<Resource<Pedido>> calificacionLiveData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetallePedidoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        viewModel = new ViewModelProvider(this).get(PedidosViewModel.class);

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

        binding.btnEnviarCalificacion.setOnClickListener(v -> {
            enviarCalificacion();
        });
    }

    private void cargarPedido() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutContent.setVisibility(View.GONE);

        viewModel.getPedidoById(pedidoId).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);

            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                actualizarUI(result.data);
                binding.layoutContent.setVisibility(View.VISIBLE);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI(com.example.porvenirsteaks.data.model.Pedido pedido) {
        try {
        binding.tvPedidoId.setText("Pedido #" + pedido.getId());
        binding.tvTotal.setText(currencyFormatter.format(pedido.getTotal()));

        // Estado
        String estado = pedido.getEstado();
        if (estado != null) {
            binding.tvEstado.setText(getEstadoFormateado(estado));

            // Color según estado
            int colorRes;
            switch (estado) {  // Usar la variable estado, no pedido.getEstado()
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
            binding.tvEstado.setTextColor(requireContext().getResources().getColor(colorRes));
        } else {
            // Si estado es null, asignar valores por defecto
            binding.tvEstado.setText("Desconocido");
            binding.tvEstado.setTextColor(requireContext().getResources().getColor(R.color.estado_pendiente));
        }

        // Fecha de pedido
        try {
            Date date = apiDateFormat.parse(pedido.getFechaPedido());
            binding.tvFechaPedido.setText(displayDateFormat.format(date));
        } catch (ParseException e) {
            binding.tvFechaPedido.setText(pedido.getFechaPedido());
        }

        // Fecha de entrega (si existe)
        if (pedido.getFechaEntrega() != null && !pedido.getFechaEntrega().isEmpty()) {
            try {
                Date date = apiDateFormat.parse(pedido.getFechaEntrega());
                binding.tvFechaEntrega.setText(displayDateFormat.format(date));
                binding.tvFechaEntregaLabel.setVisibility(View.VISIBLE);
                binding.tvFechaEntrega.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                binding.tvFechaEntrega.setText(pedido.getFechaEntrega());
                binding.tvFechaEntregaLabel.setVisibility(View.VISIBLE);
                binding.tvFechaEntrega.setVisibility(View.VISIBLE);
            }
        } else {
            binding.tvFechaEntregaLabel.setVisibility(View.GONE);
            binding.tvFechaEntrega.setVisibility(View.GONE);
        }

        // Ubicación
        if (pedido.getUbicacion() != null) {
            binding.tvDireccion.setText(pedido.getUbicacion().getDireccionCompleta());

            if (pedido.getUbicacion().getReferencias() != null && !pedido.getUbicacion().getReferencias().isEmpty()) {
                binding.tvReferencias.setText(pedido.getUbicacion().getReferencias());
                binding.tvReferenciasLabel.setVisibility(View.VISIBLE);
                binding.tvReferencias.setVisibility(View.VISIBLE);
            } else {
                binding.tvReferenciasLabel.setVisibility(View.GONE);
                binding.tvReferencias.setVisibility(View.GONE);
            }
        }

        } catch (Exception e) {
            Log.e("DetallePedido", "Error en actualizarUI: " + e.getMessage(), e);
        }


        // Detalles del pedido
        if (pedido.getDetalles() != null) {
            adapter.submitList(pedido.getDetalles());
        }

        // Sección de calificación (solo para pedidos entregados)
        if (pedido.getEstado().equals(Constants.ESTADO_ENTREGADO)) {
            binding.layoutCalificacion.setVisibility(View.VISIBLE);

            // Si ya está calificado, mostrar la calificación actual
            if (pedido.getCalificacion() != null) {
                binding.ratingBar.setRating(pedido.getCalificacion());
                binding.etComentario.setText(pedido.getComentarioCalificacion());
                binding.ratingBar.setIsIndicator(true);
                binding.etComentario.setEnabled(false);
                binding.btnEnviarCalificacion.setVisibility(View.GONE);
            } else {
                binding.ratingBar.setRating(0);
                binding.etComentario.setText("");
                binding.ratingBar.setIsIndicator(false);
                binding.etComentario.setEnabled(true);
                binding.btnEnviarCalificacion.setVisibility(View.VISIBLE);
            }
        } else {
            binding.layoutCalificacion.setVisibility(View.GONE);
        }
    }

        private String getEstadoFormateado(String estado) {
            if (estado == null) {
                return "Desconocido";
            }

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

    private void enviarCalificacion() {
        int calificacion = (int) binding.ratingBar.getRating();
        if (calificacion == 0) {
            Toast.makeText(requireContext(), "Por favor, selecciona una calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        String comentario = binding.etComentario.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnEnviarCalificacion.setEnabled(false);

        // Obtener el nuevo LiveData y observarlo
        viewModel.calificarPedido(pedidoId, calificacion, comentario).observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnEnviarCalificacion.setEnabled(true);

            if (result.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Calificación enviada con éxito", Toast.LENGTH_SHORT).show();

                // En lugar de actualizar la UI, navegamos de vuelta a la lista de pedidos
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigateUp(); // O usa popBackStack() para volver atrás

                // Alternativamente, puedes navegar directamente a nav_pedidos:
                // navController.navigate(R.id.nav_pedidos);
            } else if (result.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (calificacionLiveData != null) {
            calificacionLiveData.removeObservers(getViewLifecycleOwner());
        }
        binding = null;
    }
}