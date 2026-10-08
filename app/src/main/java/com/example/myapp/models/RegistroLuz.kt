package com.example.myapp.models

import com.google.firebase.firestore.DocumentId

/**
 * Modelo de datos para representar una lectura o registro de iluminación LDR
 * en la colección "registrosLuz" de Firestore.
 */
data class RegistroLuz(
    @DocumentId
    val id: String = "",
    val dormitorio: String = "",
    val nivelIluminacion: Int = 0,
    val estado: String = "Apagada", // "Encendida" o "Apagada"
    val fechaHora: String = "",
    val usuarioId: String = ""
) {
    /**
     * Convierte el modelo a un mapa para persistir en Firestore
     * sin duplicar el ID del documento en los campos del cuerpo.
     */
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "dormitorio" to dormitorio,
            "nivelIluminacion" to nivelIluminacion,
            "estado" to estado,
            "fechaHora" to fechaHora,
            "usuarioId" to usuarioId,
            "fechaRegistro" to System.currentTimeMillis()
        )
    }

    companion object {
        /**
         * Crea una instancia a partir de un DocumentSnapshot de forma tolerante a fallos,
         * extrayendo el ID asignado por Firestore y normalizando valores numéricos.
         */
        fun fromDocument(doc: com.google.firebase.firestore.DocumentSnapshot): RegistroLuz {
            val id = doc.id
            val dormitorio = doc.getString("dormitorio") ?: doc.getString("ubicacion") ?: ""
            val nivel = doc.getLong("nivelIluminacion")?.toInt()
                ?: doc.getDouble("nivelIluminacion")?.toInt()
                ?: (doc.get("nivelIluminacion") as? Number)?.toInt()
                ?: (doc.getString("nivelIluminacion")?.toIntOrNull())
                ?: 0
            val estado = doc.getString("estado") ?: "Apagada"
            val fechaHora = doc.getString("fechaHora") ?: doc.getString("hora") ?: ""
            val usuarioId = doc.getString("usuarioId") ?: ""

            return RegistroLuz(
                id = id,
                dormitorio = dormitorio,
                nivelIluminacion = nivel,
                estado = estado,
                fechaHora = fechaHora,
                usuarioId = usuarioId
            )
        }
    }
}
