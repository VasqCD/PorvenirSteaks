package com.example.porvenirsteaks.ui.admin.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.databinding.ItemRepartidorBinding;
import com.example.porvenirsteaks.utils.ImageUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RepartidorAdapter extends ListAdapter<Repartidor, RepartidorAdapter.RepartidorViewHolder> {
    private final OnRepartidorClickListener listener;
    private final SimpleDateFormat apiDateFormat;

    public interface OnRepartidorClickListener {
        void onRepartidorClick(Repartidor repartidor, Integer pedidoId);
    }

    public RepartidorAdapter(OnRepartidorClickListener listener) {
        super(new RepartidorDiffCallback());
        this.listener = listener;
        this.apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
    }

    @NonNull
    @Override
    public RepartidorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRepartidorBinding binding = ItemRepartidorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RepartidorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RepartidorViewHolder holder, int position) {
        Repartidor repartidor = getItem(position);
        holder.bind(repartidor, listener, apiDateFormat);
    }

    static class RepartidorViewHolder extends RecyclerView.ViewHolder {
        private final ItemRepartidorBinding binding;

        public RepartidorViewHolder(ItemRepartidorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Repartidor repartidor, OnRepartidorClickListener listener, SimpleDateFormat apiDateFormat) {
            // Foto de perfil
            if (repartidor.getUsuario() != null && repartidor.getUsuario().getFotoPerfil() != null) {
                ImageUtils.loadUserPhoto(binding.ivProfilePic, repartidor.getUsuario().getFotoPerfil());
            } else {
                binding.ivProfilePic.setImageResource(R.drawable.user_placeholder);
            }

            // Nombre del repartidor
            if (repartidor.getUsuario() != null) {
                String nombre = repartidor.getUsuario().getName();
                String apellido = repartidor.getUsuario().getApellido();

                if (apellido != null && !apellido.isEmpty()) {
                    binding.tvRepartidorNombre.setText(nombre + " " + apellido);
                } else {
                    binding.tvRepartidorNombre.setText(nombre);
                }

                // Teléfono
                binding.tvRepartidorTelefono.setText(repartidor.getUsuario().getTelefono());
            } else {
                binding.tvRepartidorNombre.setText("Repartidor #" + repartidor.getId());
                binding.tvRepartidorTelefono.setText("");
            }

            // Indicador de disponibilidad
            if (repartidor.isDisponible()) {
                binding.badgeDisponible.setBackgroundResource(R.drawable.circle_indicator);
                binding.badgeDisponible.setBackgroundTintList(
                        ContextCompat.getColorStateList(binding.badgeDisponible.getContext(), R.color.estado_entregado));
            } else {
                binding.badgeDisponible.setBackgroundResource(R.drawable.circle_indicator);
                binding.badgeDisponible.setBackgroundTintList(
                        ContextCompat.getColorStateList(binding.badgeDisponible.getContext(), R.color.estado_cancelado));
            }

            // Última actualización
            if (repartidor.getUltimaActualizacion() != null) {
                try {
                    Date lastUpdate = apiDateFormat.parse(repartidor.getUltimaActualizacion());
                    if (lastUpdate != null) {
                        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                                lastUpdate.getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS);
                        binding.tvUltimaActualizacion.setText("Última actualización: " + timeAgo);
                    }
                } catch (ParseException e) {
                    binding.tvUltimaActualizacion.setText("Última actualización: " + repartidor.getUltimaActualizacion());
                }
            } else {
                binding.tvUltimaActualizacion.setVisibility(View.GONE);
            }

            // Botón de asignar pedido
            binding.btnAsignarPedido.setOnClickListener(v -> {
                // Pasar null por ahora, ya que no tenemos un pedido seleccionado
                // En una implementación más completa, se podría implementar un mecanismo para seleccionar un pedido
                listener.onRepartidorClick(repartidor, null);
            });

            // Click en el elemento completo
            itemView.setOnClickListener(v -> {
                listener.onRepartidorClick(repartidor, null);
            });
        }
    }

    static class RepartidorDiffCallback extends DiffUtil.ItemCallback<Repartidor> {
        @Override
        public boolean areItemsTheSame(@NonNull Repartidor oldItem, @NonNull Repartidor newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Repartidor oldItem, @NonNull Repartidor newItem) {
            return oldItem.isDisponible() == newItem.isDisponible() &&
                    oldItem.getUltimaActualizacion() != null &&
                    oldItem.getUltimaActualizacion().equals(newItem.getUltimaActualizacion());
        }
    }
}