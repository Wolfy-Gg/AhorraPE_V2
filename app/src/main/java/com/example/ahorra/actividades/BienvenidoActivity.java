package com.example.ahorra.actividades;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.TypedValue;
import android.view.View;
import androidx.core.view.ViewCompat;

import com.example.ahorra.R;
import com.example.ahorra.clases.Menu;

public class BienvenidoActivity extends AppCompatActivity implements Menu {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bienvenido);
        final int marginDp = 16; // margin en dp desde borde
        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics());


        FloatingActionButton fab = new FloatingActionButton(this);


        fab.setId(ViewCompat.generateViewId());

        fab.setImageResource(android.R.drawable.ic_input_add); // icono incorporado
        fab.setContentDescription("Abrir registro de gastos");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END
        );
        params.setMargins(marginPx, marginPx, marginPx, marginPx);
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);

        rootView.addView(fab, params);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FAB", "FAB clickeado: abriendo RegistroGasActivity");
                Intent intent = new Intent(BienvenidoActivity.this, com.example.ahorra.actividades.RegistroGasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public void onClickMenu(int id) {
        Intent principal = new Intent(this, PrincipalActivity.class);
        principal.putExtra("id", id);
        startActivity(principal);
        finish();
    }
}