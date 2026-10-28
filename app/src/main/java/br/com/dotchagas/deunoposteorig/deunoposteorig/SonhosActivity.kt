package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ProgressBar

class SonhosActivity : AppCompatActivity() {

    private lateinit var lvSonhos: ListView
    private lateinit var sonhosdetails: ArrayList<SonhosDetails>
    private val context = this@SonhosActivity
    private lateinit var dbloterias: DataBase

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sonhos)
        configureAdScreen()

        progressBar = findViewById(R.id.progressBar)

        dbloterias = DataBase(context)

        lvSonhos = findViewById(R.id.lvSonhos)
        atualizaindice()

        lvSonhos.setOnItemClickListener { _, _, position, _ ->
            val item = sonhosdetails[position].getZDB_CHAVE()
            Toast.makeText(this@SonhosActivity, item, Toast.LENGTH_SHORT).show()

            val i = Intent(context, SignificadoActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putString("indice", item)
                })
            }

            startActivity(i)
        }
    }

    private fun atualizaindice() {
        lifecycleScope.launch {
            val sonhosList = withContext(Dispatchers.IO) {
                dbloterias.getIndice(context, progressBar)
            }

            sonhosdetails = ArrayList()
            for (ld in sonhosList) {
                val detail = SonhosDetails()
                ld.getZDB_CHAVE()?.let { detail.setZDB_CHAVE(it) }
                sonhosdetails.add(detail)
            }

            lvSonhos.adapter = SonhosCustomAdapter(sonhosdetails, context)
        }
    }

}
