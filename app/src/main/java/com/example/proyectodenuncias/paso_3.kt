package com.example.proyectodenuncias

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class paso_3 : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_REQUEST = 101
        private const val PICK_IMAGE_REQUEST = 1
        private const val CAMERA_PERMISSION_REQUEST = 102
    }

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
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Permisos necesarios denegados", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    ivPreview.setImageBitmap(bitmap)

                    // Notificar a la galería
                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(path),
                        arrayOf("image/jpeg"),
                        null
                    )
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la foto", Toast.LENGTH_SHORT).show()
                    Log.e("CameraError", "Error al mostrar foto", e)
                }
            }
        } else {
            Toast.makeText(this, "No se pudo guardar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso3)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etDescripcion = findViewById(R.id.etDescripcion)
        etLugar = findViewById(R.id.etLugar)
        btnHora = findViewById(R.id.btnHora)
        btnFecha = findViewById(R.id.btnFecha)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnContinuar = findViewById(R.id.btnContinuar)
        ivPreview = findViewById(R.id.ivPreview)
    }

    private fun setupListeners() {
        val calendar = Calendar.getInstance()

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
            } else {
                requestStoragePermission()
            }
        }

        btnTomarFoto.setOnClickListener {
            if (checkAllPermissions()) {
                dispatchTakePictureIntent()
            } else {
                requestNeededPermissions()
            }
        }

        btnContinuar.setOnClickListener {
            if (validarCampos()) {
                navigateToNextStep()
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        return checkCameraPermission() && checkStoragePermission()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
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

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        if (permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
            showPermissionExplanation(
                "Permisos requeridos",
                "La aplicación necesita acceso a la cámara y al almacenamiento para funcionar correctamente",
                permissions
            )
        } else {
            requestPermissions.launch(permissions)
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionExplanation(
                "Permiso requerido",
                "Necesitamos acceso al almacenamiento para seleccionar archivos",
                arrayOf(permission)
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                STORAGE_PERMISSION_REQUEST
            )
        }
    }

    private fun showPermissionExplanation(title: String, message: String, permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { _, _ ->
                requestPermissions.launch(permissions)
            }
            .setNegativeButton("Configuración") { _, _ ->
                openAppSettings()
            }
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            val photoFile = createImageFile().also {
                currentPhotoPath = it.absolutePath
            }

            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            ).also { uri ->
                // Otorgar permisos temporales a la app de cámara
                val resolveInfo = packageManager.resolveActivity(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                resolveInfo?.let {
                    grantUriPermission(
                        it.activityInfo.packageName,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                // Lanzar la actividad de la cámara
                takePicture.launch(uri)
            }
        } catch (ex: IOException) {
            Toast.makeText(this, "Error al crear archivo para la foto", Toast.LENGTH_SHORT).show()
            Log.e("CameraError", "Error al crear archivo", ex)
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al abrir la cámara: ${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e("CameraError", "Error general", ex)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ProyectoDenuncias").apply {
            if (!exists()) mkdirs()
        }

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        try {
                            currentPhotoPath = saveImageToTempFile(uri)
                            ivPreview.setImageURI(uri)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                            Log.e("PASO_3", "Error al cargar imagen", e)
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
            val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ProyectoDenuncias").apply {
                if (!exists()) mkdirs()
            }

            val tempFile = File.createTempFile(
                "TEMP_IMG_${timeStamp}_",
                ".jpg",
                storageDir
            )

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
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                }
            }
        }
    }

    private fun navigateToNextStep() {
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