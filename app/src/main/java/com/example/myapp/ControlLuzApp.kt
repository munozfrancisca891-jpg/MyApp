package com.example.myapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Clase de aplicación principal para inicializar y mantener el tema global seleccionado
 * por el usuario en toda la app Control de Luz.
 */
class ControlLuzApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Recuperar y aplicar la preferencia de tema guardada al abrir la aplicación
        val prefs = getSharedPreferences("preferencias_control_luz", MODE_PRIVATE)
        val esTemaOscuro = prefs.getBoolean("tema_oscuro", false)

        val modoTema = if (esTemaOscuro) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        AppCompatDelegate.setDefaultNightMode(modoTema)
    }
}
