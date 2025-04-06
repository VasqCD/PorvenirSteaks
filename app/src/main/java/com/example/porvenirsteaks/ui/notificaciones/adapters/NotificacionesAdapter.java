package com.example.porvenirsteaks.ui.notificaciones.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.data.model.Notificacion;
import com.example.porvenirsteaks.databinding.ItemNotificacionBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificacionesAdapter extends ListAdapter<Notificacion, NotificacionesAdapter.NotificacionViewHolder> {
    private OnNotificacionClickListener listener;
    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;

    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificacion notificacion);
    }

    public NotificacionesAdapter(OnNotificacionClickListener listener) {
        super(new NotificacionDiffCallback());
        this.listener = listener;
        this.apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        this.displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificacionBinding binding = ItemNotificacionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificacionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificacion notificacion = getItem(position);
        holder.bind(notificacion, listener, apiDateFormat, displayDateFormat);
    }

    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificacionBinding binding;

        public NotificacionViewHolder(ItemNotificacionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Notificacion notificacion, OnNotificacionClickListener listener,
                         SimpleDateFormat apiDateFormat, SimpleDateFormat displayDateFormat) {

            binding.tvTitulo.setText(notificacion.getTitulo());
            binding.tvMensaje.setText(notificacion.getMensaje());

            // Formatear fecha
            try {
                Date date = apiDateFormat.parse(notificacion.getCreatedAt());
                binding.tvFecha.setText(displayDateFormat.format(date));
            } catch (ParseException e) {
                binding.tvFecha.setText(notificacion.getCreatedAt());
            }

            // Estilo según estado (leída o no)
            if (!notificacion.isLeida()) {
                binding.tvTitulo.setTypeface(null, Typeface.BOLD);
                binding.indicadorNoLeida.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvTitulo.setTypeface(null, Typeface.NORMAL);
                binding.indicadorNoLeida.setVisibility(android.view.View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                listener.onNotificacionClick(notificacion);
            });
        }
    }

    static class NotificacionDiffCallback extends DiffUtil.ItemCallback<Notificacion> {
        @Override
        public boolean areItemsTheSame(@NonNull Notificacion oldItem, @NonNull Notificacion newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notificacion oldItem, @NonNull Notificacion newItem) {
            return oldItem.isLeida() == newItem.isLeida() &&
                    oldItem.getTitulo().equals(newItem.getTitulo()) &&
                    oldItem.getMensaje().equals(newItem.getMensaje());
        }
    }
}