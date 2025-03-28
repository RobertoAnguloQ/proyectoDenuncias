package com.example.proyectodenuncias

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
    private val STORAGE_PERMISSION_REQUEST = 101

    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso3)

        // Inicializar vistas
        etDescripcion = findViewById(R.id.etDescripcion)
        etLugar = findViewById(R.id.etLugar)
        btnHora = findViewById(R.id.btnHora)
        btnFecha = findViewById(R.id.btnFecha)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnContinuar = findViewById(R.id.btnContinuar)
        ivPreview = findViewById(R.id.ivPreview)

        val calendar = Calendar.getInstance()

        // Configurar listeners
        btnHora.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                btnHora.text = selectedTime
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnFecha.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = "$day/${month + 1}/$year"
                    btnFecha.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnAdjuntar.setOnClickListener {
            if (checkStoragePermission()) {
                openImagePicker()
            }
        }

        btnTomarFoto.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            }
        }

        btnContinuar.setOnClickListener {
            if (validarCampos()) {
                val intent = Intent(this, paso_4::class.java).apply {
                    putExtra("descripcion", etDescripcion.text.toString().trim())
                    putExtra("lugar", etLugar.text.toString().trim())
                    putExtra("fecha", selectedDate)
                    putExtra("hora", selectedTime)
                    currentPhotoPath?.let { path ->
                        putExtra("fotoPath", path)
                    }
                }
                startActivity(intent)
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
            false
        } else {
            true
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST
            )
            false
        } else {
            true
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        try {
                            // Guardar la imagen seleccionada en un archivo temporal
                            currentPhotoPath = saveImageToTempFile(uri)
                            ivPreview.setImageURI(uri)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                            Log.e("PASO_3", "Error al cargar imagen", e)
                        }
                    }
                }
                CAMERA_REQUEST -> {
                    photoUri?.let { uri ->
                        try {
                            ivPreview.setImageURI(uri)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al mostrar la foto", Toast.LENGTH_SHORT).show()
                            Log.e("PASO_3", "Error al mostrar foto", e)
                        }
                    }
                }
            }
        }
    }

    private fun saveImageToTempFile(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val tempFile = File(storageDir, "TEMP_IMG_$timeStamp.jpg")

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e("PASO_3", "Error al guardar imagen temporal", e)
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
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