@file:Suppress("UNCHECKED_CAST")

package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import android.widget.TextView
import java.util.*

class SonhosCustomAdapter(
    private val _data: ArrayList<SonhosDetails>,
    private val _c: Context
) : BaseAdapter(), SectionIndexer {

    private val alphaIndexer: HashMap<String, Int> = HashMap()
    private var sections: Array<String>

    init {
        for (x in _data.indices) {
            val msg = _data[x]
            val s = msg.getZDB_CHAVE()
            if (!s.isNullOrEmpty()) {
                val ch = s.substring(0, 1).uppercase(Locale.getDefault())
                if (!alphaIndexer.containsKey(ch)) {
                    alphaIndexer[ch] = x
                }
            }
        }
        val sectionLetters: Set<String> = alphaIndexer.keys
        val sectionList = ArrayList(sectionLetters)
        sectionList.sort()
        sections = sectionList.toTypedArray()
    }

    override fun getSections(): Array<Any> {
        return sections as Array<Any>
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return alphaIndexer[sections[sectionIndex]] ?: 0
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    override fun getCount(): Int {
        return _data.size
    }

    override fun getItem(position: Int): Any {
        return _data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(_c)
            .inflate(R.layout.sonhos_list_item, parent, false)

        val tvIndice = view.findViewById<TextView>(R.id.tvIndice)
        val msg = _data[position]
        val cSonho = msg.getZDB_CHAVE()
        tvIndice.text = cSonho

        return view
    }
}