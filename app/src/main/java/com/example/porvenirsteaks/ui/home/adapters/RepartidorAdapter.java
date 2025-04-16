package com.example.porvenirsteaks.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Repartidor;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.utils.DateUtils;
import com.example.porvenirsteaks.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepartidorAdapter extends RecyclerView.Adapter<RepartidorAdapter.RepartidorViewHolder> {

    private List<Repartidor> repartidores = new ArrayList<>();
    private OnRepartidorClickListener listener;

    public interface OnRepartidorClickListener {
        void onRepartidorClick(Repartidor repartidor);
    }

    public RepartidorAdapter(OnRepartidorClickListener listener) {
        this.listener = listener;
    }

    public void setRepartidores(List<Repartidor> repartidores) {
        this.repartidores = repartidores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RepartidorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repartidor, parent, false);
        return new RepartidorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepartidorViewHolder holder, int position) {
        holder.bind(repartidores.get(position));
    }

    @Override
    public int getItemCount() {
        return repartidores.size();
    }

    class RepartidorViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfilePic;
        private TextView tvRepartidorNombre;
        private TextView tvRepartidorTelefono;
        private View badgeDisponible;
        private TextView tvUltimaActualizacion;
        private MaterialButton btnAsignarPedido;

        public RepartidorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvRepartidorNombre = itemView.findViewById(R.id.tvRepartidorNombre);
            tvRepartidorTelefono = itemView.findViewById(R.id.tvRepartidorTelefono);
            badgeDisponible = itemView.findViewById(R.id.badgeDisponible);
            tvUltimaActualizacion = itemView.findViewById(R.id.tvUltimaActualizacion);
            btnAsignarPedido = itemView.findViewById(R.id.btnAsignarPedido);
        }

        public void bind(Repartidor repartidor) {
            Context context = itemView.getContext();

            // Información del usuario
            User usuario = repartidor.getUsuario();
            if (usuario != null) {
                tvRepartidorNombre.setText(usuario.getName() + " " + usuario.getApellido());
                tvRepartidorTelefono.setText(usuario.getTelefono());

                // Cargar foto de perfil
                if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                    ImageUtils.loadUserPhoto(ivProfilePic, usuario.getFotoPerfil());
                } else {
                    ivProfilePic.setImageResource(R.drawable.user_placeholder);
                }
            } else {
                tvRepartidorNombre.setText("Repartidor no disponible");
                tvRepartidorTelefono.setText("Sin teléfono");
                ivProfilePic.setImageResource(R.drawable.user_placeholder);
            }

            // Estado de disponibilidad
            if (repartidor.isDisponible()) {
                badgeDisponible.setBackgroundColor(ContextCompat.getColor(context, R.color.estado_en_camino));
            } else {
                badgeDisponible.setBackgroundColor(ContextCompat.getColor(context, R.color.estado_cancelado));
            }

            // Última actualización
            String ultimaActualizacion = "Sin actividad reciente";
            if (repartidor.getUltimaActualizacion() != null) {
                Date fecha = DateUtils.parseDate(repartidor.getUltimaActualizacion());
                if (fecha != null) {
                    ultimaActualizacion = "Última actualización: " + DateUtils.formatDate(fecha, "HH:mm");
                }
            }
            tvUltimaActualizacion.setText(ultimaActualizacion);

            // Configurar acción del botón
            btnAsignarPedido.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRepartidorClick(repartidor);
                }
            });

            // También permitir hacer clic en toda la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRepartidorClick(repartidor);
                }
            });
        }
    }
}
