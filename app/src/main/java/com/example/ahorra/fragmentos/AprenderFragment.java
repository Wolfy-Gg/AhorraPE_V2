package com.example.ahorra.fragmentos;

import android.content.Context; // <-- AÑADIDO
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // <-- AÑADIDO

import com.example.ahorra.R;

public class AprenderFragment extends Fragment {

    // --- 1. CREAR LA INTERFAZ DE COMUNICACIÓN ---
    // Esta es la "señal" que enviará a la PrincipalActivity
    public interface OnAprenderInteractionListener {
        void onModulo1Clicked();
        // (Aquí puedes añadir onModulo2Clicked(), onModulo3Clicked()...)
    }

    // Variable para "apuntar" a la PrincipalActivity
    private OnAprenderInteractionListener mListener;
    // ------------------------------------------------

    // ... (Tu código de newInstance, mParam, etc. puede ir aquí) ...

    public AprenderFragment() {
        // Required empty public constructor
    }

    // ... (Tu método newInstance) ...

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ... (Tu código de getArguments) ...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aprender, container, false);
    }

    // --- 2. AÑADIR onViewCreated PARA LOS CLICS ---
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Encontrar los botones
        ImageButton imgbtModulo1 = view.findViewById(R.id.imgbtModulo1);
        ImageButton imgbtModulo2 = view.findViewById(R.id.imgbtModulo2);
        ImageButton imgbtModulo3 = view.findViewById(R.id.imgbtModulo3);

        imgbtModulo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Enviar la señal (si el listener está conectado)
                if (mListener != null) {
                    mListener.onModulo1Clicked();
                }
            }
        });

        // (Listeners para los otros botones, por ahora hacen lo mismo)
        imgbtModulo2.setOnClickListener(v -> {
            if (mListener != null) mListener.onModulo1Clicked();
        });
        imgbtModulo3.setOnClickListener(v -> {
            if (mListener != null) mListener.onModulo1Clicked();
        });
    }

    // --- 4. "CONECTAR" LA INTERFAZ A LA ACTIVIDAD ---
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Esto comprueba que PrincipalActivity haya implementado la interfaz
        if (context instanceof OnAprenderInteractionListener) {
            mListener = (OnAprenderInteractionListener) context;
        } else {
            // Si olvidas el Paso 2, la app crasheará aquí (lo cual es bueno)
            throw new RuntimeException(context.toString()
                    + " debe implementar OnAprenderInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}