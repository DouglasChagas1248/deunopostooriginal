package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var dbloterias: DataBase

    companion object {
        var isQuit = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbloterias = DataBase(this)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_launcher)
        }

        isQuit = false

        val i8 = Intent(this, PrincipalActivity::class.java)

        val btok = findViewById<Button>(R.id.btok)
        val cbaviso = findViewById<CheckBox>(R.id.cbaviso)

        btok.setOnClickListener {
            lifecycleScope.launch {
                if (cbaviso.isChecked) {
                    dbloterias.setAviso("S")
                }
                startActivity(i8)
            }
        }

        lifecycleScope.launch {
            val aviso = dbloterias.getAviso()
            if (aviso.trim() == "S") {
                startActivity(i8)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isQuit) {
            finish()
        }
    }

}
