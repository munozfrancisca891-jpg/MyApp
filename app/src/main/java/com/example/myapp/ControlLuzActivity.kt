
package com.example.myapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Pantalla encargada de controlar visualmente
 * el encendido y apagado de la luz del dormitorio.
 */
class ControlLuzActivity : AppCompatActivity() {

    // Guarda el estado actual de la luz.
    // false = apagada, true = encendida.
    private var luzEncendida = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar el diseño de la pantalla Control de Luz.
        setContentView(R.layout.activity_control_luz)

        // Vincular los textos del XML con Kotlin.
        val txtEstado =
            findViewById<TextView>(R.id.txtEstado)

        val txtAmpolleta =
            findViewById<TextView>(R.id.txtAmpolleta)

        // Vincular los botones de la pantalla.
        val btnEncender =
            findViewById<Button>(R.id.btnEncender)

        val btnApagar =
            findViewById<Button>(R.id.btnApagar)

        val btnVolver =
            findViewById<Button>(R.id.btnVolver)

        /**
         * Actualiza visualmente el estado de la luz.
         * Cambia el texto y la intensidad de la ampolleta.
         */
        fun actualizarEstado() {

            if (luzEncendida) {

                // Mostrar la luz encendida.
                txtEstado.text = "ENCENDIDA"

                // Mostrar la ampolleta iluminada.
                txtAmpolleta.alpha = 1f

            } else {

                // Mostrar la luz apagada.
                txtEstado.text = "APAGADA"

                // Oscurecer visualmente la ampolleta.
                txtAmpolleta.alpha = 0.35f
            }
        }

        // Mostrar el estado inicial de la luz.
        actualizarEstado()

        // Botón para encender visualmente la luz.
        btnEncender.setOnClickListener {

            luzEncendida = true
            actualizarEstado()
        }

        // Botón para apagar visualmente la luz.
        btnApagar.setOnClickListener {

            luzEncendida = false
            actualizarEstado()
        }

        // Botón para regresar a la pantalla anterior.
        btnVolver.setOnClickListener {

            finish()
        }
    }
}
