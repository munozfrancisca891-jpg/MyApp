package com.example.myapp

// Importa Handler para programar la ejecución diferida de código en un hilo específico
import android.os.Handler
// Importa Looper para enlazar la ejecución con el hilo principal de la interfaz de usuario
import android.os.Looper
// Importa Bundle para guardar y recuperar el estado de la actividad
import android.os.Bundle
// Importa View para recibir la vista que originó el evento de clic
import android.view.View
// Importa ProgressBar para manipular la visibilidad de la barra de progreso
import android.widget.ProgressBar
// Importa RadioButton para interactuar con el botón de opción seleccionado
import android.widget.RadioButton
// Importa RadioGroup para consultar cuál opción está marcada en el grupo
import android.widget.RadioGroup
// Importa Toast para mostrar notificaciones breves en pantalla
import android.widget.Toast
// Importa enableEdgeToEdge para extender la interfaz a los bordes de la pantalla
import androidx.activity.enableEdgeToEdge
// Importa AppCompatActivity para compatibilidad de características de Android
import androidx.appcompat.app.AppCompatActivity
// Importa ViewCompat para ajustar márgenes compatibles con las barras del sistema
import androidx.core.view.ViewCompat
// Importa WindowInsetsCompat para obtener información de los bordes del sistema
import androidx.core.view.WindowInsetsCompat

// Define la clase PreferenciasActivity heredando de AppCompatActivity
class PreferenciasActivity : AppCompatActivity() {

    // Método del ciclo de vida invocado al iniciar la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ejecuta la inicialización estándar de la clase padre
        super.onCreate(savedInstanceState)
        // Activa la visualización de pantalla completa de borde a borde
        enableEdgeToEdge()
        // Enlaza el diseño de activity_preferencias con esta actividad
        setContentView(R.layout.activity_preferencias)
        // Configura el ajuste automático del padding según las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Obtiene los márgenes de las barras de estado y navegación
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Asigna los márgenes correspondientes a la vista principal
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna los insets actualizados
            insets
        }
    }

    // Método que atiende el clic del botón btnGuardarPreferencia
    fun onGuardarPreferenciaClick(view: View) {
        // Busca el ProgressBar pbGuardando en el layout
        val pbGuardando = findViewById<ProgressBar>(R.id.pbGuardando)
        // Busca el RadioGroup rgUnidad en el layout
        val rgUnidad = findViewById<RadioGroup>(R.id.rgUnidad)
        // Obtiene el ID del RadioButton que está seleccionado actualmente
        val idSeleccionado = rgUnidad.checkedRadioButtonId
        // Busca el RadioButton específico utilizando su ID obtenido
        val rbSeleccionado = findViewById<RadioButton>(idSeleccionado)
        // Extrae el texto de la opción seleccionada (Celsius o Fahrenheit)
        val unidadElegida = rbSeleccionado?.text?.toString() ?: "No especificada"

        // Hace visible el ProgressBar para indicar que el proceso comenzó
        pbGuardando.visibility = View.VISIBLE

        // Programa una tarea en el hilo principal con un retardo de 1 segundo (1000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
            // Oculta nuevamente el ProgressBar cambiando su visibilidad a GONE
            pbGuardando.visibility = View.GONE
            // Muestra un Toast confirmando la unidad que fue seleccionada por el usuario
            Toast.makeText(this, "Unidad elegida: $unidadElegida", Toast.LENGTH_SHORT).show()
        }, 1000)
    }
}