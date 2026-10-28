@file:Suppress("FunctionName")

package br.com.dotchagas.deunoposteorig.deunoposteorig

@Suppress("PrivatePropertyName")
class SonhosDetails {

    private var ZDB_CHAVE: String? = null
    private var ZDB_TEXTO1: String? = null
    private var ZDB_TEXTO2: String? = null

    fun setZDB_CHAVE(chave: String?) {
        ZDB_CHAVE = chave
    }

    fun setZDB_TEXTO1(texto1: String?) {
        ZDB_TEXTO1 = texto1
    }

    fun setZDB_TEXTO2(texto2: String?) {
        ZDB_TEXTO2 = texto2
    }

    fun getZDB_CHAVE(): String? = ZDB_CHAVE
    fun getZDB_TEXTO1(): String? = ZDB_TEXTO1
    fun getZDB_TEXTO2(): String? = ZDB_TEXTO2
}
