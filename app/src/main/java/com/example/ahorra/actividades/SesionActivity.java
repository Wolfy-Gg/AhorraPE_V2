package com.example.ahorra.actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ahorra.R;
import com.example.ahorra.api.RetrofitClient;
import com.example.ahorra.modelos.LoginRequest;
import com.example.ahorra.modelos.LoginResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SesionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SESION_API";

    // Constantes para SharedPreferences
    private static final String PREFS_NAME = "AhorraPrefs";
    private static final String KEY_USER_ID = "idUsuarioLogeado";

    // 1. Declaración de vistas
    EditText txtCorreo, txtClave;
    CheckBox chkRecordar;
    Button btnIngresar, btnSalir;
    TextView lblRegistrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. VERIFICACIÓN DE SESIÓN EXISTENTE (Auto-Login)
        if (isUserLoggedIn()) {
            // Si el usuario ya está logeado, navega directamente al Home y sale
            SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String nombreUsuario = sharedPrefs.getString("nombreUsuario", "Usuario");

            navegarAlHome(nombreUsuario);
            return;
        }

        // Si no está logeado, se muestra la pantalla de login
        setContentView(R.layout.activity_sesion);

        // 3. Inicialización de vistas
        txtCorreo = findViewById(R.id.sesTxtCorreo);
        txtClave = findViewById(R.id.sesTxtClave);
        chkRecordar = findViewById(R.id.sesChkRecordar);
        btnIngresar = findViewById(R.id.sesBtnIngresar);
        btnSalir = findViewById(R.id.sesBtnSalir);
        lblRegistrate = findViewById(R.id.sesLblRegistro);

        // 4. Asignar Listeners
        btnIngresar.setOnClickListener(this);
        btnSalir.setOnClickListener(this);
        lblRegistrate.setOnClickListener(this);
    }

    /**
     * Verifica si existe una sesión válida guardada en SharedPreferences.
     */
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // El ID de usuario por defecto es -1 si no se ha guardado nada.
        int id = sharedPrefs.getInt(KEY_USER_ID, -1);
        boolean loggedInFlag = sharedPrefs.getBoolean("isLoggedIn", false);

        return id != -1 && loggedInFlag;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.sesBtnIngresar) {
            String correo = txtCorreo.getText().toString().trim();
            String clave = txtClave.getText().toString();

            if (correo.isEmpty() || clave.isEmpty()) {
                mostrarMensaje("Por favor, ingresa tu correo y clave.");
                return;
            }

            iniciarSesion(correo, clave);
        }
        else if (id == R.id.sesBtnSalir) {
            salir();
        }
        else if (id == R.id.sesLblRegistro) {
            // Navegar a RegistroActivity
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(SesionActivity.this, mensaje, Toast.LENGTH_LONG).show();
    }

    /**
     * Guarda la información esencial de la sesión en SharedPreferences.
     */
    private void guardarSesion(int idUsuario, String nombreUsuario) {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (idUsuario > 0) {
            editor.putInt(KEY_USER_ID, idUsuario);
            editor.putString("nombreUsuario", nombreUsuario);
            editor.putBoolean("isLoggedIn", true);
        } else {
            editor.putBoolean("isLoggedIn", false);
        }

        editor.apply();
        Log.d(TAG, "Sesión de usuario ID: " + idUsuario + " guardada.");
    }

    private void navegarAlHome(String nombreUsuario) {
        // Navega a la pantalla principal (PrincipalActivity.java)
        Intent principal = new Intent(SesionActivity.this, PrincipalActivity.class);
        principal.putExtra("NOMBRE_USUARIO", nombreUsuario);
        // Flags para asegurar que no se pueda volver a la pantalla de login con el botón "atrás"
        principal.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(principal);
    }

    private void iniciarSesion(String correo, String clave) {

        LoginRequest loginRequest = new LoginRequest(correo, clave);

        RetrofitClient.INSTANCE.getInstance().iniciarSesion(loginRequest)
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        // 🛑 CORRECCIÓN: Usamos response.isSuccessful() para garantizar que Retrofit
                        // ha podido parsear el cuerpo como LoginResponse (código 2xx).
                        if (response.isSuccessful()) {
                            // 🚀 CÓDIGO 200: RESPUESTA VÁLIDA (Login exitoso o fallido por credenciales) 🚀
                            LoginResponse respuesta = response.body();

                            if (respuesta != null && respuesta.getExito() != null && respuesta.getExito()) {

                                // El servidor confirmó éxito
                                int idUsuario = respuesta.getIdUsuario() != null ? respuesta.getIdUsuario() : -1;
                                String nombreUsuario = respuesta.getNombreUsuario() != null ? respuesta.getNombreUsuario() : "Usuario";

                                // Guardamos la sesión
                                guardarSesion(idUsuario, nombreUsuario);

                                mostrarMensaje("¡Bienvenido, " + nombreUsuario + "!");
                                navegarAlHome(nombreUsuario);
                            } else {
                                // El servidor respondió 200 OK, pero envió un error de credenciales (exito: false)
                                String errorMsg = respuesta != null && respuesta.getError() != null ? respuesta.getError() : "Inicio de sesión fallido. Credenciales incorrectas.";
                                mostrarMensaje(errorMsg);
                            }
                        } else {
                            // 🛑 CÓDIGOS DE ERROR HTTP (4xx, 5xx) 🛑
                            // Simplificamos el manejo de errores para evitar el crash de Gson al recibir HTML.
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Cuerpo de error no disponible.";

                                Log.e(TAG, "Error HTTP " + response.code() + ": " + errorBody);

                                // Ya no intentamos parsear el errorBody con Gson, ya que podría ser HTML.
                                mostrarMensaje("Error del servidor (Código " + response.code() + "). Verifica la URL.");


                            } catch (IOException e) {
                                Log.e(TAG, "Error leyendo cuerpo de error", e);
                                mostrarMensaje("Error de comunicación. Inténtalo más tarde.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        // ❌ FALLO DE CONEXIÓN DE RED ❌
                        mostrarMensaje("Fallo de conexión. Verifica tu internet.");
                        Log.e(TAG, "Fallo de conexión: " + t.getMessage(), t);
                    }
                });
    }

    private void salir() {
        finishAffinity(); // Cierra todas las actividades y sale de la aplicación
    }
}
