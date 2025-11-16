package com.example.ahorra.fragmentos;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.app.LocaleManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.ahorra.R; // Asegúrate de que este sea tu paquete R correcto

import java.util.Locale;

public class PreferenciasFragment extends Fragment {

    // Constantes para SharedPreferences
    public static final String PREFS_NAME = "ConfiguracionApp";
    public static final String KEY_IDIOMA_POS = "idioma_pos";
    public static final String KEY_MONEDA_POS = "moneda_pos";
    public static final String KEY_TEMA = "tema";
    public static final String KEY_NOTIFICACIONES = "notificaciones";
    public static final String KEY_UBICACION = "ubicacion";
    public static final String TEMA_CLARO = "claro";
    public static final String TEMA_OSCURO = "oscuro";

    // Vistas
    private MaterialToolbar toolbar;
    private Spinner spinnerIdioma;
    private Spinner spinnerMoneda;
    private RadioGroup radioGroupTema;
    private RadioButton radioClaro;
    private RadioButton radioOscuro;
    private CheckBox checkboxNotificaciones;
    private CheckBox checkboxUbicacion;
    private Button btnGuardarCambios;

    private SharedPreferences prefs;

    // Lanzadores de permisos modernos
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SharedPreferences
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // --- Inicializar lanzadores de permisos ---

        // Para Ubicación
        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                checkboxUbicacion.setChecked(false); // Desmarcar si el usuario deniega
            }
        });

        // Para Notificaciones (requerido en API 33+)
        requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(getContext(), "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                checkboxNotificaciones.setChecked(false); // Desmarcar si el usuario deniega
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferencias, container, false);

        // 1. Vincular todas las vistas
        bindViews(view);

        // 2. Configurar los listeners
        setupListeners();

        // 3. Configurar los Spinners
        setupSpinners();

        // 4. Cargar las preferencias guardadas
        loadPreferences();

        return view;
    }

    private void bindViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_preferencias);
        spinnerIdioma = view.findViewById(R.id.spinner_idioma);
        spinnerMoneda = view.findViewById(R.id.spinner_moneda);
        radioGroupTema = view.findViewById(R.id.radio_group_tema);
        radioClaro = view.findViewById(R.id.radio_claro);
        radioOscuro = view.findViewById(R.id.radio_oscuro);
        checkboxNotificaciones = view.findViewById(R.id.checkbox_notificaciones);
        checkboxUbicacion = view.findViewById(R.id.checkbox_ubicacion);
        btnGuardarCambios = view.findViewById(R.id.btn_guardar_cambios);
    }

    private void setupListeners() {
        // Botón de retroceso en la Toolbar
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Botón Guardar
        btnGuardarCambios.setOnClickListener(v -> savePreferences());

        // Listeners para los CheckBox que solicitan permisos
        checkboxUbicacion.setOnClickListener(v -> {
            if (checkboxUbicacion.isChecked()) {
                requestLocationPermission();
            }
        });

        checkboxNotificaciones.setOnClickListener(v -> {
            if (checkboxNotificaciones.isChecked()) {
                requestNotificationPermission();
            }
        });
    }

    private void setupSpinners() {
        // Configurar Spinner de Idioma
        ArrayAdapter<CharSequence> idiomaAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.idiomas_nombres, android.R.layout.simple_spinner_item);
        idiomaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdioma.setAdapter(idiomaAdapter);

        // Configurar Spinner de Moneda
        ArrayAdapter<CharSequence> monedaAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.monedas_array, android.R.layout.simple_spinner_item);
        monedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(monedaAdapter);
    }

    private void loadPreferences() {
        // Cargar Tema
        String tema = prefs.getString(KEY_TEMA, TEMA_CLARO);
        if (tema.equals(TEMA_OSCURO)) {
            radioOscuro.setChecked(true);
        } else {
            radioClaro.setChecked(true);
        }

        // Cargar Spinners
        spinnerIdioma.setSelection(prefs.getInt(KEY_IDIOMA_POS, 0));
        spinnerMoneda.setSelection(prefs.getInt(KEY_MONEDA_POS, 0));

        // Cargar Notificaciones (basado en la preferencia guardada)
        checkboxNotificaciones.setChecked(prefs.getBoolean(KEY_NOTIFICACIONES, true));

        // Cargar Ubicación (basado en la preferencia guardada)
        // También podríamos verificar el permiso real del sistema aquí
        boolean ubicacionGuardada = prefs.getBoolean(KEY_UBICACION, false);
        boolean permisoUbicacionConcedido = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // El checkbox solo debe estar marcado si la pref está guardada Y el permiso concedido
        checkboxUbicacion.setChecked(ubicacionGuardada && permisoUbicacionConcedido);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();

        // Guardar Spinners (guardamos la posición)
        int idiomaPos = spinnerIdioma.getSelectedItemPosition();
        editor.putInt(KEY_IDIOMA_POS, idiomaPos);
        editor.putInt(KEY_MONEDA_POS, spinnerMoneda.getSelectedItemPosition());

        // Guardar Tema
        String tema;
        if (radioGroupTema.getCheckedRadioButtonId() == R.id.radio_oscuro) {
            tema = TEMA_OSCURO;
        } else {
            tema = TEMA_CLARO;
        }
        editor.putString(KEY_TEMA, tema);

        // Guardar CheckBoxes
        editor.putBoolean(KEY_NOTIFICACIONES, checkboxNotificaciones.isChecked());
        editor.putBoolean(KEY_UBICACION, checkboxUbicacion.isChecked());

        // Aplicar los cambios
        editor.apply();

        Toast.makeText(getContext(), "Preferencias guardadas", Toast.LENGTH_SHORT).show();

        // Aplicar cambios que requieren reinicio
        applyTheme(tema);
        applyLanguage(idiomaPos);
    }

    // --- LÓGICA DE APLICACIÓN DE CAMBIOS ---

    private void requestLocationPermission() {
        // Verificar si ya tenemos el permiso
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no, lanzamos la solicitud
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void requestNotificationPermission() {
        // Solo aplica para Android 13 (API 33) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si no, lanzamos la solicitud
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void applyTheme(String tema) {
        if (tema.equals(TEMA_OSCURO)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // El sistema se encargará de recrear la Activity si es necesario
    }

    private void applyLanguage(int position) {
        // Obtenemos el código de idioma (ej: "es", "en") de nuestro array de recursos
        String[] langCodes = getResources().getStringArray(R.array.idiomas_codigos);
        String langCode = langCodes[position];

        // API 33+ (Android 13) tiene una nueva API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Context context = getContext();
            if (context != null) {
                context.getSystemService(LocaleManager.class).setApplicationLocales(
                        LocaleList.forLanguageTags(langCode)
                );
            }
        } else {
            // Método legacy para versiones anteriores
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);
            Resources resources = requireActivity().getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        // ¡IMPORTANTE! Se debe recrear la Activity para que los cambios de idioma surtan efecto
        requireActivity().recreate();
    }
}