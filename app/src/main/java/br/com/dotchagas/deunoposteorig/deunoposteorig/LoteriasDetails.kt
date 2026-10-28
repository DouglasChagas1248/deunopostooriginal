@file:Suppress("PrivatePropertyName", "FunctionName")

package br.com.dotchagas.deunoposteorig.deunoposteorig

class LoteriasDetails {

    private var ZDA_DATA: String? = null
    private var ZDA_LOTER: String? = null
    private var ZDA_PREMIO: String? = null
    private var ZDA_MILHAR: String? = null

    fun getZDA_DATA(): String? {
        return ZDA_DATA
    }

    fun setZDA_DATA(value: String?) {
        ZDA_DATA = value
    }

    fun getZDA_LOTER(): String? {
        return ZDA_LOTER
    }

    fun setZDA_LOTER(value: String?) {
        ZDA_LOTER = value
    }

    fun getZDA_PREMIO(): String? {
        return ZDA_PREMIO
    }

    fun setZDA_PREMIO(value: String?) {
        ZDA_PREMIO = value
    }

    fun getZDA_MILHAR(): String? {
        return ZDA_MILHAR
    }

    fun setZDA_MILHAR(value: String?) {
        ZDA_MILHAR = value
    }
}
