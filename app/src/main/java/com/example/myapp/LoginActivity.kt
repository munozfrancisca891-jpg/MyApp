package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Valida los datos de acceso y autentica al usuario en Firebase.
 * Permite recordar el correo y acceder al formulario de registro.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa Firebase Authentication.
        auth = FirebaseAuth.getInstance()

        // Vincula los componentes del formulario con sus IDs del XML.
        val etCorreo = findViewById<EditText>(R.id.etCorreoLogin)
        val etContrasena = findViewById<EditText>(R.id.etContrasenaLogin)
        val cbRecordarme = findViewById<CheckBox>(R.id.cbRecordarme)
        val btnIniciar = findViewById<Button>(R.id.btnIniciarSesion)
        val btnRegistro = findViewById<Button>(R.id.btnIrRegistro)

        // Recupera el correo guardado. No almacena la contraseña.
        val preferencias = getSharedPreferences("login", MODE_PRIVATE)
        val recordar = preferencias.getBoolean("recordar", false)

        if (recordar) {
            etCorreo.setText(preferencias.getString("correo", ""))
            cbRecordarme.isChecked = true
        }

        btnIniciar.setOnClickListener {
            // Limpia los mensajes del intento anterior.
            etCorreo.error = null
            etContrasena.error = null

            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString()

            // Valida que el correo esté completo y tenga formato válido.
            if (correo.isEmpty()) {
                etCorreo.error = "Ingresa tu correo"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.error = "Correo no válido"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            if (correo.length > 50) {
                etCorreo.error = "Máximo 50 caracteres"
                etCorreo.requestFocus()
                return@setOnClickListener
            }

            // Valida la contraseña antes de enviar la solicitud a Firebase.
            if (contrasena.isEmpty()) {
                etContrasena.error = "Ingresa tu contraseña"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                etContrasena.error = "Mínimo 6 caracteres"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            if (contrasena.length > 15) {
                etContrasena.error = "Máximo 15 caracteres"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            if (!contrasena.matches(Regex("^[A-Za-z0-9]+$"))) {
                etContrasena.error = "Solo se permiten letras y números"
                etContrasena.requestFocus()
                return@setOnClickListener
            }

            // Evita enviar varias solicitudes mientras se autentica.
            btnIniciar.isEnabled = false

            auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener { task ->
                    btnIniciar.isEnabled = true

                    if (task.isSuccessful) {
                        // Guarda el correo si el usuario seleccionó Recordarme.
                        if (cbRecordarme.isChecked) {
                            preferencias.edit()
                                .putBoolean("recordar", true)
                                .putString("correo", correo)
                                .apply()
                        } else {
                            preferencias.edit()
                                .clear()
                                .apply()
                        }

                        Toast.makeText(
                            this,
                            "Inicio de sesión exitoso",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Envía el correo a la pantalla de bienvenida.
                        val intent = Intent(
                            this,
                            BienvenidaActivity::class.java
                        )

                        intent.putExtra("correo", correo)
                        intent.putExtra("usuario", correo)

                        startActivity(intent)
                        finish()
                    } else {
                        // Si la autenticación falla, permanece en el login.
                        Toast.makeText(
                            this,
                            "No se pudo iniciar sesión. Revisa tu correo y contraseña e inténtalo nuevamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Abre el formulario para crear una cuenta.
        btnRegistro.setOnClickListener {
            val intent = Intent(
                this,
                RegistroActivity::class.java
            )
            startActivity(intent)
        }
    }
}