package com.example.proyectodenuncias

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class paso_3 : AppCompatActivity() {

    private lateinit var etDescripcion: EditText
    private lateinit var etLugar: EditText
    private lateinit var btnHora: Button
    private lateinit var btnFecha: Button
    private lateinit var btnAdjuntar: Button
    private lateinit var btnTomarFoto: Button
    private lateinit var btnContinuar: Button
    private lateinit var ivPreview: ImageView

    private var selectedTime: String? = null
    private var selectedDate: String? = null
    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2
    private val CAMERA_PERMISSION_REQUEST = 100

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso3)

        etDescripcion = findViewById(R.id.etDescripcion)
        etLugar = findViewById(R.id.etLugar)
        btnHora = findViewById(R.id.btnHora)
        btnFecha = findViewById(R.id.btnFecha)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnContinuar = findViewById(R.id.btnContinuar)
        ivPreview = findViewById(R.id.ivPreview)

        val calendar = Calendar.getInstance()

        btnHora.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                btnHora.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnFecha.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                btnFecha.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnAdjuntar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnTomarFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
            } else {
                abrirCamara()
            }
        }

        btnContinuar.setOnClickListener {
            if (validarCampos()) {
                startActivity(Intent(this, paso_4::class.java))
            }
        }
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                crearArchivoImagen()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.proyectodenuncias.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, CAMERA_REQUEST)
            } else {
                Toast.makeText(this, "No se pudo crear el archivo de imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun crearArchivoImagen(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = externalCacheDir
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir).apply {
            photoUri = FileProvider.getUriForFile(this@paso_3, "com.example.proyectodenuncias.fileprovider", this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri: Uri? = data?.data
                    ivPreview.setImageURI(imageUri)
                }
                CAMERA_REQUEST -> {
                    photoUri?.let {
                        ivPreview.setImageURI(it)
                    } ?: run {
                        Toast.makeText(this, "Error al obtener la foto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val descripcion = etDescripcion.text.toString().trim()
        val lugar = etLugar.text.toString().trim()

        if (descripcion.isEmpty()) {
            etDescripcion.error = "Ingrese una descripción"
            etDescripcion.requestFocus()
            return false
        }
        if (lugar.isEmpty()) {
            etLugar.error = "Ingrese el lugar del incidente"
            etLugar.requestFocus()
            return false
        }
        if (selectedDate == null) {
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedTime == null) {
            Toast.makeText(this, "Seleccione una hora", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}