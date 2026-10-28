package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class SonhosRest(private val _contexto: Context) {

    companion object {
        private const val ENDPOINT = "https://deunoposte-api.dotchagas.com.br/rest01/dp/dprsonhos"
        private val USER = BuildConfig.DP_API_USER
        private val PASSWORD = BuildConfig.DP_API_PASSWORD
    }

    suspend fun execute(sonho: String) = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(ENDPOINT)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")

            val credentials = "$USER:$PASSWORD"
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", auth)
            conn.doOutput = true

            val body = "{\"Sonho\":\"$sonho\"}"
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

            val json = JSONObject(response.toString())
            val dados: JSONArray = json.getJSONArray("dados")

            val dbloteriass = DataBase(_contexto)
            val db: SQLiteDatabase = dbloteriass.writableDatabase

            for (i in 0 until dados.length()) {
                val item = dados.getJSONObject(i)
                val values = ContentValues().apply {
                    put("ZDB_TEXTO1", item.getString("ZDB_TEXTO1").trim())
                    put("ZDB_TEXTO2", item.getString("ZDB_TEXTO2").trim())
                }
                val chave = item.getString("ZDB_CHAVE").trim()
                db.update("ZDB", values, "ZDB_CHAVE = ?", arrayOf(chave))
            }

            dbloteriass.close()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            conn?.disconnect()
        }
    }
}
