package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class LoteriasRest(
    private val contexto: Context,
    private val progressBar: ProgressBar
) {

    companion object {
        private const val ENDPOINT = "https://deunoposte-api.dotchagas.com.br/rest01/dp/dprresult"
        private val USER = BuildConfig.DP_API_USER
        private val PASSWORD = BuildConfig.DP_API_PASSWORD
    }

    suspend fun execute(data: String): Boolean {
        return try {
            // Exibe o progressBar na thread principal
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
            }

            // Executa a sincronização real (REST + banco)
            val success = executarSincronizacao(data)

            // Oculta o progressBar e mostra erro se necessário
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (!success) {
                    Toast.makeText(contexto, "Erro ao sincronizar resultados.", Toast.LENGTH_LONG).show()
                }
            }

            success
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                Toast.makeText(contexto, "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }


    private suspend fun executarSincronizacao(data: String): Boolean {
        return try {
            val url = URL(ENDPOINT)
            val conn = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")

            val credentials = "$USER:$PASSWORD"
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", auth)
            conn.doOutput = true

            val body = "{\"Data\":\"$data\"}"
            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray())
                os.flush()
            }

            val response = StringBuilder()
            BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                var linha: String?
                while (br.readLine().also { linha = it } != null) {
                    response.append(linha)
                }
            }

            conn.disconnect()

            val json = JSONObject(response.toString())
            val retorno = json.getJSONArray("dados")

            val dbloteriass = DataBase(contexto)
            val dbloterias: SQLiteDatabase = dbloteriass.writableDatabase

            dbloterias.delete("ZDA", "ZDA_DATA = ?", arrayOf(data))

            for (i in 0 until retorno.length()) {
                val campos = retorno.getJSONObject(i)
                val values = ContentValues().apply {
                    put("ZDA_DATA", campos.getString("ZDA_DATA").trim())
                    put("ZDA_LOTER", campos.getString("ZDA_LOTER").trim())
                    put("ZDA_PREMIO", campos.getString("ZDA_PREMIO").trim())
                    put("ZDA_MILHAR", campos.getString("ZDA_MILHAR").trim())
                }
                dbloterias.insert("ZDA", null, values)
            }

            dbloteriass.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "${e.javaClass.simpleName}: ${e.message}"

            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Erro REST: $errorMsg", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
}
