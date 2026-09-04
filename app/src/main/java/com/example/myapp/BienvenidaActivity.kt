package com.example.myapp

// Importa la clase Intent para realizar la navegación entre componentes de la aplicación
import android.content.Intent
// Importa Bundle para guardar y restaurar el estado de la actividad
import android.os.Bundle
// Importa View para recibir el botón u objeto que disparó el evento de clic
import android.view.View
// Importa enableEdgeToEdge para permitir que la interfaz se extienda de borde a borde
import androidx.activity.enableEdgeToEdge
// Importa AppCompatActivity para proveer compatibilidad con versiones anteriores de Android
import androidx.appcompat.app.AppCompatActivity
// Importa ViewCompat para aplicar ajustes compatibles en las vistas
import androidx.core.view.ViewCompat
// Importa WindowInsetsCompat para manejar las dimensiones de las barras del sistema
import androidx.core.view.WindowInsetsCompat

// Define la clase de la actividad BienvenidaActivity heredando de AppCompatActivity
class BienvenidaActivity : AppCompatActivity() {

    // Método del ciclo de vida que se ejecuta cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ejecuta la inicialización de la clase padre
        super.onCreate(savedInstanceState)
        // Activa la renderización de borde a borde en la pantalla
        enableEdgeToEdge()
        // Infla y enlaza el archivo de diseño XML activity_bienvenida a esta actividad
        setContentView(R.layout.activity_bienvenida)
        // Configura el ajuste automático de espaciado según las barras de estado y navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Obtiene los márgenes de las barras del sistema
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Asigna los márgenes obtenidos al relleno de la vista raíz
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Retorna los insets actualizados
            insets
        }
    }

    // Método vinculado al botón mediante el atributo android:onClick="onPreferenciasClick"
    fun onPreferenciasClick(view: View) {
        // Obtiene el texto del usuario recibido como extra en el Intent actual
        val usuario = intent.getStringExtra("usuario")
        // Crea un nuevo Intent para abrir la actividad PreferenciasActivity
        val intent = Intent(this, PreferenciasActivity::class.java)
        // Adjunta el nombre del usuario obtenido como extra en el nuevo Intent
        intent.putExtra("usuario", usuario)
        // Inicia la actividad PreferenciasActivity pasando el Intent configurado
        startActivity(intent)
    }
}
