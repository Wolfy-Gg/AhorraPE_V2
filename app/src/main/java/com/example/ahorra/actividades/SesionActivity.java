package com.example.ahorra.actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings; // Import para los ajustes de la huella
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton; // Import para el botón de huella
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import para biometría
import androidx.appcompat.app.AppCompatActivity;

// --- Imports Nuevos para Biometría ---
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
// --- Fin de Imports Nuevos ---

import com.example.ahorra.R;
import com.example.ahorra.api.RetrofitClient;
import com.example.ahorra.modelos.LoginRequest;
import com.example.ahorra.modelos.LoginResponse;

import java.io.IOException;
import java.util.concurrent.Executor; // Import para biometría

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
    Button btnIngresar;
    // Button btnSalir; // ELIMINADO: Este botón ya no existe en tu layout
    TextView lblRegistrate;

    // --- Vistas y Variables Nuevas para Biometría ---
    private ImageButton btnIngresarHuella;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    // --- Fin de Biometría ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MODIFICACIÓN: Siempre mostramos la pantalla de login
        setContentView(R.layout.activity_sesion);

        // 3. Inicialización de vistas
        txtCorreo = findViewById(R.id.sesTxtCorreo);
        txtClave = findViewById(R.id.sesTxtClave);
        chkRecordar = findViewById(R.id.sesChkRecordar);
        btnIngresar = findViewById(R.id.sesBtnIngresar);
        // btnSalir = findViewById(R.id.sesBtnSalir); // ELIMINADO: Esto causaba un crash
        lblRegistrate = findViewById(R.id.sesLblRegistro);

        // AÑADIDO: Vinculación del botón de huella
        btnIngresarHuella = findViewById(R.id.btn_ingresar_huella);

        // 4. Asignar Listeners
        btnIngresar.setOnClickListener(this);
        // btnSalir.setOnClickListener(this); // ELIMINADO
        lblRegistrate.setOnClickListener(this);
        btnIngresarHuella.setOnClickListener(this); // AÑADIDO


        // --- INICIO: Configuración de Biometría ---
        // Explicación: Preparamos todo lo necesario para mostrar el pop-up de huella.

        executor = ContextCompat.getMainExecutor(this);

        // 2. Definimos qué pasa cuando la autenticación es exitosa, fallida o da error
        biometricPrompt = new BiometricPrompt(SesionActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Si el usuario cancela (error 10 o 13), no mostramos nada.
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    mostrarMensaje(getString(R.string.bio_error, errString));
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // ¡ÉXITO! La huella fue reconocida.
                // Buscamos el nombre guardado en SharedPreferences y navegamos al Home
                SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String nombreUsuario = sharedPrefs.getString("nombreUsuario", "Usuario");

                mostrarMensaje(getString(R.string.bio_exito) + ", ¡Bienvenido " + nombreUsuario + "!");
                navegarAlHome(nombreUsuario);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Falló. La huella no fue reconocida.
                mostrarMensaje(getString(R.string.bio_fallo));
            }
        });

        // 3. Configurar el diálogo (el pop-up) que verá el usuario
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.bio_titulo))
                .setSubtitle(getString(R.string.bio_subtitulo))
                .setNegativeButtonText(getString(R.string.bio_boton_negativo)) // Botón para cancelar
                .build();

        // --- FIN: Configuración de Biometría ---


        // 5. MODIFICACIÓN: Lógica de Auto-Login
        // En lugar de navegar directo, intentamos mostrar la huella automáticamente
        // si el usuario ya tiene una sesión válida.

        // Revisa si el dispositivo puede usar huella
        BiometricManager biometricManager = BiometricManager.from(this);

        if (isUserLoggedIn() && biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            // Si el usuario TIENE sesión Y TIENE huella...
            // Mostramos el pop-up de huella automáticamente.
            biometricPrompt.authenticate(promptInfo);
        }

        // Si isUserLoggedIn() es falso, o no tiene huella,
        // simplemente se queda en la pantalla de login,
        // esperando que ingrese contraseña o toque el botón de huella.
    }

    /**
     * Verifica si existe una sesión válida guardada en SharedPreferences.
     * (Tu método original, sin cambios)
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
        // else if (id == R.id.sesBtnSalir) { // ELIMINADO
        //     salir();
        // }
        else if (id == R.id.sesLblRegistro) {
            // Navegar a RegistroActivity
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.btn_ingresar_huella) { // AÑADIDO
            // El usuario toca el botón de huella manualmente
            checkBiometricSupport();
        }
    }


    /**
     * MÉTODO NUEVO:
     * Revisa si el dispositivo tiene sensor de huella Y si el usuario
     * ya ha iniciado sesión antes (para poder usar la huella).
     */
    private void checkBiometricSupport() {

        // 1. Verificación de Seguridad:
        // ¿El usuario ya tiene una sesión guardada de antes?
        if (!isUserLoggedIn()) {
            // Si no tiene sesión, no podemos loguearlo con huella.
            // (Necesitamos los strings del Paso 5 que te di antes)
            mostrarMensaje(getString(R.string.bio_error_no_sesion));
            return;
        }

        // 2. Verificación de Hardware:
        // Si SÍ tiene sesión, revisamos si el teléfono tiene el hardware
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {

            // Éxito: El dispositivo tiene sensor Y huellas registradas
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Muestra el diálogo de huella
                biometricPrompt.authenticate(promptInfo);
                break;

            // Error: No hay sensor
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                mostrarMensaje(getString(R.string.bio_error_no_hardware));
                break;

            // Error: El sensor está disponible, pero no hay huellas registradas
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                mostrarMensaje(getString(R.string.bio_error_no_registradas));

                // Opcional: Llévalo a los Ajustes
                // final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                // enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                //        BiometricManager.Authenticators.BIOMETRIC_STRONG);
                // startActivity(enrollIntent);
                break;

            // Otros errores
            default:
                mostrarMensaje("Función biométrica no disponible");
                break;
        }
    }


    // --- EL RESTO DE TUS MÉTODOS (SIN CAMBIOS) ---

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(SesionActivity.this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void guardarSesion(int idUsuario, String nombreUsuario) {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (idUsuario > 0) {
            editor.putInt(KEY_USER_ID, idUsuario);
            editor.putString("nombreUsuario", nombreUsuario);

            // IMPORTANTE: Marcamos la sesión como logueada
            // solo si el CheckBox "Recordar" está marcado.
            editor.putBoolean("isLoggedIn", chkRecordar.isChecked());
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
                        if (response.isSuccessful()) {
                            LoginResponse respuesta = response.body();

                            if (respuesta != null && respuesta.getExito() != null && respuesta.getExito()) {

                                int idUsuario = respuesta.getIdUsuario() != null ? respuesta.getIdUsuario() : -1;
                                String nombreUsuario = respuesta.getNombreUsuario() != null ? respuesta.getNombreUsuario() : "Usuario";

                                // Guardamos la sesión
                                guardarSesion(idUsuario, nombreUsuario);

                                mostrarMensaje("¡Bienvenido, " + nombreUsuario + "!");
                                navegarAlHome(nombreUsuario);
                            } else {
                                String errorMsg = respuesta != null && respuesta.getError() != null ? respuesta.getError() : "Inicio de sesión fallido. Credenciales incorrectas.";
                                mostrarMensaje(errorMsg);
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Cuerpo de error no disponible.";
                                Log.e(TAG, "Error HTTP " + response.code() + ": " + errorBody);
                                mostrarMensaje("Error del servidor (Código " + response.code() + "). Verifica la URL.");
                            } catch (IOException e) {
                                Log.e(TAG, "Error leyendo cuerpo de error", e);
                                mostrarMensaje("Error de comunicación. Inténtalo más tarde.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        mostrarMensaje("Fallo de conexión. Verifica tu internet.");
                        Log.e(TAG, "Fallo de conexión: " + t.getMessage(), t);
                    }
                });
    }

    private void salir() {
        finishAffinity(); // Cierra todas las actividades y sale de la aplicación
    }
}