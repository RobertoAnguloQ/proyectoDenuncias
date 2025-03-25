package com.example.proyectodenuncias

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import android.widget.Button
import android.widget.TextView

class Paso_dos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso2)

        // Configuramos el Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Para mostrar el botón de "atrás" en la barra
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paso 2"

        val tvConoceABC = findViewById<TextView>(R.id.tvConoceABC)
        val btnServidorPublico = findViewById<Button>(R.id.btnServidorPublico)
        val btnVehiculo = findViewById<Button>(R.id.btnVehiculo)
        val btnServicio = findViewById<Button>(R.id.btnServicio)

        // Función para ir a Paso 3
        val irAPasoTres = {
            val intent = Intent(this, paso_3::class.java)
            startActivity(intent)
        }

        // Asignar la navegación a los botones
        btnServidorPublico.setOnClickListener { irAPasoTres() }
        btnVehiculo.setOnClickListener { irAPasoTres() }
        btnServicio.setOnClickListener { irAPasoTres() }
    }

    // Manejar la flecha de atrás en la Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
