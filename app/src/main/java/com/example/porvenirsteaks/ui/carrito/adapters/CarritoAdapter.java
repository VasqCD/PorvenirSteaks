package com.example.porvenirsteaks.ui.carrito.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.CartItem;
import com.example.porvenirsteaks.databinding.ItemCarritoBinding;
import com.example.porvenirsteaks.utils.ImageUtils;

import java.text.NumberFormat;

public class CarritoAdapter extends ListAdapter<CartItem, CarritoAdapter.CartItemViewHolder> {
    private OnItemUpdateListener listener;
    private NumberFormat currencyFormatter;

    public interface OnItemUpdateListener {
        void onQuantityChanged(int productoId, int newCantidad);
        void onItemRemoved(int productoId);
    }

    public CarritoAdapter(OnItemUpdateListener listener, NumberFormat currencyFormatter) {
        super(new CartItemDiffCallback());
        this.listener = listener;
        this.currencyFormatter = currencyFormatter;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCarritoBinding binding = ItemCarritoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item, listener, currencyFormatter);
    }

    static class CartItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemCarritoBinding binding;

        public CartItemViewHolder(ItemCarritoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem item, OnItemUpdateListener listener, NumberFormat currencyFormatter) {
            binding.tvNombre.setText(item.getProducto().getNombre());
            binding.tvPrecio.setText(currencyFormatter.format(item.getProducto().getPrecio()));
            binding.tvCantidad.setText(String.valueOf(item.getCantidad()));
            binding.tvSubtotal.setText(currencyFormatter.format(item.getSubtotal()));

            ImageUtils.loadImage(binding.ivProducto, item.getProducto().getImagen());

            // Setup cantidad buttons
            binding.btnIncrement.setOnClickListener(v -> {
                int newCantidad = item.getCantidad() + 1;
                binding.tvCantidad.setText(String.valueOf(newCantidad));
                listener.onQuantityChanged(item.getProducto().getId(), newCantidad);
            });

            binding.btnDecrement.setOnClickListener(v -> {
                if (item.getCantidad() > 1) {
                    int newCantidad = item.getCantidad() - 1;
                    binding.tvCantidad.setText(String.valueOf(newCantidad));
                    listener.onQuantityChanged(item.getProducto().getId(), newCantidad);
                }
            });

            binding.btnRemove.setOnClickListener(v -> {
                listener.onItemRemoved(item.getProducto().getId());
            });
        }
    }

    static class CartItemDiffCallback extends DiffUtil.ItemCallback<CartItem> {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getProducto().getId() == newItem.getProducto().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getCantidad() == newItem.getCantidad() &&
                    oldItem.getProducto().getPrecio() == newItem.getProducto().getPrecio();
        }
    }
}