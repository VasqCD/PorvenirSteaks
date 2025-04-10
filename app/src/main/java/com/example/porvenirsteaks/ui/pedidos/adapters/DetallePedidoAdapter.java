package com.example.porvenirsteaks.ui.pedidos.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.DetallePedido;
import com.example.porvenirsteaks.databinding.ItemDetallePedidoBinding;

import java.text.NumberFormat;

public class DetallePedidoAdapter extends ListAdapter<DetallePedido, DetallePedidoAdapter.DetallePedidoViewHolder> {
    private NumberFormat currencyFormatter;

    public DetallePedidoAdapter(NumberFormat currencyFormatter) {
        super(new DetallePedidoDiffCallback());
        this.currencyFormatter = currencyFormatter;
    }

    @NonNull
    @Override
    public DetallePedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDetallePedidoBinding binding = ItemDetallePedidoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DetallePedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetallePedidoViewHolder holder, int position) {
        DetallePedido detalle = getItem(position);
        holder.bind(detalle, currencyFormatter);
    }

    static class DetallePedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemDetallePedidoBinding binding;

        public DetallePedidoViewHolder(ItemDetallePedidoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetallePedido detalle, NumberFormat currencyFormatter) {
            if (detalle.getProducto() != null) {
                binding.tvNombreProducto.setText(detalle.getProducto().getNombre());
            } else {
                binding.tvNombreProducto.setText("Producto #" + detalle.getProductoId());
            }

            binding.tvCantidad.setText("Cantidad: " + detalle.getCantidad());
            binding.tvPrecioUnitario.setText(currencyFormatter.format(detalle.getPrecioUnitario()));
            binding.tvSubtotal.setText(currencyFormatter.format(detalle.getSubtotal()));
        }
    }

    static class DetallePedidoDiffCallback extends DiffUtil.ItemCallback<DetallePedido> {
        @Override
        public boolean areItemsTheSame(@NonNull DetallePedido oldItem, @NonNull DetallePedido newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DetallePedido oldItem, @NonNull DetallePedido newItem) {
            return oldItem.getCantidad() == newItem.getCantidad() &&
                    oldItem.getPrecioUnitario() == newItem.getPrecioUnitario() &&
                    oldItem.getSubtotal() == newItem.getSubtotal();
        }
    }
}