package com.example.proyectodenuncias
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class pasofinal : AppCompatActivity() {

    private lateinit var tvFolio: TextView
    private lateinit var btnVolverInicio: Button
    private lateinit var btnSeguirFolio: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasofinal)

        tvFolio = findViewById(R.id.tvFolio)
        btnVolverInicio = findViewById(R.id.btnVolverInicio)
        btnSeguirFolio = findViewById(R.id.btnSeguirFolio)

        // Recibir folio de la denuncia (suponiendo que se pasa como extra en Intent)
        val folio = intent.getStringExtra("FOLIO") ?: "No disponible"
        tvFolio.text = "Su folio es: $folio"  // Mostrar el folio recibido

        // Botón para volver al inicio
        btnVolverInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Botón para seguir folio (Redirige a pantalla de seguimiento)
        btnSeguirFolio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FOLIO", folio)
            startActivity(intent)
        }
    }
}
