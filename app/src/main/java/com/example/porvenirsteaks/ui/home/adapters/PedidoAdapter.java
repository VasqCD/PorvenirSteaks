package com.example.porvenirsteaks.ui.home.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porvenirsteaks.R;
import com.example.porvenirsteaks.data.model.DetallePedido;
import com.example.porvenirsteaks.data.model.Pedido;
import com.example.porvenirsteaks.utils.Constants;
import com.example.porvenirsteaks.utils.DateUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos = new ArrayList<>();
    private OnPedidoClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
    }

    public PedidoAdapter(OnPedidoClickListener listener) {
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "HN"));
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido_home, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        holder.bind(pedidos.get(position));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPedidoId;
        private TextView tvEstado;
        private TextView tvFecha;
        private TextView tvProductosResumen;
        private TextView tvTotal;
        private Button btnDetalle;
        private Button btnRepetir;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoIdHome);
            tvEstado = itemView.findViewById(R.id.tvEstadoHome);
            tvFecha = itemView.findViewById(R.id.tvFechaHome);
            tvProductosResumen = itemView.findViewById(R.id.tvProductosResumen);
            tvTotal = itemView.findViewById(R.id.tvTotalHome);
            btnDetalle = itemView.findViewById(R.id.btnDetalleHome);
            btnRepetir = itemView.findViewById(R.id.btnRepetirHome);
        }

        public void bind(Pedido pedido) {
            Context context = itemView.getContext();

            tvPedidoId.setText("Pedido #" + pedido.getId());
            tvEstado.setText(formatEstadoPedido(pedido.getEstado()));
            tvEstado.setTextColor(getColorForEstado(context, pedido.getEstado()));

            // Formatear fecha
            String fechaFormateada = DateUtils.formatDateString(pedido.getFechaPedido(), "dd/MM/yyyy HH:mm");
            tvFecha.setText(fechaFormateada);

            // Generar resumen de productos
            StringBuilder resumen = new StringBuilder();
            if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                for (int i = 0; i < Math.min(pedido.getDetalles().size(), 3); i++) {
                    DetallePedido detalle = pedido.getDetalles().get(i);
                    resumen.append(detalle.getCantidad())
                            .append("x ")
                            .append(detalle.getProducto().getNombre());

                    if (i < Math.min(pedido.getDetalles().size(), 3) - 1) {
                        resumen.append(", ");
                    }
                }

                if (pedido.getDetalles().size() > 3) {
                    resumen.append("...");
                }
            } else {
                resumen.append("Sin detalles disponibles");
            }
            tvProductosResumen.setText(resumen.toString());

            // Formatear total
            tvTotal.setText(currencyFormat.format(pedido.getTotal()));

            // Configurar botones
            btnDetalle.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPedidoClick(pedido);
                }
            });

            btnRepetir.setOnClickListener(v -> {
                // Aquí se implementaría la funcionalidad para repetir pedido
                // Por ahora, solo mostramos el detalle
                if (listener != null) {
                    listener.onPedidoClick(pedido);
                }
            });

            // También permitir hacer clic en toda la tarjeta
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPedidoClick(pedido);
                }
            });
        }

        private String formatEstadoPedido(String estado) {
            switch (estado) {
                case Constants.ESTADO_PENDIENTE:
                    return "Pendiente";
                case Constants.ESTADO_EN_COCINA:
                    return "En cocina";
                case Constants.ESTADO_EN_CAMINO:
                    return "En camino";
                case Constants.ESTADO_ENTREGADO:
                    return "Entregado";
                case Constants.ESTADO_CANCELADO:
                    return "Cancelado";
                default:
                    return estado;
            }
        }

        private int getColorForEstado(Context context, String estado) {
            switch (estado) {
                case Constants.ESTADO_PENDIENTE:
                    return ContextCompat.getColor(context, R.color.estado_pendiente);
                case Constants.ESTADO_EN_COCINA:
                    return ContextCompat.getColor(context, R.color.estado_en_cocina);
                case Constants.ESTADO_EN_CAMINO:
                    return ContextCompat.getColor(context, R.color.estado_en_camino);
                case Constants.ESTADO_ENTREGADO:
                    return ContextCompat.getColor(context, R.color.estado_entregado);
                case Constants.ESTADO_CANCELADO:
                    return ContextCompat.getColor(context, R.color.estado_cancelado);
                default:
                    return ContextCompat.getColor(context, R.color.text_primary);
            }
        }
    }
}