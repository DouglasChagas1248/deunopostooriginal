@file:Suppress("LocalVariableName")

package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class LoteriasCustomAdapter(
    private val _data: ArrayList<LoteriasDetails>,
    private val _c: Context
) : BaseAdapter() {

    override fun getCount(): Int {
        return _data.size
    }

    override fun getItem(position: Int): Any {
        return _data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(_c).inflate(R.layout.loterias_list_item, parent, false)

        val tvPremio = view.findViewById<TextView>(R.id.tvPremio)
        val tvNumpremio = view.findViewById<TextView>(R.id.tvnumpremio)

        val item = _data[position]

        val cpremio = item.getZDA_PREMIO().orEmpty() + " - "
        tvNumpremio.text = cpremio

        val cmilhar = item.getZDA_MILHAR().orEmpty()
        val cgrupo = getGrupo(cmilhar)
        tvPremio.text = "$cmilhar    - $cgrupo"

        return view
    }

    private fun getGrupo(zda_milhar: String?): String {
        if (zda_milhar.isNullOrBlank()) return ""

        val dezena = try {
            val milhar = zda_milhar.trim()
            if (milhar.length == 4) milhar.substring(2, 4)
            else milhar.substring(1, 3)
        } catch (e: Exception) {
            return ""
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
}