package com.example.myapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapp.adapters.RegistroLuzAdapter
import com.example.myapp.models.RegistroLuz
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterRegistros: RegistroLuzAdapter
    private var listenerRegistros: ListenerRegistration? = null

    // Componentes del formulario.
    private lateinit var spDormitorioRegistro: Spinner
    private lateinit var etNivelIluminacion: EditText
    private lateinit var rgEstadoLuz: RadioGroup
    private lateinit var rbEncendida: RadioButton
    private lateinit var rbApagada: RadioButton
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var tvHoraSeleccionada: TextView
    private lateinit var btnGuardarRegistro: MaterialButton
    private lateinit var btnCancelarEdicion: MaterialButton
    private lateinit var tvTituloFormulario: TextView

    // Componentes del historial.
    private lateinit var layoutListaRegistros: LinearLayout
    private lateinit var tvSinRegistros: TextView

    // Fecha y hora seleccionadas.
    private var anioSeleccionado = 0
    private var mesSeleccionado = 0
    private var diaSeleccionado = 0
    private var horaSeleccionada = 0
    private var minutoSeleccionado = 0

    // null indica que se está creando un registro.
    private var idRegistroEnEdicion: String? = null

    private val dormitorios = listOf(
        "Dormitorio principal",
        "Dormitorio secundario",
        "Dormitorio de visitas"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { vista, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            vista.setPadding(
                barras.left,
                barras.top,
                barras.right,
                barras.bottom
            )
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            mostrarMensaje("Inicia sesión para acceder a los registros")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<TextView>(R.id.tvSubtituloUsuario).text =
            "Sesión: ${usuarioActual.email ?: "Usuario conectado"}"

        spDormitorioRegistro = findViewById(R.id.spDormitorioRegistro)
        etNivelIluminacion = findViewById(R.id.etNivelIluminacion)
        rgEstadoLuz = findViewById(R.id.rgEstadoLuz)
        rbEncendida = findViewById(R.id.rbEncendida)
        rbApagada = findViewById(R.id.rbApagada)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        tvHoraSeleccionada = findViewById(R.id.tvHoraSeleccionada)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)
        btnCancelarEdicion = findViewById(R.id.btnCancelarEdicion)
        tvTituloFormulario = findViewById(R.id.tvTituloFormulario)

        layoutListaRegistros = findViewById(R.id.layoutListaRegistros)
        tvSinRegistros = findViewById(R.id.tvSinRegistros)

        val adapterSpinner = ArrayAdapter(
            this,
            R.layout.item_spinner,
            dormitorios
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

        spDormitorioRegistro.adapter = adapterSpinner
        inicializarFechaHoraActual()

        findViewById<MaterialButton>(
            R.id.btnSeleccionarFecha
        ).setOnClickListener {
            mostrarSelectorFecha()
        }

        tvFechaSeleccionada.setOnClickListener {
            mostrarSelectorFecha()
        }

        findViewById<MaterialButton>(
            R.id.btnSeleccionarHora
        ).setOnClickListener {
            mostrarSelectorHora()
        }

        tvHoraSeleccionada.setOnClickListener {
            mostrarSelectorHora()
        }

        btnGuardarRegistro.setOnClickListener {
            procesarGuardado(usuarioActual.uid)
        }

        btnCancelarEdicion.setOnClickListener {
            limpiarFormulario()
            mostrarMensaje("Edición cancelada")
        }

        findViewById<View>(R.id.btnVolverMain)?.setOnClickListener {
            finish()
        }

        // El adaptador muestra las tarjetas y comunica sus acciones.
        adapterRegistros = RegistroLuzAdapter(
            this,
            onEditar = { registro ->
                iniciarEdicion(registro)
            },
            onEliminar = { registro ->
                confirmarEliminacion(registro)
            }
        )

        iniciarEscuchaRegistros()
    }

    override fun onDestroy() {
        listenerRegistros?.remove()
        super.onDestroy()
    }

    /** Inicializa los selectores con la fecha y hora actuales. */
    private fun inicializarFechaHoraActual() {
        val ahora = Calendar.getInstance()

        anioSeleccionado = ahora.get(Calendar.YEAR)
        mesSeleccionado = ahora.get(Calendar.MONTH)
        diaSeleccionado = ahora.get(Calendar.DAY_OF_MONTH)
        horaSeleccionada = ahora.get(Calendar.HOUR_OF_DAY)
        minutoSeleccionado = ahora.get(Calendar.MINUTE)

        actualizarTextosDateTime()
    }

    private fun mostrarSelectorFecha() {
        DatePickerDialog(
            this,
            { _, anio, mes, dia ->
                anioSeleccionado = anio
                mesSeleccionado = mes
                diaSeleccionado = dia
                actualizarTextosDateTime()
            },
            anioSeleccionado,
            mesSeleccionado,
            diaSeleccionado
        ).show()
    }

    private fun mostrarSelectorHora() {
        TimePickerDialog(
            this,
            { _, hora, minuto ->
                horaSeleccionada = hora
                minutoSeleccionado = minuto
                actualizarTextosDateTime()
            },
            horaSeleccionada,
            minutoSeleccionado,
            true
        ).show()
    }

    private fun actualizarTextosDateTime() {
        tvFechaSeleccionada.text = String.format(
            Locale.getDefault(),
            "%02d/%02d/%04d",
            diaSeleccionado,
            mesSeleccionado + 1,
            anioSeleccionado
        )

        tvHoraSeleccionada.text = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            horaSeleccionada,
            minutoSeleccionado
        )
    }

    private fun obtenerFechaHoraFormateada(): String {
        return String.format(
            Locale.getDefault(),
            "%02d/%02d/%04d %02d:%02d",
            diaSeleccionado,
            mesSeleccionado + 1,
            anioSeleccionado,
            horaSeleccionada,
            minutoSeleccionado
        )
    }

    /** Recupera la fecha y hora de un registro al editarlo. */
    private fun parsearFechaHora(fechaHora: String) {
        try {
            val partes = fechaHora.split(" ")

            if (partes.size == 2) {
                val fecha = partes[0].split("/")
                val hora = partes[1].split(":")

                if (fecha.size == 3 && hora.size == 2) {
                    diaSeleccionado = fecha[0].toInt()
                    mesSeleccionado = fecha[1].toInt() - 1
                    anioSeleccionado = fecha[2].toInt()
                    horaSeleccionada = hora[0].toInt()
                    minutoSeleccionado = hora[1].toInt()

                    actualizarTextosDateTime()
                    return
                }
            }
        } catch (_: NumberFormatException) {
            // Si el formato no es válido, se usa el momento actual.
        }

        inicializarFechaHoraActual()
    }

    /** Valida los campos antes de crear o actualizar en Firestore. */
    private fun procesarGuardado(usuarioId: String) {
        val dormitorio =
            spDormitorioRegistro.selectedItem?.toString()?.trim() ?: ""

        val nivelTexto = etNivelIluminacion.text.toString().trim()

        if (dormitorio.isEmpty()) {
            mostrarMensaje("Selecciona un dormitorio")
            return
        }

        if (nivelTexto.isEmpty()) {
            etNivelIluminacion.error =
                "El nivel de iluminación es obligatorio"
            etNivelIluminacion.requestFocus()
            return
        }

        val nivel = nivelTexto.toIntOrNull()

        if (nivel == null || nivel !in 0..1023) {
            etNivelIluminacion.error =
                "Ingresa un número entero entre 0 y 1023"
            etNivelIluminacion.requestFocus()
            return
        }

        if (rgEstadoLuz.checkedRadioButtonId == -1) {
            mostrarMensaje("Selecciona el estado de la luz")
            return
        }

        etNivelIluminacion.error = null
        btnGuardarRegistro.isEnabled = false

        val estado =
            if (rbEncendida.isChecked) "Encendida" else "Apagada"

        val idActual = idRegistroEnEdicion

        val registro = RegistroLuz(
            id = idActual ?: "",
            dormitorio = dormitorio,
            nivelIluminacion = nivel,
            estado = estado,
            fechaHora = obtenerFechaHoraFormateada(),
            usuarioId = usuarioId
        )

        if (idActual == null) {
            db.collection("registrosLuz")
                .add(registro.toMap())
                .addOnSuccessListener {
                    mostrarMensaje("Registro guardado exitosamente")
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    btnGuardarRegistro.isEnabled = true
                    mostrarMensaje(
                        "No se pudo guardar. Revisa tu conexión y vuelve a intentar."
                    )
                }
        } else {
            // Actualiza el mismo documento, conservando su ID.
            db.collection("registrosLuz")
                .document(idActual)
                .set(registro.toMap())
                .addOnSuccessListener {
                    mostrarMensaje("Registro actualizado exitosamente")
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    btnGuardarRegistro.isEnabled = true
                    mostrarMensaje(
                        "No se pudo actualizar. Revisa tu conexión."
                    )
                }
        }
    }

    /** Actualiza el historial cuando cambia la colección. */
    private fun iniciarEscuchaRegistros() {
        listenerRegistros = db.collection("registrosLuz")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    tvSinRegistros.visibility = View.VISIBLE
                    tvSinRegistros.text =
                        "No se pudieron cargar los registros. Revisa tu conexión."
                    return@addSnapshotListener
                }

                val registros = snapshots?.documents
                    ?.map { RegistroLuz.fromDocument(it) }
                    ?: emptyList()

                adapterRegistros.actualizar(registros)
                layoutListaRegistros.removeAllViews()

                tvSinRegistros.visibility =
                    if (registros.isEmpty()) View.VISIBLE else View.GONE

                tvSinRegistros.text = "No hay registros guardados aún"

                for (posicion in 0 until adapterRegistros.count) {
                    layoutListaRegistros.addView(
                        adapterRegistros.getView(
                            posicion,
                            null,
                            layoutListaRegistros
                        )
                    )
                }
            }
    }

    /** Carga el registro seleccionado en el formulario. */
    private fun iniciarEdicion(registro: RegistroLuz) {
        idRegistroEnEdicion = registro.id
        tvTituloFormulario.text = "Editando: ${registro.dormitorio}"
        btnGuardarRegistro.text = "Actualizar registro"
        btnCancelarEdicion.visibility = View.VISIBLE

        val posicion = dormitorios.indexOfFirst {
            it.equals(registro.dormitorio, ignoreCase = true)
        }

        if (posicion >= 0) {
            spDormitorioRegistro.setSelection(posicion)
        }

        etNivelIluminacion.setText(
            registro.nivelIluminacion.toString()
        )

        if (registro.estado.equals("Encendida", ignoreCase = true)) {
            rbEncendida.isChecked = true
        } else {
            rbApagada.isChecked = true
        }

        parsearFechaHora(registro.fechaHora)
        etNivelIluminacion.requestFocus()

        mostrarMensaje("Modifica los campos y presiona Actualizar")
    }

    /** Solicita confirmación antes de eliminar el documento. */
    private fun confirmarEliminacion(registro: RegistroLuz) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage(
                "¿Eliminar el registro de \"${registro.dormitorio}\"?"
            )
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("registrosLuz")
                    .document(registro.id)
                    .delete()
                    .addOnSuccessListener {
                        mostrarMensaje("Registro eliminado exitosamente")

                        if (idRegistroEnEdicion == registro.id) {
                            limpiarFormulario()
                        }
                    }
                    .addOnFailureListener {
                        mostrarMensaje(
                            "No se pudo eliminar. Revisa tu conexión."
                        )
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Restablece el formulario para crear otro registro. */
    private fun limpiarFormulario() {
        idRegistroEnEdicion = null
        tvTituloFormulario.text = "Nuevo registro"
        btnGuardarRegistro.text = "Guardar registro"
        btnGuardarRegistro.isEnabled = true
        btnCancelarEdicion.visibility = View.GONE

        spDormitorioRegistro.setSelection(0)
        etNivelIluminacion.text.clear()
        etNivelIluminacion.error = null
        rbApagada.isChecked = true

        inicializarFechaHoraActual()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}