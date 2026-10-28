package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignificadoActivity : AppCompatActivity() {
    private val dbloterias = DataBase(this)
    private val context: SignificadoActivity = this

    private var palpite: String = ""
    private lateinit var indice: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_significado)
        configureAdScreen()

        val bundle = intent.extras
        indice = bundle?.getString("indice") ?: ""

        val tvIndice = findViewById<TextView>(R.id.tvIndiceS)
        val tvSignificado = findViewById<TextView>(R.id.tvSignificado)
        val tvPalpite = findViewById<TextView>(R.id.tvPalpite)
        val btCompPalpite = findViewById<Button>(R.id.btCompPalpite)
        tvIndice.text = indice
        btCompPalpite.isEnabled = false

        lifecycleScope.launch {
            val sonhosList = withContext(Dispatchers.IO) {
                dbloterias.getSignificado(indice, context)
            }

            val sonho = sonhosList.firstOrNull()
            if (sonho == null) {
                tvSignificado.setText(R.string.significado_indisponivel)
                return@launch
            }
            val chave = sonho.getZDB_CHAVE()?.trim()
            tvIndice.text = chave
            val t2 = sonho.getZDB_TEXTO1()
            tvSignificado.text = t2
            val t3 = sonho.getZDB_TEXTO2()
            tvPalpite.text = t3

            palpite = "Sonho com:" + chave + "\r\n" +
                    "Significado:" + t2 + "\r\n" +
                    "Palpite:" + t3 + "\r\n"

            palpite += "Compartilhado pelo APP DeuNoPoste\r\n" +
                    "https://play.google.com/store/apps/details?id=br.com.deunoposte.app"
            btCompPalpite.isEnabled = true
        }

        btCompPalpite.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, palpite)
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        /*
        sonhosdetails = ArrayList<SonhosDetails>()
        for (ld in sonhosList) {
            val detail = SonhosDetails()
            detail.setZDB_CHAVE(ld.getZDB_CHAVE())
            sonhosdetails.add(detail)
        }
        */
    }
}
