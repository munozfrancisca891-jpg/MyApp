package com.example.myapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreferenciasActivity : AppCompatActivity() {

    private lateinit var swAlertasLuz: Switch
    private lateinit var spDormitorio: Spinner
    private lateinit var rgTema: RadioGroup
    private lateinit var rbTemaClaro: RadioButton
    private lateinit var rbTemaOscuro: RadioButton
    private lateinit var pbGuardando: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferencias)

        // Configuración de insets para barras del sistema (Edge to Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de componentes de la vista
        swAlertasLuz = findViewById(R.id.swAlertasLuz)
        spDormitorio = findViewById(R.id.spDormitorio)
        rgTema = findViewById(R.id.rgTema)
        rbTemaClaro = findViewById(R.id.rbTemaClaro)
        rbTemaOscuro = findViewById(R.id.rbTemaOscuro)
        pbGuardando = findViewById(R.id.pbGuardando)

        // Botón volver
        val btnVolver = findViewById<View?>(R.id.btnVolverPreferencias)
        btnVolver?.setOnClickListener {
            finish()
        }

        // Listener explícito para el botón guardar
        val btnGuardar = findViewById<View?>(R.id.btnGuardarPreferencia)
        btnGuardar?.setOnClickListener {
            onGuardarPreferenciaClick(it)
        }

        // Configurar el adaptador para el selector de dormitorios
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.dormitorios,
            R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spDormitorio.adapter = adapter

        // Cargar las preferencias persistidas previamente
        cargarPreferencias()
    }

    /**
     * Carga las preferencias almacenadas en SharedPreferences
     * para que persistan entre sesiones de la aplicación.
     */
    private fun cargarPreferencias() {
        val prefs = getSharedPreferences("preferencias_control_luz", MODE_PRIVATE)

        // 1. Estado del switch de alertas de luz
        val alertasLuz = prefs.getBoolean("alertas_luz", false)
        swAlertasLuz.isChecked = alertasLuz

        // 2. Posición del dormitorio seleccionado
        val posDormitorio = prefs.getInt("dormitorio_posicion", 0)
        if (posDormitorio in 0 until spDormitorio.count) {
            spDormitorio.setSelection(posDormitorio)
        }

        // 3. Tema de la aplicación (Claro / Oscuro)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)
        if (esTemaOscuro) {
            rbTemaOscuro.isChecked = true
        } else {
            rbTemaClaro.isChecked = true
        }
    }

    /**
     * Guarda las preferencias del usuario, aplica el tema globalmente
     * y muestra retroalimentación visual al usuario.
     */
    fun onGuardarPreferenciaClick(view: View) {
        val alertasLuz = swAlertasLuz.isChecked
        val posDormitorio = spDormitorio.selectedItemPosition
        val nombreDormitorio = spDormitorio.selectedItem?.toString() ?: ""
        val esTemaOscuro = rbTemaOscuro.isChecked

        // Guardar las configuraciones en SharedPreferences
        val prefs = getSharedPreferences("preferencias_control_luz", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("alertas_luz", alertasLuz)
            .putInt("dormitorio_posicion", posDormitorio)
            .putString("dormitorio_nombre", nombreDormitorio)
            .putBoolean("tema_oscuro", esTemaOscuro)
            .commit()

        // Mostrar indicador visual de guardado
        pbGuardando.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            pbGuardando.visibility = View.GONE

            // Notificación de confirmación al usuario
            Toast.makeText(
                applicationContext,
                "Preferencias guardadas exitosamente",
                Toast.LENGTH_SHORT
            ).show()

            // Aplicar el tema seleccionado a toda la aplicación
            val nuevoModo = if (esTemaOscuro) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(nuevoModo)
        }, 500)
    }
}