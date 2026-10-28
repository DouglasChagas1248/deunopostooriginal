package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.net.toUri

class HistoriaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historia)

        // Configuração da ActionBar
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_launcher)
        }

        // Encontrar a TextView pelo ID e configurar o OnClickListener
        val tvwiki: TextView = findViewById(R.id.tvwilki)

        tvwiki.setOnClickListener {
            val url = "https://pt.wikipedia.org/wiki/Jogo_do_bicho"
            val uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}