
package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class BienvenidaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        auth = FirebaseAuth.getInstance()

        val txtBienvenida =
            findViewById<TextView>(R.id.txtBienvenida)

        val btnControlarLuz =
            findViewById<Button>(R.id.btnControlarLuz)

        val btnRegistros =
            findViewById<Button>(R.id.btnRegistrosIluminacion)

        val btnPreferencias =
            findViewById<Button>(R.id.btnPreferencias)

        val btnCerrarSesion =
            findViewById<Button?>(R.id.btnCerrarSesion)

        // Obtener el correo del usuario
        val correo = intent.getStringExtra("correo")
            ?: intent.getStringExtra("usuario")
            ?: auth.currentUser?.email
            ?: "usuario"

        txtBienvenida.text = "Bienvenido/a, $correo"

        // Abrir pantalla para controlar la luz
        btnControlarLuz.setOnClickListener {
            val intent = Intent(
                this,
                ControlLuzActivity::class.java
            )
            startActivity(intent)
        }

        // Abrir registros de iluminación
        btnRegistros.setOnClickListener {
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        // Abrir preferencias
        btnPreferencias.setOnClickListener {
            val intent = Intent(
                this,
                PreferenciasActivity::class.java
            )
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        // Cerrar sesión
        btnCerrarSesion?.setOnClickListener {
            auth.signOut()

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    // Método compatible con android:onClick
    fun onPreferenciasClick(view: View) {
        val correo = intent.getStringExtra("correo")
            ?: intent.getStringExtra("usuario")
            ?: auth.currentUser?.email
            ?: "usuario"

        val intent = Intent(
            this,
            PreferenciasActivity::class.java
        )

        intent.putExtra("correo", correo)
        startActivity(intent)
    }
}
