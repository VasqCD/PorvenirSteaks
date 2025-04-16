package com.example.porvenirsteaks.ui.home.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.data.model.User;
import com.example.porvenirsteaks.data.model.Ubicacion;

import java.util.ArrayList;
import java.util.List;

public class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.EntregaViewHolder> {

    private List<Pedido> pedidos = new ArrayList<>();
    private OnEntregaClickListener listener;

    public interface OnEntregaClickListener {
        void onEntregaClick(Pedido pedido);
    }

    public EntregaAdapter(OnEntregaClickListener listener) {
        this.listener = listener;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntregaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrega_home, parent, false);
        return new EntregaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntregaViewHolder holder, int position) {
        holder.bind(pedidos.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    class EntregaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPedidoId;
        private TextView tvDistancia;
        private TextView tvClienteNombre;
        private TextView tvDireccion;
        private ImageButton btnVerMapa;
        private Button btnLlamarCliente;
        private Button btnIniciarEntrega;

        public EntregaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoIdEntrega);
            tvDistancia = itemView.findViewById(R.id.tvDistanciaEntrega);
            tvClienteNombre = itemView.findViewById(R.id.tvClienteNombreEntrega);
            tvDireccion = itemView.findViewById(R.id.tvDireccionEntrega);
            btnVerMapa = itemView.findViewById(R.id.btnVerMapaEntrega);
            btnLlamarCliente = itemView.findViewById(R.id.btnLlamarClienteEntrega);
            btnIniciarEntrega = itemView.findViewById(R.id.btnIniciarEntrega);
        }

        public void bind(Pedido pedido) {
            Context context = itemView.getContext();

            tvPedidoId.setText("Pedido #" + pedido.getId());

            // Información del cliente
            User cliente = pedido.getUsuario();
            if (cliente != null) {
                tvClienteNombre.setText(cliente.getName() + " " + cliente.getApellido());
            } else {
                tvClienteNombre.setText("Cliente no disponible");
            }

            // Información de la ubicación
            Ubicacion ubicacion = pedido.getUbicacion();
            if (ubicacion != null) {
                tvDireccion.setText(ubicacion.getDireccionCompleta());

                // Simulamos una distancia (en una implementación real, esto vendría de la API)
                double distanciaSimulada = 1.5 + (Math.random() * 5); // Entre 1.5 y 6.5 km
                tvDistancia.setText(String.format("%.1f km", distanciaSimulada));
            } else {
                tvDireccion.setText("Dirección no disponible");
                tvDistancia.setText("N/A");
            }

            // Configurar acciones de los botones
            btnVerMapa.setOnClickListener(v -> {
                // En una implementación real, esto abriría un mapa
                Toast.makeText(context, "Abrir mapa (no implementado)", Toast.LENGTH_SHORT).show();
            });

            btnLlamarCliente.setOnClickListener(v -> {
                // En una implementación real, esto iniciaría una llamada
                if (cliente != null && cliente.getTelefono() != null) {
                    try {
                        Uri uri = Uri.parse("tel:" + cliente.getTelefono());
                        // Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                        // context.startActivity(intent);

                        // Por ahora, solo mostramos un mensaje
                        Toast.makeText(context, "Llamar a: " + cliente.getTelefono(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Teléfono no disponible", Toast.LENGTH_SHORT).show();
                }
            });

            btnIniciarEntrega.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntregaClick(pedido);
                }
            });

            // También permitir hacer clic en toda la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEntregaClick(pedido);
                }
            });
        }
    }
}