package com.example.proyectodenuncias

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class paso5 : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etTelefono: EditText
    private lateinit var radioGroupSexo: RadioGroup
    private lateinit var switchAnonimo: Switch
    private lateinit var btnDenunciar: Button
    private lateinit var btnGenerarPDF: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso5)

        try {
            // Inicialización de vistas
            etNombre = findViewById(R.id.etNombre)
            etCorreo = findViewById(R.id.etCorreo)
            etTelefono = findViewById(R.id.etTelefono)
            radioGroupSexo = findViewById(R.id.radioGroupSexo)
            switchAnonimo = findViewById(R.id.switchAnonimo)
            btnDenunciar = findViewById(R.id.btnDenunciar)
            btnGenerarPDF = findViewById(R.id.btnGenerarPDF)

            // Configurar listeners
            switchAnonimo.setOnCheckedChangeListener { _, isChecked ->
                etNombre.isEnabled = !isChecked
                etCorreo.isEnabled = !isChecked
                etTelefono.isEnabled = !isChecked

                if (isChecked) {
                    etNombre.setText("")
                    etCorreo.setText("")
                    etTelefono.setText("")
                }
            }

            btnDenunciar.setOnClickListener {
                if (validarCampos()) {
                    generarYEnviarDenuncia()
                }
            }

            btnGenerarPDF.setOnClickListener {
                if (validarCampos()) {
                    generarYEnviarDenuncia()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la pantalla", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        if (!switchAnonimo.isChecked) {
            if (etNombre.text.toString().trim().isEmpty()) {
                etNombre.error = "Ingrese su nombre o active anónimo"
                etNombre.requestFocus()
                return false
            }

            if (etCorreo.text.toString().trim().isEmpty()) {
                etCorreo.error = "Ingrese su correo"
                etCorreo.requestFocus()
                return false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etCorreo.text.toString()).matches()) {
                etCorreo.error = "Ingrese un correo válido"
                etCorreo.requestFocus()
                return false
            }

            if (etTelefono.text.toString().trim().isEmpty()) {
                etTelefono.error = "Ingrese su teléfono"
                etTelefono.requestFocus()
                return false
            }
        }
        return true
    }

    private fun generarYEnviarDenuncia() {
        // Generar PDF primero
        val pdfPath = generatePDF() ?: return // Si falla, salir

        // Crear intent para pasofinal
        val intent = Intent(this, pasofinal::class.java).apply {
            // Pasar datos del usuario
            putExtra("nombre", etNombre.text.toString())
            putExtra("correo", etCorreo.text.toString())
            putExtra("telefono", etTelefono.text.toString())
            putExtra("anonimo", switchAnonimo.isChecked)

            // Pasar sexo seleccionado
            putExtra("sexo", when (radioGroupSexo.checkedRadioButtonId) {
                R.id.rbFemenino -> "Femenino"
                R.id.rbMasculino -> "Masculino"
                R.id.rbOtro -> "Otro"
                else -> "No especificado"
            })

            // Pasar ruta del PDF generado
            putExtra("pdfPath", pdfPath)

            // Pasar datos adicionales si es necesario
            intent.extras?.let { bundle ->
                putExtras(bundle)
            }
        }

        // Iniciar actividad pasofinal
        startActivity(intent)
    }

    private fun generatePDF(): String? {
        val document = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        // Configurar página A4
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Obtener datos
        val nombre = if (switchAnonimo.isChecked) "Anónimo" else etNombre.text.toString()
        val correo = etCorreo.text.toString()
        val telefono = etTelefono.text.toString()
        val sexo = when (radioGroupSexo.checkedRadioButtonId) {
            R.id.rbFemenino -> "Femenino"
            R.id.rbMasculino -> "Masculino"
            R.id.rbOtro -> "Otro"
            else -> "No especificado"
        }

        // Contenido del PDF
        canvas.drawText("DENUNCIA OFICIAL", 50f, 50f, paint.apply { textSize = 18f })
        canvas.drawText("Información del Denunciante:", 50f, 80f, paint.apply { textSize = 14f })
        canvas.drawText("Nombre: $nombre", 50f, 100f, paint)
        canvas.drawText("Correo: $correo", 50f, 120f, paint)
        canvas.drawText("Teléfono: $telefono", 50f, 140f, paint)
        canvas.drawText("Sexo: $sexo", 50f, 160f, paint)

        document.finishPage(page)

        return try {
            // Crear directorio si no existe
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: run {
                Toast.makeText(this, "No se pudo acceder al almacenamiento", Toast.LENGTH_SHORT).show()
                return null
            }

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            // Guardar PDF
            val fileName = "denuncia_${System.currentTimeMillis()}.pdf"
            val file = File(storageDir, fileName)
            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            // Devolver la ruta del archivo
            file.absolutePath
        } catch (e: IOException) {
            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}