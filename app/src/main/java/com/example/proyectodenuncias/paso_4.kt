package com.example.proyectodenuncias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText

class paso_4 : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etPuesto: EditText
    private lateinit var etDependencia: EditText
    private lateinit var etLugar: EditText
    private lateinit var btnContinuar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso4)

        etNombre = findViewById(R.id.etNombre)
        etPuesto = findViewById(R.id.etPuesto)
        etDependencia = findViewById(R.id.etDependencia)
        etLugar = findViewById(R.id.etLugar)
        btnContinuar = findViewById(R.id.btnContinuar)

        btnContinuar.setOnClickListener {
            // Avanzar a la pantalla del paso 5 sin validaciones
            val intent = Intent(this, paso5::class.java)
            startActivity(intent)
        }
    }
}
