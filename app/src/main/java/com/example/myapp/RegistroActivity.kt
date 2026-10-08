
package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Pantalla de registro de usuarios.
 * Valida los datos, crea una cuenta en Firebase Authentication
 * y guarda la información del usuario en Cloud Firestore.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Componentes del formulario
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button

    // Datos pendientes si falla el guardado en Firestore
    private var uidPendiente: String? = null
    private var nombrePendiente = ""
    private var correoPendiente = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar servicios Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vincular los campos del XML
        etNombre = findViewById(R.id.etNombreRegistro)
        etCorreo = findViewById(R.id.etCorreoRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        etConfirmar = findViewById(R.id.etConfirmarContrasena)
        btnRegistrar = findViewById(R.id.btnRegistrarse)
        btnVolver = findViewById(R.id.btnVolverLogin)

        // Recuperar un guardado pendiente
        uidPendiente = savedInstanceState
            ?.getString("uidPendiente")

        nombrePendiente = savedInstanceState
            ?.getString("nombrePendiente") ?: ""

        correoPendiente = savedInstanceState
            ?.getString("correoPendiente") ?: ""

        if (uidPendiente != null) {
            bloquearCampos()
            btnRegistrar.text = "Reintentar guardado"
        }

        // Botón para registrar al usuario
        btnRegistrar.setOnClickListener {

            // Reintentar si la cuenta ya fue creada
            if (uidPendiente != null) {
                guardarUsuario()
                return@setOnClickListener
            }

            // Obtener los datos del formulario
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString()
            val confirmar = etConfirmar.text.toString()

            // Limpiar errores anteriores
            etNombre.error = null
            etCorreo.error = null
            etContrasena.error = null
            etConfirmar.error = null

            var primerCampoInvalido: EditText? = null

            // Mostrar errores sin detener las demás validaciones
            fun marcarError(campo: EditText, mensaje: String) {
                campo.error = mensaje

                if (primerCampoInvalido == null) {
                    primerCampoInvalido = campo
                }
            }

            // VALIDACIÓN DEL NOMBRE
            when {
                nombre.isBlank() -> {
                    marcarError(
                        etNombre,
                        "Ingresa tu nombre"
                    )
                }

                nombre.length > 50 -> {
                    marcarError(
                        etNombre,
                        "Máximo 50 caracteres"
                    )
                }
            }

            // VALIDACIÓN DEL CORREO
            // Exige un dominio completo, como gmail.com
            val formatoCorreo = Regex(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )

            when {
                correo.isBlank() -> {
                    marcarError(
                        etCorreo,
                        "Ingresa tu correo"
                    )
                }

                correo.length > 50 -> {
                    marcarError(
                        etCorreo,
                        "Máximo 50 caracteres"
                    )
                }

                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ||
                        !formatoCorreo.matches(correo) -> {
                    marcarError(
                        etCorreo,
                        "Correo electrónico inválido"
                    )
                }
            }

            // VALIDACIÓN DE LA CONTRASEÑA
            when {
                contrasena.length !in 6..15 -> {
                    marcarError(
                        etContrasena,
                        "Debe tener entre 6 y 15 caracteres"
                    )
                }

                !contrasena.matches(Regex("^[A-Za-z0-9]+$")) -> {
                    marcarError(
                        etContrasena,
                        "Solo se permiten letras y números"
                    )
                }
            }

            // VALIDACIÓN DE CONFIRMACIÓN
            when {
                confirmar.isEmpty() -> {
                    marcarError(
                        etConfirmar,
                        "Confirma tu contraseña"
                    )
                }

                contrasena != confirmar -> {
                    marcarError(
                        etConfirmar,
                        "Las contraseñas no coinciden"
                    )
                }
            }

            // No enviar datos a Firebase si hay errores
            if (primerCampoInvalido != null) {
                primerCampoInvalido?.requestFocus()
                return@setOnClickListener
            }

            cambiarCarga(true)

            // Crear usuario en Firebase Authentication
            auth.createUserWithEmailAndPassword(
                correo,
                contrasena
            ).addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val usuario = task.result?.user

                    if (usuario == null) {
                        cambiarCarga(false)
                        mostrarMensaje(
                            "No se pudo obtener la cuenta creada"
                        )
                        return@addOnCompleteListener
                    }

                    uidPendiente = usuario.uid
                    nombrePendiente = nombre
                    correoPendiente = correo

                    bloquearCampos()
                    guardarUsuario()

                } else {
                    cambiarCarga(false)

                    if (task.exception is FirebaseAuthUserCollisionException) {
                        etCorreo.error =
                            "Este correo ya está registrado"
                    } else {
                        mostrarMensaje(
                            "No se pudo crear la cuenta. Revisa tu conexión."
                        )
                    }
                }
            }
        }

        // Regresar a la pantalla de Login
        btnVolver.setOnClickListener {
            if (uidPendiente != null) {
                auth.signOut()
            }
            finish()
        }
    }

    /**
     * Guardar datos del usuario en Firestore.
     * La contraseña nunca se guarda en la base de datos.
     */
    private fun guardarUsuario() {

        val uid = uidPendiente ?: return

        cambiarCarga(true)

        val datos = hashMapOf<String, Any>(
            "uid" to uid,
            "nombre" to nombrePendiente,
            "correo" to correoPendiente,
            "fechaRegistro" to FieldValue.serverTimestamp()
        )

        db.collection("usuarios")
            .document(uid)
            .set(datos)
            .addOnSuccessListener {

                mostrarMensaje("Cuenta creada correctamente")

                // Navegar a la pantalla de bienvenida
                val destino = Intent(
                    this,
                    BienvenidaActivity::class.java
                )

                destino.putExtra("correo", correoPendiente)
                destino.putExtra("usuario", nombrePendiente)

                startActivity(destino)
                finish()
            }
            .addOnFailureListener {

                cambiarCarga(false)
                btnRegistrar.text = "Reintentar guardado"

                mostrarMensaje(
                    "La cuenta se creó, pero falta guardar el perfil. " +
                            "Revisa la conexión y los permisos de Firestore; " +
                            "luego presiona Reintentar guardado."
                )
            }
    }

    // Bloquear campos cuando la cuenta ya fue creada
    private fun bloquearCampos() {
        etNombre.isEnabled = false
        etCorreo.isEnabled = false
        etContrasena.isEnabled = false
        etConfirmar.isEnabled = false
    }

    // Evitar múltiples registros mientras Firebase trabaja
    private fun cambiarCarga(cargando: Boolean) {

        btnRegistrar.isEnabled = !cargando
        btnVolver.isEnabled = !cargando

        btnRegistrar.text = if (cargando) {
            "Guardando..."
        } else if (uidPendiente != null) {
            "Reintentar guardado"
        } else {
            "Registrarse"
        }
    }

    // Mostrar mensajes informativos
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    // Conservar datos si Android recrea la pantalla
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("uidPendiente", uidPendiente)
        outState.putString("nombrePendiente", nombrePendiente)
        outState.putString("correoPendiente", correoPendiente)
    }
}
