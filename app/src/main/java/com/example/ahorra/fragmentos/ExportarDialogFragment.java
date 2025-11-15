package com.example.ahorra.fragmentos;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast; // <-- Ya no lo usamos en el botón, pero lo dejamos por si acaso

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ahorra.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExportarDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout que creamos en el Paso 3
        View view = inflater.inflate(R.layout.dialog_exportar, container, false);
        return view;
    }

    // ========================================================
    // == MÉTODO onViewCreated ACTUALIZADO (Paso 2) ==
    // ========================================================
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Configurar el Dropdown de Periodo ---
        AutoCompleteTextView dropdownPeriodo = view.findViewById(R.id.dropdown_periodo);

        // Opciones que aparecerán en el dropdown (usando tu lista)
        String[] periodos = {"Este Mes", "Mes Pasado", "Este Año", "Año Pasado", "Todos"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                periodos
        );
        dropdownPeriodo.setAdapter(adapter);

        // --- Encontrar los otros componentes ---
        RadioGroup radioGroup = view.findViewById(R.id.radio_group_formato);
        Button btnGenerar = view.findViewById(R.id.btn_generar_y_compartir);

        // --- Configurar el Click del Botón "Generar" (ESTA ES LA LÓGICA MODIFICADA) ---
        btnGenerar.setOnClickListener(v -> {

            // 1. Obtener los valores seleccionados
            String periodo = dropdownPeriodo.getText().toString();
            int radioId = radioGroup.getCheckedRadioButtonId();

            String formato;
            if (radioId == R.id.radio_pdf) {
                formato = "PDF";
            } else {
                formato = "Excel";
            }

            // 2. Preparar el resultado para enviarlo de vuelta
            Bundle result = new Bundle();
            result.putString("formato", formato);
            result.putString("periodo", periodo);

            // 3. Enviar el resultado al Fragmento "padre" (HistorialFragment)
            getParentFragmentManager().setFragmentResult("requestKey", result);

            // 4. Cerrar el diálogo
            dismiss();
        });
    }
}