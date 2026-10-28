package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DataBase(contexto: Context) : SQLiteOpenHelper(contexto, BANCO_DADOS, null, VERSAO) {

    companion object {
        const val BANCO_DADOS = "DB004"
        const val VERSAO = 1
        const val ZDA = "ZDA"
        const val ZDB = "ZDB"
        const val AVISO = "AVISO"
        const val NOTIFICACAO = "NOTIFICACAO"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $ZDA (_id INTEGER PRIMARY KEY," +
                " ZDA_DATA TEXT," +
                " ZDA_LOTER TEXT," +
                " ZDA_PREMIO TEXT," +
                " ZDA_MILHAR TEXT);")
        db.execSQL("CREATE TABLE $ZDB (_id INTEGER PRIMARY KEY," +
                " ZDB_CHAVE TEXT," +
                " ZDB_TEXTO1 TEXT," +
                " ZDB_TEXTO2 TEXT);")
        db.execSQL("CREATE TABLE $AVISO (_id INTEGER PRIMARY KEY," +
                " AVISO_MARCADO TEXT);")
        db.execSQL("CREATE TABLE $NOTIFICACAO (_id INTEGER PRIMARY KEY," +
                " NOTIFICACAO_MARCADO TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrade logic if needed
    }

    suspend fun getLoteria(data: String, loteria: String, context: Context, progressBar: ProgressBar): List<LoteriasDetails> =
        withContext(Dispatchers.IO) {
            getresultados(data, context, loteria, progressBar)

            val loteriasDetailsList = consultaLoteria(data, loteria)
            if (loteriasDetailsList.isEmpty() && loteria == "PTN") {
                return@withContext consultaLoteria(data, "FED")
            }

            loteriasDetailsList
        }

    private fun consultaLoteria(data: String, loteria: String): MutableList<LoteriasDetails> {
        val loteriasDetailsList = mutableListOf<LoteriasDetails>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $ZDA WHERE ZDA_DATA = ? AND ZDA_LOTER = ?", arrayOf(data, loteria))
        cursor.use {
            while (cursor.moveToNext()) {
                val loteriasDetails = LoteriasDetails().apply {
                    setZDA_DATA(cursor.getString(1))
                    setZDA_LOTER(cursor.getString(2))
                    setZDA_PREMIO(cursor.getString(3))
                    setZDA_MILHAR(cursor.getString(4))
                }
                loteriasDetailsList.add(loteriasDetails)
            }
        }
        return loteriasDetailsList
    }

    private suspend fun getresultados(param: String, context: Context, loteria: String, progressBar: ProgressBar) =
        withContext(Dispatchers.IO) {
            val conecta = LoteriasRest(context, progressBar)
            val qtd = getVerificaResultado(param, loteria)
            if (qtd < 7) {
                try {
                    conecta.execute(param)
                    delay(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private suspend fun getVerificaResultado(codigo: String, loteria: String): Int =
        withContext(Dispatchers.IO) {
            readableDatabase.rawQuery("SELECT COUNT(*) FROM $ZDA WHERE ZDA_DATA = ? AND ZDA_LOTER = ?", arrayOf(codigo, loteria)).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        }

    suspend fun setAviso(valor: String): Boolean =
        withContext(Dispatchers.IO) {
            val db = writableDatabase
            db.delete(AVISO, null, null)
            val values = ContentValues().apply { put("AVISO_MARCADO", valor) }
            db.insert(AVISO, null, values) != -1L
        }

    suspend fun getAviso(): String =
        withContext(Dispatchers.IO) {
            readableDatabase.rawQuery("SELECT MAX(AVISO_MARCADO) FROM $AVISO", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) ?: "N" else "N"
            }
        }

    suspend fun getSignificado(indice: String, context: Context): List<SonhosDetails> = withContext(Dispatchers.IO) {
        var vazio = false

        val selectQueryInicial = "SELECT * FROM $ZDB WHERE ZDB_CHAVE = '${indice.uppercase()}'"
        var db = this@DataBase.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQueryInicial, null)
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(2) == null) {
                        vazio = true
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        if (vazio) {
            val conecta = SonhosRest(context)
            try {
                conecta.execute(indice)
                delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sonhosDetailsList = ArrayList<SonhosDetails>()
        val selectQueryFinal = "SELECT * FROM $ZDB WHERE ZDB_CHAVE = '${indice.uppercase()}'"
        db = this@DataBase.readableDatabase
        cursor = null

        try {
            cursor = db.rawQuery(selectQueryFinal, null)
            if (cursor.moveToFirst()) {
                do {
                    val sonhosDetails = SonhosDetails().apply {
                        setZDB_CHAVE(cursor.getString(1))
                        setZDB_TEXTO1(cursor.getString(2))
                        setZDB_TEXTO2(cursor.getString(3))
                    }
                    sonhosDetailsList.add(sonhosDetails)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        db.close()
        return@withContext sonhosDetailsList
    }

    suspend fun getIndice(context: Context, progressBar: ProgressBar): List<SonhosDetails> = withContext(Dispatchers.IO) {
        val qtd = getVerificaIndice()
        if (qtd <= 0) {
            val conecta = IndiceRest(context, progressBar)
            try {
                conecta.execute()
                //delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sonhosDetailsList = ArrayList<SonhosDetails>()
        val selectQuery = "SELECT * FROM $ZDB"

        val db = this@DataBase.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val sonhosDetails = SonhosDetails().apply {
                        setZDB_CHAVE(cursor.getString(1))
                        setZDB_TEXTO1(cursor.getString(2))
                        setZDB_TEXTO2(cursor.getString(3))
                    }
                    sonhosDetailsList.add(sonhosDetails)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
            db.close()
        }

        return@withContext sonhosDetailsList
    }

    private suspend fun getVerificaIndice(): Int = withContext(Dispatchers.IO) {
        var quantidade = 0
        val selectQuery = "SELECT COUNT(*) FROM $ZDB"

        val db = this@DataBase.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    quantidade = cursor.getInt(0)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
            db.close()
        }

        return@withContext quantidade
    }
}
