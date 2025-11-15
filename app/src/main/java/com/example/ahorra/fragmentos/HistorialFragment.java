package com.example.ahorra.fragmentos;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

// --- IMPORTS AÑADIDOS PARA PDF y COMPARTIR ---
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
// --- FIN DE IMPORTS AÑADIDOS ---

// Tus imports existentes
import com.example.ahorra.actividades.HistorialAdapter;
import com.example.ahorra.R;
import com.example.ahorra.actividades.Transaccion;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.ahorra.fragmentos.ExportarDialogFragment;

public class HistorialFragment extends Fragment {

    // ... (variables de clase) ...
    private RecyclerView historialRecyclerView;
    private HistorialAdapter historialAdapter;
    private List<Object> listaAgrupada; // <-- Esta lista se usará para el PDF


    public HistorialFragment() {
        // Required empty public constructor
    }

    // ... (tu método newInstance va aquí) ...

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... (tu código de getArguments va aquí) ...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }


    // ========================================================
    // == MÉTODO onViewCreated ACTUALIZADO (Paso 3) ==
    // ========================================================
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. CONFIGURACIÓN DE FILTROS ---
        TextView filtroMes = view.findViewById(R.id.filtro_mes);
        TextView filtroCategoria = view.findViewById(R.id.filtro_categoria);
        TextView filtroTipo = view.findViewById(R.id.filtro_tipo);

        // ... (Tu listener para filtroMes) ...
        // ... (Tu listener para filtroCategoria) ...
        // ... (Tu listener para filtroTipo) ...


        // --- 2. CONFIGURACIÓN DEL RECYCLERVIEW ---
        historialRecyclerView = view.findViewById(R.id.historial_recyclerview);
        historialRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarDatosDeEjemplo(); // Esto llena la 'listaAgrupada'
        historialAdapter = new HistorialAdapter(getContext(), listaAgrupada);
        historialRecyclerView.setAdapter(historialAdapter);


        // ========================================================
        // == LÓGICA DEL FAB MODIFICADA (Paso 3) ==
        // ========================================================

        // 1. Encontrar el FAB
        FloatingActionButton fabExportar = view.findViewById(R.id.fab_exportar);

        // 2. Configurar el "Escuchador" de resultados
        // Esto se ejecutará cuando el diálogo llame a setFragmentResult("requestKey", ...)
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            // Se ejecutó el resultado del diálogo
            String formato = bundle.getString("formato");
            String periodo = bundle.getString("periodo");

            // 3. Llamar a la función de generación
            if (formato.equals("PDF")) {
                Toast.makeText(getContext(), "Generando PDF...", Toast.LENGTH_SHORT).show();
                // Llamamos al nuevo método con el periodo seleccionado
                generarYCompartirPDF(periodo);
            } else {
                Toast.makeText(getContext(), "Generación de Excel no implementada", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Asignar el OnClickListener al FAB (esto solo muestra el diálogo)
        fabExportar.setOnClickListener(v -> {
            ExportarDialogFragment dialogFragment = new ExportarDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "ExportarDialog");
        });
    } // <-- Fin de onViewCreated


    // =======================================================
    // MÉTODO DE DATOS DE EJEMPLO (Sin cambios)
    // =======================================================
    private void cargarDatosDeEjemplo() {
        listaAgrupada = new ArrayList<>();
        List<Transaccion> transaccionesDB = new ArrayList<>();
        // ... (tu código para añadir transacciones a transaccionesDB)
        transaccionesDB.add(new Transaccion("Comida", "10:30 AM", 25.00, "Gasto", R.drawable.ic_comida, new Date(1715781000000L)));
        transaccionesDB.add(new Transaccion("Transporte", "12:45 PM", 5.00, "Gasto", R.drawable.ic_transporte, new Date(1715781000000L)));
        transaccionesDB.add(new Transaccion("Salario", "2:15 PM", 1500.00, "Ingreso", R.drawable.ic_salario, new Date(1715781000000L)));
        transaccionesDB.add(new Transaccion("Compras", "9:00 AM", 120.00, "Gasto", R.drawable.ic_compras, new Date(1715694600000L)));
        transaccionesDB.add(new Transaccion("Entretenimiento", "11:00 AM", 40.00, "Gasto", R.drawable.ic_entretenimiento, new Date(1715694600000L)));

        // --- LÓGICA DE AGRUPACIÓN POR FECHA ---
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'DE' MMMM", new Locale("es", "ES"));
        String fechaActual = "";

        for (Transaccion t : transaccionesDB) {
            String fechaFormateada = sdf.format(t.getFecha()).toUpperCase();
            if (!fechaFormateada.equals(fechaActual)) {
                listaAgrupada.add(fechaFormateada);
                fechaActual = fechaFormateada;
            }
            listaAgrupada.add(t);
        }
    }


    // ========================================================
    // == MÉTODOS NUEVOS PARA GENERAR PDF (Paso 3) ==
    // ========================================================

    /**
     * Crea un PDF con los datos de 'listaAgrupada' y lo comparte.
     */
    private void generarYCompartirPDF(String periodo) {
        // 1. Crear un documento PDF
        PdfDocument documento = new PdfDocument();

        // 2. Definir el tamaño de la página (A4) y crearla
        int anchoPagina = 595;
        int altoPagina = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(anchoPagina, altoPagina, 1).create();
        PdfDocument.Page pagina = documento.startPage(pageInfo);

        // 3. Obtener el "lienzo" (Canvas) para dibujar
        Canvas canvas = pagina.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        // 4. "Dibujar" el contenido
        int x = 40; // Margen izquierdo
        int y = 60; // Margen superior

        // Título
        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        canvas.drawText("Reporte Financiero (" + periodo + ")", x, y, paint);
        y += 40; // Siguiente línea

        // Contenido de la lista
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);

        // Usamos la lista 'listaAgrupada' que ya está cargada en el fragmento
        for (Object item : listaAgrupada) {

            // Si llegamos al final de la página, creamos una nueva
            if (y > (altoPagina - 40)) {
                documento.finishPage(pagina);
                pagina = documento.startPage(pageInfo);
                canvas = pagina.getCanvas();
                y = 60;
            }

            if (item instanceof String) {
                // Es un cabezal de fecha (ej. "15 DE MAYO")
                paint.setFakeBoldText(true);
                canvas.drawText((String) item, x, y, paint);
                y += 20;
                paint.setFakeBoldText(false);

            } else if (item instanceof Transaccion) {
                // Es una transacción
                Transaccion t = (Transaccion) item;
                String monto = String.format(Locale.US, "%.2f", t.getMonto());
                String linea = t.getCategoria() + " (" + t.getHora() + ")";

                // Dibujar texto de la transacción
                canvas.drawText(linea, x + 10, y, paint);

                // Dibujar el monto (rojo o verde) a la derecha
                if (t.getTipo().equals("Gasto")) {
                    paint.setColor(Color.RED);
                    canvas.drawText("-S/ " + monto, anchoPagina - 100, y, paint);
                } else {
                    paint.setColor(Color.rgb(0, 100, 0)); // Verde oscuro
                    canvas.drawText("+S/ " + monto, anchoPagina - 100, y, paint);
                }

                paint.setColor(Color.BLACK); // Resetear color
                y += 20; // Siguiente línea
            }
        }

        // 5. Terminar la página y el documento
        documento.finishPage(pagina);

        // 6. Guardar el archivo en el 'cache' de la app
        // (Usamos getContext() porque estamos en un Fragment)
        File archivoPDF = new File(getContext().getCacheDir(), "reporte_financiero.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(archivoPDF);
            documento.writeTo(fos);
            documento.close();
            fos.close();

            // 7. Si se guardó bien, compartirlo
            compartirArchivo(archivoPDF);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Abre el menú de compartir de Android para un archivo específico.
     */
    private void compartirArchivo(File archivo) {
        // 1. Obtener la URI segura usando el FileProvider (que configuramos en el Manifest)
        Uri uriDelArchivo = FileProvider.getUriForFile(
                getContext(),
                // Esta autoridad debe coincidir con la del AndroidManifest.xml
                getContext().getApplicationContext().getPackageName() + ".provider",
                archivo
        );

        // 2. Crear el Intent para "Enviar"
        Intent intentCompartir = new Intent(Intent.ACTION_SEND);
        intentCompartir.setType("application/pdf");
        intentCompartir.putExtra(Intent.EXTRA_STREAM, uriDelArchivo);

        // 3. Dar permiso temporal al receptor para leer la URI
        intentCompartir.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 4. Mostrar el menú de compartir de Android
        startActivity(Intent.createChooser(intentCompartir, "Compartir Reporte..."));
    }

} // <-- Fin de la clase HistorialFragment