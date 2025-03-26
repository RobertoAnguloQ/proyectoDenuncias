package com.example.proyectodenuncias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.*

class paso_3 : AppCompatActivity() {

    private lateinit var etDescripcion: EditText
    private lateinit var etLugar: EditText
    private lateinit var btnHora: Button
    private lateinit var btnFecha: Button
    private lateinit var btnAdjuntar: Button
    private lateinit var btnContinuar: Button

    private var selectedTime: String? = null
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso3)

        etDescripcion = findViewById(R.id.etDescripcion)
        etLugar = findViewById(R.id.etLugar)
        btnHora = findViewById(R.id.btnHora)
        btnFecha = findViewById(R.id.btnFecha)
        btnAdjuntar = findViewById(R.id.btnAdjuntar)
        btnContinuar = findViewById(R.id.btnContinuar)

        val calendar = Calendar.getInstance()

        btnHora.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    btnHora.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        btnFecha.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = "$day/${month + 1}/$year"
                    btnFecha.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnAdjuntar.setOnClickListener {
            Toast.makeText(this, "Función de adjuntar en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnContinuar.setOnClickListener {
            if (validarCampos()) {
                val intent = Intent(this, paso_4::class.java)
                startActivity(intent)
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