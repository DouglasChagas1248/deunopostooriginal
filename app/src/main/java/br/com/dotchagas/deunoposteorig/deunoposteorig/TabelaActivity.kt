package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TabelaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_tabela)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_launcher)
        }


    }

}
