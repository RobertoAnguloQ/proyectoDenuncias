package com.example.proyectodenuncias

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class pasofinal : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    private lateinit var tvFolio: TextView
    private lateinit var btnVolverInicio: Button
    private lateinit var btnSeguirFolio: Button
    private lateinit var btnGenerarPDF: Button
    private var fotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasofinal)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvFolio = findViewById(R.id.tvFolio)
        btnVolverInicio = findViewById(R.id.btnVolverInicio)
        btnSeguirFolio = findViewById(R.id.btnSeguirFolio)
        btnGenerarPDF = findViewById(R.id.btnGenerarPDF)

        fotoPath = intent.getStringExtra("fotoPath")
        val folio = intent.getStringExtra("FOLIO") ?: generarFolioTemporal()
        tvFolio.text = "Su folio es: $folio"
    }

    private fun setupListeners() {
        btnVolverInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        btnSeguirFolio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("FOLIO", intent.getStringExtra("FOLIO") ?: generarFolioTemporal())
            }
            startActivity(intent)
        }

        btnGenerarPDF.setOnClickListener {
            if (verificarPermisos()) {
                generarPdfCompleto(intent.getStringExtra("FOLIO") ?: generarFolioTemporal())
            } else {
                solicitarPermisos()
            }
        }
    }

    private fun verificarPermisos(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun solicitarPermisos() {
        ActivityCompat.requestPermissions(
            this,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            },
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    generarPdfCompleto(intent.getStringExtra("FOLIO") ?: generarFolioTemporal())
                } else {
                    Toast.makeText(this, "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generarPdfCompleto(folio: String) {
        try {
            val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Denuncias")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val pdfFile = File(storageDir, "Denuncia_$folio.pdf")

            PdfWriter(pdfFile).use { writer ->
                PdfDocument(writer).use { pdfDocument ->
                    Document(pdfDocument).use { document ->
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

                        // Datos del incidente
                        document.add(Paragraph("I. DATOS DEL INCIDENTE")
                            .setFont(boldFont)
                            .setFontSize(14f)
                            .setMarginBottom(5f))

                        addFormField(document, "Descripción:", intent.getStringExtra("descripcion"), font)
                        addFormField(document, "Lugar:", intent.getStringExtra("lugar"), font)
                        addFormField(document, "Fecha:", intent.getStringExtra("fecha"), font)
                        addFormField(document, "Hora:", intent.getStringExtra("hora"), font)

                        // Datos personales
                        document.add(Paragraph("\nII. DATOS PERSONALES")
                            .setFont(boldFont)
                            .setFontSize(14f)
                            .setMarginBottom(5f))

                        addFormField(document, "Nombre:", intent.getStringExtra("nombre_anterior"), font)
                        addFormField(document, "Puesto:", intent.getStringExtra("puesto"), font)
                        addFormField(document, "Dependencia:", intent.getStringExtra("dependencia"), font)

                        // Datos de contacto
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

            // Notificar al sistema sobre el nuevo archivo
            MediaScannerConnection.scanFile(
                this,
                arrayOf(pdfFile.absolutePath),
                arrayOf("application/pdf"),
                null
            )

            Toast.makeText(this, "PDF generado correctamente", Toast.LENGTH_LONG).show()
            compartirPDF(pdfFile)

        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("PDF_ERROR", "Error al generar PDF", e)
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
                "${packageName}.fileprovider",  // Asegúrate que coincida con tu manifest
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Compartir denuncia"))
            } else {
                Toast.makeText(this, "No hay aplicaciones para compartir PDF", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            Log.e("PDF_SHARE", "Error al compartir PDF", e)
        }
    }

    private fun generarFolioTemporal(): String {
        return "TEMP-${SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())}"
    }
}