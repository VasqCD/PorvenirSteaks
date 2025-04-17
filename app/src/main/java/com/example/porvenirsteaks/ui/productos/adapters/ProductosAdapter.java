package com.example.porvenirsteaks.ui.productos.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.databinding.ItemProductoBinding;
import com.example.porvenirsteaks.utils.ImageUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductosAdapter extends ListAdapter<Producto, ProductosAdapter.ProductoViewHolder> {
    private OnProductoClickListener listener;
    private NumberFormat currencyFormatter;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
        void onAddToCartClick(Producto producto);  // Añadir este método
    }

    public ProductosAdapter(OnProductoClickListener listener) {
        super(new ProductoDiffCallback());
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductoBinding binding = ItemProductoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = getItem(position);
        holder.bind(producto, currencyFormatter);

        holder.itemView.setOnClickListener(v -> {
            listener.onProductoClick(producto);
        });

        // Añadir listener para el botón de agregar al carrito
        holder.binding.btnAddToCart.setOnClickListener(v -> {
            listener.onAddToCartClick(producto);
        });
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductoBinding binding;

        public ProductoViewHolder(ItemProductoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Producto producto, NumberFormat currencyFormatter) {
            binding.tvNombre.setText(producto.getNombre());
            binding.tvPrecio.setText(currencyFormatter.format(producto.getPrecio()));

            // Cargar imagen
            ImageUtils.loadImage(binding.ivProducto, producto.getImagen());
        }
    }

    static class ProductoDiffCallback extends DiffUtil.ItemCallback<Producto> {
        @Override
        public boolean areItemsTheSame(@NonNull Producto oldItem, @NonNull Producto newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Producto oldItem, @NonNull Producto newItem) {
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.getPrecio() == newItem.getPrecio() &&
                    oldItem.isDisponible() == newItem.isDisponible();
        }
    }
}