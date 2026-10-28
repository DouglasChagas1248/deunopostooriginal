@file:Suppress("LocalVariableName")

package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrincipalActivity : AppCompatActivity() {
    private val dbloterias: DataBase = DataBase(this)
    private val context: PrincipalActivity = this
    private lateinit var listViewLoterias: ListView
    private lateinit var etData: EditText
    private lateinit var tvdiadasemana: TextView
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyyMMdd")
    private val c1 = Calendar.getInstance()
    private val c2 = Calendar.getInstance()
    private var data: String = sdf.format(c1.time)
    private var dayOfTheWeek: Int = c2.get(Calendar.DAY_OF_WEEK)
    private var loteria: String = "PPT"
    private var resultado: String = ""

    private lateinit var rbPPT: RadioButton
    private lateinit var rbPTM: RadioButton
    private lateinit var rbPT: RadioButton
    private lateinit var rbPTV: RadioButton
    private lateinit var rbPTN: RadioButton
    private lateinit var rbCOR: RadioButton

    //var token: String? = null
    //var emailaccount: String? = null
    //var phonenumber: String? = null

    @SuppressLint("DefaultLocale")
    override fun onResume() {
        super.onResume()
        atualizaresultados(data, loteria)
        val today = Calendar.getInstance()
        val datahoje = String.format("%02d", today.get(Calendar.DAY_OF_MONTH)) + "/" +
                String.format("%02d", today.get(Calendar.MONTH) + 1) + "/" +
                today.get(Calendar.YEAR)
        etData.setText(datahoje)
    }

    @SuppressLint("MissingInflatedId", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        configureAdScreen()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MainActivity.isQuit = true
                finish()
            }
        })

        listViewLoterias = findViewById(R.id.listViewLoterias)
        val cabecalho = layoutInflater.inflate(R.layout.resultados_cabecalho, listViewLoterias, false)
        val acoes = layoutInflater.inflate(R.layout.resultados_acoes, listViewLoterias, false)
        listViewLoterias.addHeaderView(cabecalho, null, false)
        listViewLoterias.addFooterView(acoes, null, false)
        listViewLoterias.adapter = LoteriasCustomAdapter(arrayListOf(), context)
        registerForContextMenu(listViewLoterias)

        val btCompartilhar = acoes.findViewById<Button>(R.id.btCompartilhar)
        etData = cabecalho.findViewById(R.id.etData)
        tvdiadasemana = cabecalho.findViewById(R.id.tvdiadasemana)

        btCompartilhar.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resultado)
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        val today = Calendar.getInstance()
        val datahoje = String.format("%02d", today.get(Calendar.DAY_OF_MONTH)) + "/" +
                String.format("%02d", today.get(Calendar.MONTH) + 1) + "/" +
                today.get(Calendar.YEAR)
        etData.setText(datahoje)

        etData.setOnClickListener { showDatePicker() }
        tvdiadasemana.setOnClickListener { showDatePicker() }

        rbPPT = cabecalho.findViewById(R.id.rbPPT)
        rbPTM = cabecalho.findViewById(R.id.rbPTM)
        rbPT = cabecalho.findViewById(R.id.rbPT)
        rbPTV = cabecalho.findViewById(R.id.rbPTV)
        rbPTN = cabecalho.findViewById(R.id.rbPTN)
        rbCOR = cabecalho.findViewById(R.id.rbCOR)

        intent.extras?.getString("loteria")?.let {
            loteria = it
            when (loteria) {
                "PPT" -> rbPPT.isChecked = true
                "PTM" -> rbPTM.isChecked = true
                "PT" -> rbPT.isChecked = true
                "PTV" -> rbPTV.isChecked = true
                "PTN" -> rbPTN.isChecked = true
                "COR" -> rbCOR.isChecked = true
            }
        }

        atualizaresultados(data, loteria)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("PrincipalActivity", "Erro ao obter token FCM", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("PrincipalActivity", "Token obtido manualmente: $token")
            FcmToken.enviarToken(token)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_padrao",
                "Notificações Padrão",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Canal padrão para notificações" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val btSonhos = acoes.findViewById<Button>(R.id.btSonhos)
        btSonhos.setOnClickListener {
            startActivity(Intent(this, SonhosActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PrincipalActivity", "Permissão de notificação concedida.")
            } else {
                Log.w("PrincipalActivity", "Permissão de notificação negada.")
            }
        }
    }

    private fun showDatePicker() {
        val calender = Calendar.getInstance()
        val args = Bundle()
        args.putInt("year", calender.get(Calendar.YEAR))
        args.putInt("month", calender.get(Calendar.MONTH))
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH))

        val date = DatePickerFragment(ondate)
        date.arguments = args
        date.show(supportFragmentManager, "Date Picker")
    }


    @SuppressLint("DefaultLocale")
    private val ondate = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val dataselecionada = String.format("%02d", dayOfMonth) + "/" + String.format("%02d", monthOfYear + 1) + "/" + year
        etData.setText(dataselecionada)
        c2.set(year, monthOfYear, dayOfMonth)
        dayOfTheWeek = c2.get(Calendar.DAY_OF_WEEK)
        data = "$year${String.format("%02d", monthOfYear + 1)}${String.format("%02d", dayOfMonth)}"

        if (rbPPT.isChecked) loteria = "PPT"
        if (rbPTM.isChecked) loteria = "PTM"
        if (rbPT.isChecked) loteria = "PT"
        if (rbPTV.isChecked) loteria = "PTV"
        if (rbPTN.isChecked) loteria = "PTN"
        if (rbCOR.isChecked) loteria = "COR"

        atualizaresultados(data, loteria)
    }

    private fun atualizaresultados(data: String, loteria: String) {
        if (conectado()) {
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            lifecycleScope.launch {
                val loteriasList = withContext(Dispatchers.IO) {
                    dbloterias.getLoteria(data, loteria, context, progressBar)
                }

                val loteriasdetails = ArrayList<LoteriasDetails>()
                val dataselecionada = etData.text.toString().trim()
                val loteriaResultado = loteriasList.firstOrNull()?.getZDA_LOTER() ?: loteria

                resultado = "Resultado do Bicho:\r\n" +
                        "Data:$dataselecionada - Loteria:$loteriaResultado\r\n"

                for (ld in loteriasList) {
                    val detail = LoteriasDetails().apply {
                        setZDA_DATA(ld.getZDA_DATA())
                        setZDA_LOTER(ld.getZDA_LOTER())
                        setZDA_PREMIO(ld.getZDA_PREMIO())
                        setZDA_MILHAR(ld.getZDA_MILHAR())
                    }

                    loteriasdetails.add(detail)

                    resultado += "${ld.getZDA_PREMIO() ?: ""}-${ld.getZDA_MILHAR() ?: ""}    - ${getGrupo(ld.getZDA_MILHAR() ?: "")}\r\n"
                }

                resultado += "Compartilhado pelo APP DeuNoPoste\r\n" +
                        "https://play.google.com/store/apps/details?id=br.com.dotchagas.deunoposteorig.deunoposteorig"

                tvdiadasemana.text = diadasemana()
                listViewLoterias.adapter = LoteriasCustomAdapter(loteriasdetails, context)
            }
        } else {
            Toast.makeText(context, "No Response - Verifique a Internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun diadasemana(): String {
        return when (dayOfTheWeek) {
            1 -> "Domingo"
            2 -> "Segunda-Feira"
            3 -> "Terça-Feira"
            4 -> "Quarta-Feira"
            5 -> "Quinta-Feira"
            6 -> "Sexta-Feira"
            7 -> "Sábado"
            else -> ""
        }
    }

    fun selecionaloteria(view: View) {
        val checked = (view as RadioButton).isChecked
        if (!checked) return

        when (view.id) {
            R.id.rbPPT -> loteria = "PPT"
            R.id.rbPTM -> loteria = "PTM"
            R.id.rbPT -> loteria = "PT"
            R.id.rbPTV -> loteria = "PTV"
            R.id.rbPTN -> {
                // Usa o dia da semana da data selecionada no DatePicker (já armazenado em `dayOfTheWeek`)
                loteria = if (dayOfTheWeek == Calendar.WEDNESDAY || dayOfTheWeek == Calendar.SATURDAY) {
                    "FED"
                } else {
                    "PTN"
                }
            }
            R.id.rbCOR -> loteria = "COR"
        }

        atualizaresultados(data, loteria)
    }

    private fun getGrupo(zda_milhar: String): String {
        val dezena = if (zda_milhar.trim().length == 4) {
            zda_milhar.trim().substring(2, 4)
        } else {
            zda_milhar.trim().substring(1, 3)
        }
        return when (dezena) {
            "01", "02", "03", "04" -> "01-Avestruz"
            "05", "06", "07", "08" -> "02-Águia"
            "09", "10", "11", "12" -> "03-Burro"
            "13", "14", "15", "16" -> "04-Borboleta"
            "17", "18", "19", "20" -> "05-Cachorro"
            "21", "22", "23", "24" -> "06-Cabra"
            "25", "26", "27", "28" -> "07-Carneiro"
            "29", "30", "31", "32" -> "08-Camelo"
            "33", "34", "35", "36" -> "09-Cobra"
            "37", "38", "39", "40" -> "10-Coelho"
            "41", "42", "43", "44" -> "11-Cavalo"
            "45", "46", "47", "48" -> "12-Elefante"
            "49", "50", "51", "52" -> "13-Galo"
            "53", "54", "55", "56" -> "14-Gato"
            "57", "58", "59", "60" -> "15-Jacaré"
            "61", "62", "63", "64" -> "16-Leão"
            "65", "66", "67", "68" -> "17-Macaco"
            "69", "70", "71", "72" -> "18-Porco"
            "73", "74", "75", "76" -> "19-Pavão"
            "77", "78", "79", "80" -> "20-Peru"
            "81", "82", "83", "84" -> "21-Touro"
            "85", "86", "87", "88" -> "22-Tigre"
            "89", "90", "91", "92" -> "23-Urso"
            "93", "94", "95", "96" -> "24-Veado"
            "97", "98", "99", "00" -> "25-Vaca"
            else -> ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_historia -> {
                startActivity(Intent(this, HistoriaActivity::class.java))
                false
            }
            R.id.menu_sonhos -> {
                startActivity(Intent(this, SonhosActivity::class.java))
                false
            }
            R.id.menu_tabela -> {
                startActivity(Intent(this, TabelaActivity::class.java))
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
/*
    private fun conectado(): Boolean {
        val conectivtyManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conectivtyManager.activeNetworkInfo
        return netInfo != null && netInfo.isAvailable && netInfo.isConnected
    }

 */
private fun conectado(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

}
