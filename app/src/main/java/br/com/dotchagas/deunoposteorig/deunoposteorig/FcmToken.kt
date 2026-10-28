package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class FcmToken {

    companion object {
        private const val TAG = "FcmToken"
        private const val ENDPOINT = "https://deunoposte-api.dotchagas.com.br/rest01/dp/dprtoken"
        private val USER = BuildConfig.DP_API_USER
        private val PASSWORD = BuildConfig.DP_API_PASSWORD

        fun enviarToken(token: String) {
            CoroutineScope(Dispatchers.IO).launch {
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

                    val jsonParam = JSONObject()
                    jsonParam.put("token", token)

                    conn.outputStream.use { os: OutputStream ->
                        os.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
                        os.flush()
                    }

                    val responseCode = conn.responseCode
                    Log.d(TAG, "Resposta do servidor: $responseCode")

                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao enviar token FCM", e)
                } finally {
                    conn?.disconnect()
                }
            }
        }
    }
}
