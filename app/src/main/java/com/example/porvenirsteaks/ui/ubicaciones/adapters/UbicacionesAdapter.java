package com.example.porvenirsteaks.ui.ubicaciones.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.ItemUbicacionBinding;

public class UbicacionesAdapter extends ListAdapter<Ubicacion, UbicacionesAdapter.UbicacionViewHolder> {
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(Ubicacion ubicacion);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Ubicacion ubicacion);
    }

    public UbicacionesAdapter(OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        super(new UbicacionDiffCallback());
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUbicacionBinding binding = ItemUbicacionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UbicacionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UbicacionViewHolder holder, int position) {
        Ubicacion ubicacion = getItem(position);
        holder.bind(ubicacion, editListener, deleteListener);
    }

    static class UbicacionViewHolder extends RecyclerView.ViewHolder {
        private final ItemUbicacionBinding binding;

        public UbicacionViewHolder(ItemUbicacionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ubicacion ubicacion, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
            binding.tvDireccion.setText(ubicacion.getDireccionCompleta());
            binding.tvEtiqueta.setText(ubicacion.getEtiqueta());

            if (ubicacion.isEsPrincipal()) {
                binding.tvPrincipal.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvPrincipal.setVisibility(android.view.View.GONE);
            }

            binding.btnEditar.setOnClickListener(v -> {
                editListener.onEditClick(ubicacion);
            });

            binding.btnEliminar.setOnClickListener(v -> {
                deleteListener.onDeleteClick(ubicacion);
            });
        }
    }

    static class UbicacionDiffCallback extends DiffUtil.ItemCallback<Ubicacion> {
        @Override
        public boolean areItemsTheSame(@NonNull Ubicacion oldItem, @NonNull Ubicacion newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ubicacion oldItem, @NonNull Ubicacion newItem) {
            return oldItem.getDireccionCompleta().equals(newItem.getDireccionCompleta()) &&
                    oldItem.isEsPrincipal() == newItem.isEsPrincipal();
        }
    }
}