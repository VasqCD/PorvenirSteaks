package com.example.porvenirsteaks.ui.pedidos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.databinding.ItemPedidoBinding;
import com.example.porvenirsteaks.utils.Constants;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PedidosAdapter extends ListAdapter<Pedido, PedidosAdapter.PedidoViewHolder> {
    private OnPedidoClickListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;

    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
    }

    public PedidosAdapter(OnPedidoClickListener listener) {
        super(new PedidoDiffCallback());
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
        this.apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        this.displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPedidoBinding binding = ItemPedidoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = getItem(position);
        holder.bind(pedido, listener, currencyFormatter, apiDateFormat, displayDateFormat);
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoBinding binding;

        public PedidoViewHolder(ItemPedidoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Pedido pedido, OnPedidoClickListener listener, NumberFormat currencyFormatter,
                         SimpleDateFormat apiDateFormat, SimpleDateFormat displayDateFormat) {

            binding.tvPedidoId.setText("Pedido #" + pedido.getId());
            binding.tvTotal.setText(currencyFormatter.format(pedido.getTotal()));

            // Formatear fecha
            try {
                Date date = apiDateFormat.parse(pedido.getFechaPedido());
                binding.tvFecha.setText(displayDateFormat.format(date));
            } catch (ParseException e) {
                binding.tvFecha.setText(pedido.getFechaPedido());
            }

            // Estado
            binding.tvEstado.setText(formatEstado(pedido.getEstado()));

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

            binding.tvEstado.setTextColor(ContextCompat.getColor(binding.tvEstado.getContext(), colorRes));

            // Dirección
            if (pedido.getUbicacion() != null) {
                binding.tvDireccion.setText(pedido.getUbicacion().getDireccionCompleta());
            } else {
                binding.tvDireccion.setVisibility(View.GONE);
            }

            // Calificación
            if (pedido.getEstado().equals(Constants.ESTADO_ENTREGADO)) {
                binding.layoutCalificacion.setVisibility(View.VISIBLE);

                if (pedido.getCalificacion() != null) {
                    binding.ratingBar.setRating(pedido.getCalificacion());
                    binding.ratingBar.setIsIndicator(true);
                    binding.btnCalificar.setVisibility(View.GONE);
                } else {
                    binding.ratingBar.setRating(0);
                    binding.ratingBar.setIsIndicator(false);
                    binding.btnCalificar.setVisibility(View.VISIBLE);
                }
            } else {
                binding.layoutCalificacion.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                listener.onPedidoClick(pedido);
            });
        }

        private String formatEstado(String estado) {
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
    }

    static class PedidoDiffCallback extends DiffUtil.ItemCallback<Pedido> {
        @Override
        public boolean areItemsTheSame(@NonNull Pedido oldItem, @NonNull Pedido newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pedido oldItem, @NonNull Pedido newItem) {
            return oldItem.getEstado().equals(newItem.getEstado()) &&
                    oldItem.getCalificacion() == newItem.getCalificacion();
        }
    }
}