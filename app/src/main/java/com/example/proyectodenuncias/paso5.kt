package com.example.proyectodenuncias

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso5)

        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etTelefono = findViewById(R.id.etTelefono)
        radioGroupSexo = findViewById(R.id.radioGroupSexo)
        switchAnonimo = findViewById(R.id.switchAnonimo)
        btnDenunciar = findViewById(R.id.btnDenunciar)

        switchAnonimo.setOnCheckedChangeListener { _, isChecked ->
            etNombre.isEnabled = !isChecked
            etCorreo.isEnabled = !isChecked
            etTelefono.isEnabled = !isChecked
            if (isChecked) {
                etNombre.text.clear()
                etCorreo.text.clear()
                etTelefono.text.clear()
            }
        }

        btnDenunciar.setOnClickListener {
            if (!switchAnonimo.isChecked && (etNombre.text.isEmpty() || etCorreo.text.isEmpty() || etTelefono.text.isEmpty())) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val selectedSexo = when (radioGroupSexo.checkedRadioButtonId) {
                    R.id.rbFemenino -> "Femenino"
                    R.id.rbMasculino -> "Masculino"
                    R.id.rbOtro -> "Otro"
                    else -> "No especificado"
                }

                // Generar y abrir PDF
                generatePDF(etNombre.text.toString(), etCorreo.text.toString(), etTelefono.text.toString(), selectedSexo)

                // Mostrar mensaje y redirigir al paso final
                Toast.makeText(this, "Denuncia enviada como: $selectedSexo", Toast.LENGTH_LONG).show()
                val folio = generateFolio()
                val intent = Intent(this, pasofinal::class.java)
                intent.putExtra("FOLIO", folio)
                startActivity(intent)
            }
        }
    }

    private fun generateFolio(): String {
        return (1000..9999).random().toString()
    }

    private fun generatePDF(nombre: String, correo: String, telefono: String, sexo: String) {
        val document = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        paint.textSize = 12f

        canvas.drawText("Nombre: $nombre", 20f, 50f, paint)
        canvas.drawText("Correo: $correo", 20f, 70f, paint)
        canvas.drawText("Telefono: $telefono", 20f, 90f, paint)
        canvas.drawText("Sexo: $sexo", 20f, 110f, paint)

        document.finishPage(page)

        try {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "denuncia_${System.currentTimeMillis()}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            Toast.makeText(this, "PDF generado: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

            // Abrir PDF automáticamente
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
