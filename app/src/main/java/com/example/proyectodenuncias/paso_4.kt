package com.example.proyectodenuncias

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectodenuncias.R.id.switchAnonimo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class paso_4 : AppCompatActivity() {
    // Declaración de vistas
    private var fotoPath: String? = null
    private lateinit var etNombre: EditText
    private lateinit var etPuesto: EditText
    private lateinit var etDependencia: EditText
    private lateinit var etLugar: EditText
    private lateinit var tvDescripcion: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var btnContinuar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso4)

        // Inicialización de vistas
        etNombre = findViewById(R.id.etNombre)
        etPuesto = findViewById(R.id.etPuesto)
        etDependencia = findViewById(R.id.etDependencia)
        etLugar = findViewById(R.id.etLugar)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        tvFecha = findViewById(R.id.tvFecha)
        tvHora = findViewById(R.id.tvHora)
        ivPreview = findViewById(R.id.ivPreview)
        btnContinuar = findViewById(R.id.btnContinuar)


        // Obtener datos del intent
        fotoPath = intent.getStringExtra("fotoPath")
        val descripcion = intent.getStringExtra("descripcion") ?: "No proporcionado"
        val lugar = intent.getStringExtra("lugar") ?: "No proporcionado"
        val fecha = intent.getStringExtra("fecha") ?: "No proporcionada"
        val hora = intent.getStringExtra("hora") ?: "No proporcionada"

        // Mostrar datos en la UI
        tvDescripcion.text = descripcion
        etLugar.setText(lugar)
        tvFecha.text = fecha
        tvHora.text = hora

        // Cargar imagen si existe
        fotoPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    ivPreview.setImageURI(Uri.fromFile(file))
                    ivPreview.visibility = View.VISIBLE
                } else { }
            } catch (e: Exception) {
                Log.e("PASO_4", "Error al cargar imagen", e)
            }
        }



        btnContinuar.setOnClickListener {
            try {

                val intent = Intent(this, paso5::class.java).apply {
                    // Datos de paso_3
                    putExtra("descripcion", descripcion)
                    putExtra("lugar", lugar)
                    putExtra("fecha", fecha)
                    putExtra("hora", hora)
                    putExtra("fotoPath", fotoPath)

                    // Datos de paso_4
                    putExtra("nombre", etNombre.text.toString().trim())
                    putExtra("puesto", etPuesto.text.toString().trim())
                    putExtra("dependencia", etDependencia.text.toString().trim())


                    // Generar folio único
                    putExtra("FOLIO", generarFolio())
                }

                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PASO_4", "Error al continuar", e)
            }
        }
    }

    private fun generarFolio(): String {
        return "DN-${SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())}"
    }
}