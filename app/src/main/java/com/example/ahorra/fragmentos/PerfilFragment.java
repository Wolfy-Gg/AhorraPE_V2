package com.example.ahorra.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ahorra.R;
import com.example.ahorra.api.RetrofitClient;
import com.example.ahorra.actividades.PrincipalActivity;
import com.example.ahorra.actividades.SesionActivity; // Importado para el logout
import com.example.ahorra.modelos.PerfilResponse; // Importar el nuevo modelo

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private static final String PREFS_NAME = "AhorraPrefs";
    private static final String KEY_USER_ID = "idUsuarioLogeado";
    private static final String TAG = "PERFIL_API";

    // Vistas a actualizar
    private TextView lblNombreUsuario;
    private TextView lblEmailUsuario;
    private TextView lblSaldoTotal; // Asumo que añades un TextView para el saldo en el XML
    private Button btnCerrarSesion;

    public PerfilFragment() {
        // Constructor público vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // 1. Inicializar Vistas
        lblNombreUsuario = view.findViewById(R.id.lblNombreUsuario);
        lblEmailUsuario = view.findViewById(R.id.lblEmailUsuario);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        // **IMPORTANTE**: Asegúrate de añadir un TextView con este ID en fragment_perfil.xml
        // lblSaldoTotal = view.findViewById(R.id.lblSaldoTotal);

        // 2. Cargar datos del perfil de la API
        cargarDatosPerfil();

        // 3. Lógica para el botón de cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Aquí podrías añadir listeners para el resto de opciones de configuración

        return view;
    }

    private void cargarDatosPerfil() {
        Context context = getContext();
        if (context == null) return;

        // 1. Obtener el ID de usuario de SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        // Convertimos el ID a String para enviarlo como 'Authorization' header
        final String authHeader = String.valueOf(userId);

        if (userId == -1) {
            mostrarMensaje("Error: No hay una sesión activa. Redirigiendo a Login.");
            cerrarSesion(); // Forzar logout si no hay ID
            return;
        }

        // 2. Llamada a Retrofit
        RetrofitClient.INSTANCE.getInstance().obtenerPerfil(authHeader)
                .enqueue(new Callback<PerfilResponse>() {
                    @Override
                    public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                        if (response.isSuccessful()) {
                            PerfilResponse perfil = response.body();
                            if (perfil != null) {
                                // 3. Actualizar la interfaz con los datos reales
                                lblNombreUsuario.setText(perfil.getNombre());
                                lblEmailUsuario.setText(perfil.getEmail());

                                // Formatear el saldo como moneda (ej. S/. 1,234.50)
                                /*
                                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "PE")); // Perú
                                if (lblSaldoTotal != null) {
                                    lblSaldoTotal.setText(format.format(perfil.getSaldoTotal()));
                                }
                                */

                                // 4. Opcional: Actualizar el nombre en SharedPreferences (por si cambió)
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("nombreUsuario", perfil.getNombre());
                                editor.apply();

                            } else {
                                mostrarMensaje("Error al procesar los datos del perfil.");
                            }
                        } else {
                            mostrarMensaje("Error de la API: Código " + response.code());
                            Log.e(TAG, "Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<PerfilResponse> call, Throwable t) {
                        mostrarMensaje("Fallo de red: No se pudo conectar al servidor.");
                        Log.e(TAG, "Fallo: " + t.getMessage());
                    }
                });
    }

    private void cerrarSesion() {
        Context context = getContext();
        if (context == null) return;

        // 1. LIMPIAR DATOS DE SESIÓN
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // 2. NAVEGAR A LA PANTALLA DE SESIÓN (LOGIN)
        Intent intent = new Intent(context, SesionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finaliza la Activity principal que contiene este Fragmento
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }
}