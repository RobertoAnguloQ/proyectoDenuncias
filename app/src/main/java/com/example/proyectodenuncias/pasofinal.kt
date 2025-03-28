package com.example.proyectodenuncias

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Image
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class pasofinal : AppCompatActivity() {
    private lateinit var tvFolio: TextView
    private lateinit var btnVolverInicio: Button
    private lateinit var btnSeguirFolio: Button
    private lateinit var btnGenerarPDF: Button
    private var fotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasofinal)

        // Inicializar vistas
        tvFolio = findViewById(R.id.tvFolio)
        btnVolverInicio = findViewById(R.id.btnVolverInicio)
        btnSeguirFolio = findViewById(R.id.btnSeguirFolio)
        btnGenerarPDF = findViewById(R.id.btnGenerarPDF)

        // Obtener datos del intent
        fotoPath = intent.getStringExtra("fotoPath")
        val folio = intent.getStringExtra("FOLIO") ?: generarFolioTemporal()
        tvFolio.text = "Su folio es: $folio"

        // Configurar listeners
        btnVolverInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        btnSeguirFolio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("FOLIO", folio)
            }
            startActivity(intent)
        }

        btnGenerarPDF.setOnClickListener {
            generarPdfCompleto(folio)
        }
    }

    private fun generarPdfCompleto(folio: String) {
        try {
            // 1. Verificar y crear directorio si no existe
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: run {
                Toast.makeText(this, "No se pudo acceder al almacenamiento", Toast.LENGTH_LONG).show()
                return
            }

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Toast.makeText(this, "No se pudo crear el directorio", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // 2. Crear archivo PDF
            val pdfFile = File(storageDir, "Denuncia_$folio.pdf")

            // 3. Verificar permisos de escritura
            if (pdfFile.exists() && !pdfFile.canWrite()) {
                Toast.makeText(this, "No se tienen permisos para modificar el archivo", Toast.LENGTH_LONG).show()
                return
            }

            // 4. Generar PDF con manejo seguro de recursos
            FileOutputStream(pdfFile).use { fos ->
                PdfWriter(fos).use { pdfWriter ->
                    PdfDocument(pdfWriter).use { pdfDocument ->
                        Document(pdfDocument).use { document ->
                            // Configuración de fuentes
                            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                            val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                            // Cabecera
                            document.add(Paragraph("DENUNCIA OFICIAL")
                                .setFont(boldFont)
                                .setFontSize(18f)
                                .setMarginBottom(10f))

                            document.add(Paragraph("Folio: $folio")
                                .setFont(boldFont)
                                .setFontSize(14f)
                                .setMarginBottom(15f))

                            // Datos del incidente (provenientes de pasos anteriores)
                            document.add(Paragraph("I. DATOS DEL INCIDENTE")
                                .setFont(boldFont)
                                .setFontSize(14f)
                                .setMarginBottom(5f))

                            addFormField(document, "Descripción:", intent.getStringExtra("descripcion"), font)
                            addFormField(document, "Lugar:", intent.getStringExtra("lugar"), font)
                            addFormField(document, "Fecha:", intent.getStringExtra("fecha"), font)
                            addFormField(document, "Hora:", intent.getStringExtra("hora"), font)

                            // Datos personales (provenientes del paso 4)
                            document.add(Paragraph("\nII. DATOS PERSONALES")
                                .setFont(boldFont)
                                .setFontSize(14f)
                                .setMarginBottom(5f))

                            addFormField(document, "Nombre:", intent.getStringExtra("nombre_anterior"), font)
                            addFormField(document, "Puesto:", intent.getStringExtra("puesto"), font)
                            addFormField(document, "Dependencia:", intent.getStringExtra("dependencia"), font)

                            // Datos de contacto (provenientes del paso 5)
                            document.add(Paragraph("\nIII. DATOS DE CONTACTO")
                                .setFont(boldFont)
                                .setFontSize(14f)
                                .setMarginBottom(5f))

                            val esAnonimo = intent.getBooleanExtra("anonimo", false)
                            if (esAnonimo) {
                                document.add(Paragraph("(Denuncia anónima)")
                                    .setFont(font)
                                    .setItalic()
                                    .setMarginBottom(10f))
                            } else {
                                addFormField(document, "Nombre:", intent.getStringExtra("nombre"), font)
                                addFormField(document, "Correo:", intent.getStringExtra("correo"), font)
                                addFormField(document, "Teléfono:", intent.getStringExtra("telefono"), font)
                                addFormField(document, "Sexo:", intent.getStringExtra("sexo"), font)
                            }

                            // Evidencia fotográfica
                            fotoPath?.let { path ->
                                try {
                                    document.add(Paragraph("\nIV. EVIDENCIA FOTOGRÁFICA")
                                        .setFont(boldFont)
                                        .setFontSize(14f)
                                        .setMarginBottom(5f))

                                    val imageData = ImageDataFactory.create(path)
                                    document.add(Image(imageData)
                                        .setAutoScale(true)
                                        .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER))
                                } catch (e: Exception) {
                                    Log.e("PDF", "Error al agregar imagen", e)
                                    document.add(Paragraph("[No se pudo cargar la imagen adjunta]")
                                        .setFont(font)
                                        .setItalic())
                                }
                            }
                        }
                    }
                }
            }

            Toast.makeText(this, "PDF guardado en: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
            compartirPDF(pdfFile)

        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de permisos: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("PDF", "SecurityException", e)
        } catch (e: IOException) {
            Toast.makeText(this, "Error de E/S: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("PDF", "IOException", e)
        } catch (e: Exception) {
            Toast.makeText(this, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("PDF", "Exception", e)
        }
    }

    private fun addFormField(document: Document, label: String, value: String?, font: PdfFont) {
        if (!value.isNullOrEmpty()) {
            document.add(Paragraph("$label $value").setFont(font).setMarginBottom(5f))
        }
    }

    private fun compartirPDF(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartir denuncia"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("PDF", "Error al compartir", e)
        }
    }

    private fun generarFolioTemporal(): String {
        return "TEMP-${SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())}"
    }
}