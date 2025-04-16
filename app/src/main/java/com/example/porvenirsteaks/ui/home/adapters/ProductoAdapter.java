package com.example.porvenirsteaks.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Producto;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> productos = new ArrayList<>();
    private OnProductoClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    public ProductoAdapter(OnProductoClickListener listener) {
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_horizontal, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        holder.bind(productos.get(position));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProducto;
        private TextView tvNombre;
        private TextView tvPrecio;
        private MaterialButton btnAddToCart;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProductoHorizontal);
            tvNombre = itemView.findViewById(R.id.tvNombreHorizontal);
            tvPrecio = itemView.findViewById(R.id.tvPrecioHorizontal);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCartHorizontal);
        }

        public void bind(Producto producto) {
            tvNombre.setText(producto.getNombre());
            tvPrecio.setText(currencyFormat.format(producto.getPrecio()));

            // Cargar imagen
            ImageUtils.loadImage(ivProducto, producto.getImagen());

            // Configurar clics
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductoClick(producto);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                // Aquí se implementaría la lógica para agregar al carrito
                // Por ahora, solo abrimos el detalle del producto
                if (listener != null) {
                    listener.onProductoClick(producto);
                }
            });
        }
    }
}