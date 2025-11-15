package com.example.ahorra.actividades;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter; // Importación necesaria para el Spinner
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ahorra.R;
import com.example.ahorra.api.RetrofitClient;
import com.example.ahorra.modelos.RegistroRequest;
import com.example.ahorra.modelos.RegistroResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

    // 1. Declaración de vistas
    private EditText txtDni, txtNombre, txtApellido, txtEmail, txtClave, txtConfirmarClave, txtFechaNacimiento;
    private RadioGroup rgSexo;
    private CheckBox chkAceptoTerminos;
    private Button btnRegistrar, btnCamara, btnRegresar;
    private Spinner cboDistrito;

    // Constantes
    private static final int ID_MONEDA_DEFAULT = 1;
    private static final int ID_DISTRITO_DEFAULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 2. Inicializar componentes
        txtDni = findViewById(R.id.regTxtDni);
        txtNombre = findViewById(R.id.regTxtNombre);
        txtApellido = findViewById(R.id.regTxtApellido);
        txtEmail = findViewById(R.id.regTxtCorreo);
        txtClave = findViewById(R.id.regTxtClave);
        txtConfirmarClave = findViewById(R.id.regTxtClave2);
        txtFechaNacimiento = findViewById(R.id.regTxtFechaNac);

        rgSexo = findViewById(R.id.regGrpSesp);
        chkAceptoTerminos = findViewById(R.id.regChkTerminos);
        cboDistrito = findViewById(R.id.regCboDistrito);

        btnRegistrar = findViewById(R.id.regBtnCrear);
        btnCamara = findViewById(R.id.regBtnCamara);
        btnRegresar = findViewById(R.id.regBtnRegresar);

        // --- NUEVA LÓGICA DE INTERACCIÓN ---

        // 3. Cargar datos en el Spinner (temporalmente con un array de strings)
        cargarDistritosDummy();

        // 4. Asignar Listeners
        btnRegistrar.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);
        // El EditText de fecha debe abrir el DatePicker
        txtFechaNacimiento.setOnClickListener(this);
    }

    /**
     * Carga una lista dummy de distritos en el Spinner para que sea funcional.
     * En una app real, esto se cargaría desde la API.
     */
    private void cargarDistritosDummy() {
        // Lista temporal de distritos (reemplazar con datos reales de la API en el futuro)
        String[] distritos = new String[]{"Seleccione Distrito...", "Lima", "Miraflores", "San Isidro", "Barranco"};

        // Crear el adaptador usando un layout simple
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                distritos
        );

        // Asignar el adaptador al Spinner
        cboDistrito.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.regBtnCrear) {
            iniciarRegistro();
        } else if (v.getId() == R.id.regBtnRegresar) {
            Intent intent = new Intent(RegistroActivity.this, SesionActivity.class);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.regTxtFechaNac) {
            // Mostrar el selector de fecha
            mostrarDatePickerDialog();
        }
    }

    /**
     * Muestra el diálogo para seleccionar la fecha de nacimiento.
     * La fecha se guardará en formato YYYY-MM-DD en el EditText.
     */
    private void mostrarDatePickerDialog() {
        // Usa la fecha actual como valor por defecto
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, monthOfYear, dayOfMonth) -> {
                    // Formato de fecha para la API: YYYY-MM-DD
                    String date = String.format(Locale.US, "%d-%02d-%02d", selectedYear, monthOfYear + 1, dayOfMonth);
                    txtFechaNacimiento.setText(date);
                }, year, month, day);

        // Opcional: Establecer una fecha máxima (ej. no permitir fechas futuras)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    // Método para obtener el código de Sexo que la DB necesita ('M', 'F', 'N')
    private String getSelectedSexoCode() {
        int selectedId = rgSexo.getCheckedRadioButtonId();

        if (selectedId == R.id.regRbtMasculino) return "M";
        if (selectedId == R.id.regRbtFemenino) return "F";
        if (selectedId == R.id.regRbtNoDefinido) return "N";

        return "N";
    }

    private int getSelectedDistritoId() {
        // En este punto, solo devolveremos el ID 1 ya que el Spinner es dummy.
        // Cuando se integre la API, se obtendrá el ID real del elemento seleccionado.
        return ID_DISTRITO_DEFAULT;
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_LONG).show();
    }

    private boolean validarCampos() {
        String clave = txtClave.getText().toString();
        String confirmacion = txtConfirmarClave.getText().toString();

        // Validación específica para el Spinner de Distrito
        if (cboDistrito.getSelectedItemPosition() == 0) {
            mostrarMensaje("Por favor, seleccione un distrito válido.");
            return false;
        }

        if (clave.length() < 8) {
            mostrarMensaje("La clave debe tener mínimo 8 caracteres.");
            return false;
        }
        if (!clave.equals(confirmacion)) {
            mostrarMensaje("La clave y la confirmación no coinciden.");
            return false;
        }
        if (rgSexo.getCheckedRadioButtonId() == -1) {
            mostrarMensaje("Por favor, seleccione su sexo.");
            return false;
        }
        if (!chkAceptoTerminos.isChecked()) {
            mostrarMensaje("Debe aceptar los Términos y condiciones.");
            return false;
        }
        // Agregamos la validación para la fecha de nacimiento
        if (txtDni.getText().toString().isEmpty() ||
                txtNombre.getText().toString().isEmpty() ||
                txtApellido.getText().toString().isEmpty() || // Agregando validación para Apellido
                txtEmail.getText().toString().isEmpty() ||
                txtFechaNacimiento.getText().toString().isEmpty()) {
            mostrarMensaje("Por favor, complete todos los campos requeridos.");
            return false;
        }
        return true;
    }


    private void iniciarRegistro() {
        if (!validarCampos()) return;

        String dni = txtDni.getText().toString();
        String nombre = txtNombre.getText().toString();
        String apellido = txtApellido.getText().toString();
        String email = txtEmail.getText().toString();
        String contrasena = txtClave.getText().toString();
        String fechaNacimiento = txtFechaNacimiento.getText().toString();
        String sexo = getSelectedSexoCode();
        // Usamos el ID dummy, ya que los datos son dummy.
        int idDistrito = getSelectedDistritoId();
        int idMoneda = ID_MONEDA_DEFAULT;

        RegistroRequest registroRequest = new RegistroRequest(
                dni, nombre, apellido, fechaNacimiento, sexo,
                email, contrasena, idDistrito, idMoneda
        );

        RetrofitClient.INSTANCE.getInstance().registrarUsuario(registroRequest)
                .enqueue(new Callback<RegistroResponse>() {

                    @Override
                    public void onResponse(Call<RegistroResponse> call, Response<RegistroResponse> response) {
                        if (response.isSuccessful()) {
                            RegistroResponse respuesta = response.body();
                            String mensaje = respuesta != null && respuesta.getMensaje() != null ? respuesta.getMensaje() : "Registro exitoso";
                            mostrarMensaje(mensaje);

                            Intent intent = new Intent(RegistroActivity.this, SesionActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                                Log.e("REGISTRO_API", "Error HTTP " + response.code() + ": " + errorBody);

                                Gson gson = new Gson();
                                RegistroResponse errorResponse = gson.fromJson(errorBody, RegistroResponse.class);

                                if (errorResponse != null && errorResponse.getMensaje() != null) {
                                    mostrarMensaje("Error: " + errorResponse.getMensaje());
                                } else {
                                    mostrarMensaje("Error en el registro: Código " + response.code() + ". (Revisa la URL o el servidor).");
                                }

                            } catch (IOException e) {
                                Log.e("REGISTRO_API", "Error leyendo el cuerpo de error", e);
                                mostrarMensaje("Error de comunicación. Inténtalo más tarde.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RegistroResponse> call, Throwable t) {
                        mostrarMensaje("Fallo de conexión. Revisa tu Internet o la URL de la API.");
                        Log.e("REGISTRO_API", "Fallo de conexión: " + t.getMessage(), t);
                    }
                });
    }
}
