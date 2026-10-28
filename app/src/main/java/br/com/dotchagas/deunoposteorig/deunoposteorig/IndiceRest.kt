package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class IndiceRest(
    private val _contexto: Context,
    private val progressBar: ProgressBar
) {

    companion object {
        private const val ENDPOINT = "https://deunoposte-api.dotchagas.com.br/rest01/dp/dprindice"
        private val USER = BuildConfig.DP_API_USER
        private val PASSWORD = BuildConfig.DP_API_PASSWORD
    }

    suspend fun execute(): Boolean {
        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
        }

        val success = sincronizarIndice()

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            if (!success) {
                Toast.makeText(_contexto, "Erro ao sincronizar índice via REST.", Toast.LENGTH_LONG).show()
            }
        }

        return success
    }

    private suspend fun sincronizarIndice(): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val dbloteriass = DataBase(_contexto)
            val db: SQLiteDatabase = dbloteriass.readableDatabase

            val cursor: Cursor = db.rawQuery("SELECT COUNT(*) FROM ZDB", null)
            cursor.moveToFirst()
            val total = cursor.getInt(0)
            cursor.close()

            if (total > 0) {
                dbloteriass.close()
                return true
            }

            val url = URL(ENDPOINT)
            conn = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection

            // Melhorias: timeouts definidos
            conn.connectTimeout = 15000 // 15 segundos
            conn.readTimeout = 60000    // 60 segundos

            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")

            val credentials = "$USER:$PASSWORD"
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", auth)
            conn.doOutput = true

            val body = "{}"
            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray())
                os.flush()
            }

            // Melhorias: leitura direta da resposta
            val response = InputStreamReader(conn.inputStream).use { reader ->
                reader.readText()
            }

            conn.disconnect()

            val json = JSONObject(response)
            val dados: JSONArray = json.getJSONArray("dados")

            val dbWrite: SQLiteDatabase = dbloteriass.writableDatabase
            dbWrite.delete("ZDB", null, null)

            for (i in 0 until dados.length()) {
                val item = dados.getJSONObject(i)
                val values = ContentValues()
                values.put("ZDB_CHAVE", item.getString("ZDB_CHAVE").trim())
                dbWrite.insert("ZDB", null, values)
            }

            dbloteriass.close()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = "${e.javaClass.simpleName}: ${e.message}"
            withContext(Dispatchers.Main) {
                Toast.makeText(_contexto, "Erro REST: $errorMsg", Toast.LENGTH_LONG).show()
            }
            false
        } finally {
            conn?.disconnect()
        }
    }
}
