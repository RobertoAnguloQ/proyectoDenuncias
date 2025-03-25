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

    private lateinit var nombre: String
    private lateinit var correo: String
    private lateinit var telefono: String
    private lateinit var sexo: String

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

        // Recoger datos pasados de las pantallas anteriores
        nombre = intent.getStringExtra("NOMBRE") ?: ""
        correo = intent.getStringExtra("CORREO") ?: ""
        telefono = intent.getStringExtra("TELEFONO") ?: ""

        // Manejo del switch para denuncia anónima
        switchAnonimo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etNombre.setText("")
                etCorreo.setText("")
                etTelefono.setText("")
                etNombre.isEnabled = false
                etCorreo.isEnabled = false
                etTelefono.isEnabled = false
            } else {
                etNombre.isEnabled = true
                etCorreo.isEnabled = true
                etTelefono.isEnabled = true
            }
        }

        // Botón para enviar la denuncia
        btnDenunciar.setOnClickListener {
            // Validación de campos cuando no es anónima
            if (!switchAnonimo.isChecked && (etNombre.text.isEmpty() || etCorreo.text.isEmpty() || etTelefono.text.isEmpty())) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Obtener el sexo seleccionado
                val selectedSexo = when (radioGroupSexo.checkedRadioButtonId) {
                    R.id.rbFemenino -> "Femenino"
                    R.id.rbMasculino -> "Masculino"
                    R.id.rbOtro -> "Otro"
                    else -> "No especificado"
                }

                // Verificar si el permiso ya ha sido otorgado
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Si no se ha concedido, solicitar el permiso
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                } else {
                    // Permiso ya concedido, generar el PDF
                    generatePDF(selectedSexo)
                }

                // Mostrar el mensaje de confirmación
                Toast.makeText(this, "Denuncia enviada como: $selectedSexo", Toast.LENGTH_LONG).show()

                // Generar el folio
                val folio = generateFolio()

                // Pasar el folio a la siguiente pantalla
                val intent = Intent(this, pasofinal::class.java)
                intent.putExtra("FOLIO", folio) // Enviar el folio como extra
                startActivity(intent)
            }
        }
    }

    // Método para generar un folio aleatorio
    private fun generateFolio(): String {
        return (1000..9999).random().toString()
    }

    // Método para generar el PDF
    private fun generatePDF(sexo: String) {
        val document = PdfDocument()
        val paint = Paint()

        // Crear una página del documento
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        paint.textSize = 12f

        // Escribir los datos de la denuncia en el PDF
        canvas.drawText("Nombre: $nombre", 20f, 50f, paint)
        canvas.drawText("Correo: $correo", 20f, 70f, paint)
        canvas.drawText("Telefono: $telefono", 20f, 90f, paint)
        canvas.drawText("Sexo: $sexo", 20f, 110f, paint)

        // Aquí puedes agregar más campos si lo necesitas

        document.finishPage(page)

        // Guardar el archivo PDF en el almacenamiento
        try {
            val file = File(Environment.getExternalStorageDirectory(), "denuncia_${System.currentTimeMillis()}.pdf")
            document.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF generado: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }

    // Manejo de la respuesta de los permisos solicitados
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue otorgado, generar el PDF
                val selectedSexo = when (radioGroupSexo.checkedRadioButtonId) {
                    R.id.rbFemenino -> "Femenino"
                    R.id.rbMasculino -> "Masculino"
                    R.id.rbOtro -> "Otro"
                    else -> "No especificado"
                }
                generatePDF(selectedSexo) // Generar el PDF después de otorgar el permiso
            } else {
                // El permiso fue denegado, manejar el error
                Toast.makeText(this, "Permiso denegado para guardar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
