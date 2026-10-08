
package com.example.myapp.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.R
import com.example.myapp.models.RegistroLuz
import com.google.android.material.button.MaterialButton

/**
 * Adaptador encargado de mostrar los registros de iluminación.
 *
 * Cada registro contiene el dormitorio, estado de la luz,
 * nivel de iluminación LDR, fecha y hora.
 *
 * También permite editar y eliminar registros.
 */
class RegistroLuzAdapter(
    private val context: Context,
    private val onEditar: (RegistroLuz) -> Unit,
    private val onEliminar: (RegistroLuz) -> Unit
) : BaseAdapter() {

    // Lista que almacena los registros obtenidos de Firebase.
    private val registros = mutableListOf<RegistroLuz>()

    /**
     * Actualiza la lista cuando se reciben cambios
     * desde Cloud Firestore.
     */
    fun actualizar(nuevosRegistros: List<RegistroLuz>) {
        registros.clear()
        registros.addAll(nuevosRegistros)

        // Notifica que los datos fueron actualizados.
        notifyDataSetChanged()
    }

    // Devuelve la cantidad total de registros.
    override fun getCount(): Int = registros.size

    // Obtiene un registro según su posición.
    override fun getItem(position: Int): RegistroLuz {
        return registros[position]
    }

    // Devuelve la posición del registro como identificador.
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Construye la tarjeta visual de cada registro.
     *
     * Incluye dormitorio, estado, nivel LDR,
     * fecha, hora y botones de acción.
     */
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        // Obtener el registro que se mostrará.
        val registro = getItem(position)

        // Obtener el color del texto según el tema activo.
        val valorColor = TypedValue()

        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurface,
            valorColor,
            true
        )

        val colorTexto = valorColor.data

        // Crear la tarjeta principal.
        val tarjeta = LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL

            // Aplicar el fondo diseñado para las tarjetas.
            setBackgroundResource(R.drawable.bg_card)

            setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
            )

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
        }

        // Crear el encabezado de la tarjeta.
        val encabezado = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Mostrar el dormitorio registrado.
        val dormitorio = TextView(context).apply {

            text = "📍 ${registro.dormitorio}"
            textSize = 16f

            setTypeface(null, Typeface.BOLD)
            setTextColor(colorTexto)

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Mostrar si la luz está encendida o apagada.
        val estado = TextView(context).apply {

            val icono =
                if (registro.estado == "Encendida") {
                    "💡"
                } else {
                    "🌑"
                }

            text = "$icono ${registro.estado}"
            textSize = 13f

            setTextColor(colorTexto)
            setPadding(dp(8), 0, 0, 0)
        }

        // Agregar dormitorio y estado al encabezado.
        encabezado.addView(dormitorio)
        encabezado.addView(estado)

        // Mostrar nivel LDR, fecha y hora del registro.
        val detalles = TextView(context).apply {

            text =
                "🔆 Nivel LDR: ${registro.nivelIluminacion}\n" +
                        "⏰ Fecha/Hora: ${registro.fechaHora}"

            textSize = 14f
            setTextColor(colorTexto)

            setPadding(
                0,
                dp(8),
                0,
                dp(8)
            )
        }

        // Contenedor horizontal para los botones.
        val acciones = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        /**
         * Botón Editar.
         *
         * Envía el registro seleccionado a MainActivity,
         * donde se cargan sus datos en el formulario.
         */
        val editar = MaterialButton(
            context,
            null,
            com.google.android.material.R.attr.borderlessButtonStyle
        ).apply {

            text = "Editar"
            textSize = 13f

            setOnClickListener {
                onEditar(registro)
            }
        }

        /**
         * Botón Eliminar.
         *
         * Envía el registro a MainActivity,
         * donde se solicita confirmación antes de borrarlo.
         */
        val eliminar = MaterialButton(
            context,
            null,
            com.google.android.material.R.attr.borderlessButtonStyle
        ).apply {

            text = "Eliminar"
            textSize = 13f

            setTextColor(Color.RED)

            setOnClickListener {
                onEliminar(registro)
            }
        }

        // Agregar los botones al contenedor.
        acciones.addView(editar)
        acciones.addView(eliminar)

        // Construir la tarjeta con todos sus elementos.
        tarjeta.addView(encabezado)
        tarjeta.addView(detalles)
        tarjeta.addView(acciones)

        // Devolver la tarjeta para mostrarla en la lista.
        return tarjeta
    }

    /**
     * Convierte medidas dp a píxeles
     * para mantener tamaños adecuados en Android.
     */
    private fun dp(valor: Int): Int {
        return (
                valor * context.resources.displayMetrics.density
                ).toInt()
    }
}
