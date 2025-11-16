package com.example.ahorra.actividades;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// --- IMPORTS ADICIONALES PARA LA SESIÓN Y BOTÓN ---
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.ahorra.R;
import com.example.ahorra.clases.Menu;
import com.example.ahorra.fragmentos.AprenderFragment;
import com.example.ahorra.fragmentos.HistorialFragment;
import com.example.ahorra.fragmentos.LeccionFragment;
import com.example.ahorra.fragmentos.MapasFragment;
import com.example.ahorra.fragmentos.MetasFragment;
import com.example.ahorra.fragmentos.Pantalla_PrincipalFragment;
import com.example.ahorra.fragmentos.PerfilFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
// --- FIN DE IMPORTS ADICIONALES ---


// La clase implementa Menu (interfaz de navegación) y OnAprenderInteractionListener (para ir a lecciones)
public class PrincipalActivity extends AppCompatActivity
        implements Menu, AprenderFragment.OnAprenderInteractionListener {

    // Constantes para SharedPreferences (deben coincidir con SesionActivity)
    private static final String PREFS_NAME = "AhorraPrefs";
    private static final String KEY_USER_ID = "idUsuarioLogeado";

    // Array de fragments para la navegación
    Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ========================================================
        // == 1. Manejo de Sesión (Recibir datos de SesionActivity) ==
        // ========================================================

        // El nombre de usuario se pasa desde SesionActivity
        String nombreUsuario = getIntent().getStringExtra("NOMBRE_USUARIO");
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            Toast.makeText(this, "Bienvenido, " + nombreUsuario + "!", Toast.LENGTH_SHORT).show();
            Log.d("PRINCIPAL_ACT", "Usuario logeado: " + nombreUsuario);
        }

        // ========================================================
        // == 2. Configuración y Carga Inicial de Fragments ==
        // ========================================================

        // prepara fragments
        fragments = new Fragment[6];
        fragments[0] = new Pantalla_PrincipalFragment(); // Home/Dashboard
        fragments[1] = new MetasFragment();
        fragments[2] = new AprenderFragment();
        fragments[3] = new HistorialFragment();
        fragments[4] = new PerfilFragment();
        fragments[5] = new MapasFragment(); // Contiene la opción de cerrar sesión

        int id = getIntent().getIntExtra("id", 0);
        onClickMenu(id); // Carga el fragment inicial (0 es el predeterminado si no se pasa 'id')


        // ========================================================
        // == 3. CÓDIGO DEL BOTÓN FLOTANTE (FAB) ==
        // ========================================================
        final int marginDp = 50;

        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics());


        FloatingActionButton fab = new FloatingActionButton(this);

        // Se genera un ID único para el FAB
        fab.setId(ViewCompat.generateViewId());

        fab.setImageResource(android.R.drawable.ic_input_add); // icono "+" incorporado
        fab.setContentDescription("Abrir registro de gastos");

        // Definir parámetros de layout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.END
        );
        params.setMargins(marginPx, marginPx, marginPx, marginPx);

        // Añadir el FAB a la vista raíz de la *ventana*
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        rootView.addView(fab, params);

        // Asignar el OnClickListener para registrar un gasto
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrincipalActivity.this, com.example.ahorra.actividades.RegistroGasActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        // ========================================================
        // == FIN DEL CÓDIGO DEL FAB ==
        // ========================================================
    }


    /**
     * Implementación del método de la interfaz Menu.
     * Se usa para cambiar el Fragment principal.
     */
    @Override
    public void onClickMenu(int id) {
        if (id >= 0 && id < fragments.length) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            // Asegúrate de que tu layout tiene un FrameLayout o ContstraintLayout con ID: R.id.relContendor
            ft.replace(R.id.relContendor, fragments[id]);
            ft.commit();
        } else {
            Log.e("NAV_ERROR", "ID de Fragmento fuera de rango: " + id);
        }
    }

    /**
     * Implementación del método de la interfaz AprenderFragment.OnAprenderInteractionListener.
     * Navega a la vista de una lección específica.
     */
    @Override
    public void onModulo1Clicked() {
        LeccionFragment leccionFragment = new LeccionFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.relContendor, leccionFragment);
        ft.addToBackStack(null); // Permite al usuario volver al fragment Aprender
        ft.commit();
    }

    /**
     * Método público para limpiar la sesión y regresar a la pantalla de Login.
     * Es llamado principalmente desde PerfilFragment.
     */
    public void logout() {
        // Borrar datos de SharedPreferences
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.remove(KEY_USER_ID);
        editor.remove("nombreUsuario");
        editor.putBoolean("isLoggedIn", false);

        editor.apply();

        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();

        // Navegar a SesionActivity y limpiar la pila
        Intent login = new Intent(this, SesionActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
        finish();
    }
}
