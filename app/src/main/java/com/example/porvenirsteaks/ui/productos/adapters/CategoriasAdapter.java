package com.example.porvenirsteaks.ui.productos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.Categoria;
import com.example.porvenirsteaks.databinding.ItemCategoriaBinding;

public class CategoriasAdapter extends ListAdapter<Categoria, CategoriasAdapter.CategoriaViewHolder> {
    private OnCategoriaClickListener listener;
    private int selectedPosition = 0; // Por defecto, la primera categoría (Todas)

    public interface OnCategoriaClickListener {
        void onCategoriaClick(Integer categoriaId);
    }

    public CategoriasAdapter(OnCategoriaClickListener listener) {
        super(new CategoriaDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoriaBinding binding = ItemCategoriaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoriaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = getItem(position);
        holder.bind(categoria, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int oldPosition = selectedPosition;
                selectedPosition = position;

                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);

                if (position == 0) {
                    // La primera posición es "Todas las categorías"
                    listener.onCategoriaClick(null);
                } else {
                    listener.onCategoriaClick(categoria.getId());
                }
            }
        });
    }

    static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoriaBinding binding;

        public CategoriaViewHolder(ItemCategoriaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Categoria categoria, boolean isSelected) {
            binding.tvCategoria.setText(categoria.getNombre());
            binding.cardView.setSelected(isSelected);
        }
    }

    static class CategoriaDiffCallback extends DiffUtil.ItemCallback<Categoria> {
        @Override
        public boolean areItemsTheSame(@NonNull Categoria oldItem, @NonNull Categoria newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Categoria oldItem, @NonNull Categoria newItem) {
            return oldItem.getNombre().equals(newItem.getNombre());
        }
    }
}