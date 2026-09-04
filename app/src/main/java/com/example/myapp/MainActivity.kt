package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val chkGuardarSesion = findViewById<CheckBox>(R.id.chkGuardarSesion)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val btnLimpiar = findViewById<Button>(R.id.btnLimpiar)

        btnIniciarSesion.setOnClickListener {
            val usuario = edtUsuario.text.toString().trim()
            val password = edtPassword.text.toString()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Iniciando sesión como $usuario", Toast.LENGTH_SHORT).show()
                // Crea el Intent para abrir BienvenidaActivity pasando el usuario como extra
                val intent = Intent(this, BienvenidaActivity::class.java)
                intent.putExtra("usuario", usuario)
                startActivity(intent)
            }
        }

        btnLimpiar.setOnClickListener {
            edtUsuario.text.clear()
            edtPassword.text.clear()
            chkGuardarSesion.isChecked = false
            Toast.makeText(this, "Campos limpiados", Toast.LENGTH_SHORT).show()
        }
    }
}