package com.example.proyectodenuncias
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Asegúrate de que es el layout correcto

        val cardDenuncia = findViewById<CardView>(R.id.card_denuncia)

        cardDenuncia.setOnClickListener {
            val intent = Intent(this, Paso_dos::class.java)
            startActivity(intent)
        }
    }
}