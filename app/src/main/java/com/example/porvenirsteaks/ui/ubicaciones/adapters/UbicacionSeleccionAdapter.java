package com.example.porvenirsteaks.ui.ubicaciones.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.Ubicacion;
import com.example.porvenirsteaks.databinding.ItemUbicacionSeleccionBinding;

public class UbicacionSeleccionAdapter extends ListAdapter<Ubicacion, UbicacionSeleccionAdapter.UbicacionViewHolder> {
    private final OnUbicacionClickListener listener;

    public interface OnUbicacionClickListener {
        void onUbicacionClick(Ubicacion ubicacion);
    }

    public UbicacionSeleccionAdapter(OnUbicacionClickListener listener) {
        super(new UbicacionDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public UbicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUbicacionSeleccionBinding binding = ItemUbicacionSeleccionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UbicacionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UbicacionViewHolder holder, int position) {
        Ubicacion ubicacion = getItem(position);
        holder.bind(ubicacion, listener);
    }

    static class UbicacionViewHolder extends RecyclerView.ViewHolder {
        private final ItemUbicacionSeleccionBinding binding;

        public UbicacionViewHolder(ItemUbicacionSeleccionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ubicacion ubicacion, OnUbicacionClickListener listener) {
            binding.tvEtiqueta.setText(ubicacion.getEtiqueta());
            binding.tvDireccion.setText(ubicacion.getDireccionCompleta());

            // Mostrar u ocultar la etiqueta "Principal"
            if (ubicacion.isEsPrincipal()) {
                binding.tvPrincipal.setVisibility(View.VISIBLE);
            } else {
                binding.tvPrincipal.setVisibility(View.GONE);
            }

            // Configurar el listener para cuando se haga clic en la tarjeta
            itemView.setOnClickListener(v -> {
                listener.onUbicacionClick(ubicacion);
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
                    oldItem.isEsPrincipal() == newItem.isEsPrincipal() &&
                    oldItem.getEtiqueta().equals(newItem.getEtiqueta());
        }
    }
}