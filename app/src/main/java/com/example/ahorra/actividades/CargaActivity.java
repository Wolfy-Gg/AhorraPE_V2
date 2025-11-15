package com.example.ahorra.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ahorra.R;

public class CargaActivity extends AppCompatActivity {
    ProgressBar barCarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carga);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        barCarga = findViewById(R.id.carBarInicio);

        Thread carga = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <=barCarga.getMax();i++){
                    barCarga.setProgress(i);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Intent sesion = new Intent(getApplicationContext(), SesionActivity.class);
                startActivity(sesion);
            }
        });
        carga.start();
    }
}