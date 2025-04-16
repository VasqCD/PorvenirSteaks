package com.example.porvenirsteaks.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Categoria;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder> {

    private List<Categoria> categorias = new ArrayList<>();
    private OnCategoriaClickListener listener;
    private int selectedPosition = -1;

    public interface OnCategoriaClickListener {
        void onCategoriaClick(Categoria categoria);
    }

    public CategoriasAdapter(OnCategoriaClickListener listener) {
        this.listener = listener;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        holder.bind(categorias.get(position), position);
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    class CategoriaViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvCategoria;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
        }

        public void bind(Categoria categoria, int position) {
            tvCategoria.setText(categoria.getNombre());

            // Marcar la categoría seleccionada
            cardView.setChecked(selectedPosition == position);

            itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = position;

                // Actualizar los cambios visuales
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onCategoriaClick(categoria);
                }
            });
        }
    }
}