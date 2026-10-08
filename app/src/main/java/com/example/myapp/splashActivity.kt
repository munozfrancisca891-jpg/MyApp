
package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class splashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar la pantalla de presentación
        setContentView(R.layout.activity_splash)

        // Mantener la presentación durante 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({

            // Abrir la pantalla de inicio de sesión
            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            startActivity(intent)

            // Cerrar la presentación para no regresar a ella
            finish()

        }, 2000)
    }
}
