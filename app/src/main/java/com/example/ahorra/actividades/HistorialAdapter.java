package com.example.ahorra.actividades;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Importante para los colores
import androidx.recyclerview.widget.RecyclerView;
import com.example.ahorra.R; // Asegúrate que esta R sea la de tu app

import java.util.List;
import java.util.Locale;

// Este Adapter maneja una LISTA DE OBJETOS.
// Estos "Object" pueden ser de tipo "Transaccion" o "String" (para las fechas)
public class HistorialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    // Constantes para saber qué "molde" (XML) usar
    private static final int TIPO_TRANSACCION = 0;
    private static final int TIPO_FECHA = 1;

    private Context context;
    private List<Object> items;

    // Constructor
    public HistorialAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    // ===================================================================
    // 1. EL CEREBRO: ¿Qué "molde" uso para esta posición?
    // ===================================================================
    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Transaccion) {
            return TIPO_TRANSACCION; // Usa el molde de transacción
        } else if (items.get(position) instanceof String) {
            return TIPO_FECHA; // Usa el molde de fecha
        }
        return -1; // Error
    }

    // ===================================================================
    // 2. EL CONSTRUCTOR: Crea el "molde" (ViewHolder)
    // ===================================================================
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;

        if (viewType == TIPO_TRANSACCION) {
            // Infla el XML de la tarjeta de transacción
            view = inflater.inflate(R.layout.item_transaccion, parent, false);
            return new TransaccionViewHolder(view);
        } else {
            // Infla el XML del header de fecha
            view = inflater.inflate(R.layout.item_fecha_header, parent, false);
            return new FechaViewHolder(view);
        }
    }

    // ===================================================================
    // 3. EL RELLENADOR: Rellena el "molde" con los datos
    // ===================================================================
    // Pega esto en HistorialAdapter.java
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Revisa qué tipo de "molde" (ViewHolder) es

        if (holder.getItemViewType() == TIPO_TRANSACCION) {
            // Es una transacción, así que lo "convertimos"
            TransaccionViewHolder tvh = (TransaccionViewHolder) holder;
            Transaccion transaccion = (Transaccion) items.get(position);

            // Rellenamos los datos (ESTA ES LA PARTE QUE TE FALTA)
            tvh.iconoCategoria.setImageResource(transaccion.getIcono());
            tvh.textoCategoria.setText(transaccion.getCategoria());
            tvh.textoHora.setText(transaccion.getHora());

            // --- LA LÓGICA DE COLOR (ROJO / VERDE) ---
            if (transaccion.getTipo().equals("Gasto")) {
                tvh.textoMonto.setText("-S/ " + String.format(Locale.US, "%.2f", transaccion.getMonto()));
                // (Asegúrate de tener rojo_gasto en res/values/colors.xml)
                tvh.textoMonto.setTextColor(ContextCompat.getColor(context, R.color.rojo_gasto));
            } else { // Es "Ingreso"
                tvh.textoMonto.setText("+S/ " + String.format(Locale.US, "%.2f", transaccion.getMonto()));
                // (Asegúrate de tener verde_ingreso en res/values/colors.xml)
                tvh.textoMonto.setTextColor(ContextCompat.getColor(context, R.color.verde_ingreso));
            }

        } else {
            // Es una fecha, así que lo "convertimos"
            FechaViewHolder fvh = (FechaViewHolder) holder;
            String fecha = (String) items.get(position);

            // Rellenamos la fecha (ESTA PARTE TAMBIÉN ES CLAVE)
            fvh.textoFechaHeader.setText(fecha);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    // ===================================================================
    // "CAJAS" DE VISTAS (VIEWHOLDERS)
    // ===================================================================

    // "Caja" para el molde de Transacción (item_transaccion.xml)
    class TransaccionViewHolder extends RecyclerView.ViewHolder {
        ImageView iconoCategoria;
        TextView textoCategoria, textoHora, textoMonto;

        public TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoCategoria = itemView.findViewById(R.id.icono_categoria);
            textoCategoria = itemView.findViewById(R.id.texto_categoria);
            textoHora = itemView.findViewById(R.id.texto_hora);
            textoMonto = itemView.findViewById(R.id.texto_monto);
            // TODO: Añadir un setOnClickListener al 'itemView' si quieres que la tarjeta sea clickeable
        }
    }

    // "Caja" para el molde de Fecha (item_fecha_header.xml)
    class FechaViewHolder extends RecyclerView.ViewHolder {
        TextView textoFechaHeader;

        public FechaViewHolder(@NonNull View itemView) {
            super(itemView);
            textoFechaHeader = itemView.findViewById(R.id.texto_fecha_header);
        }
    }


}
