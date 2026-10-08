package com.example.myapp.models
import com.google.firebase.firestore.DocumentId

data class EstadoLuz (
    @DocumentId
    val id: String = "",
    val ubicacion: String = "",       // Ejemplo: "Dormitorio Principal"
    val nivelBrillo: Double = 0.0,    // Campo numérico del dispositivo/sensor
    val hora: String = ""             // Hora del registro
     )