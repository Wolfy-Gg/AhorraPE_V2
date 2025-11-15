package com.example.ahorra.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ahorra.R;
import com.google.android.material.appbar.MaterialToolbar; // <-- AÑADIR IMPORT

public class LeccionFragment extends Fragment {

    // ... (Tu constructor, newInstance, onCreate, etc. irían aquí)

    public LeccionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leccion, container, false);
    }

    // ========================================================
    // == PASO 3: AÑADIDO EL MÉTODO PARA LA FLECHA DE "ATRÁS" ==
    // ========================================================
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Encontrar la Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_leccion);

        // 2. Asignar el OnClickListener a la flecha
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Simular el clic en el botón "atrás" del sistema
                // (Esto saca a LeccionFragment y regresa a AprenderFragment)
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // (Aquí puedes añadir la lógica para el botón de guardar/marcador)
    }
}