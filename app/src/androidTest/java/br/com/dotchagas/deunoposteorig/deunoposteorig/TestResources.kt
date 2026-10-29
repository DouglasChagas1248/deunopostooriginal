package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.annotation.SuppressLint
import android.app.Activity

/** The optimized app may inline and remove its generated R classes. */
@SuppressLint("DiscouragedApi")
internal fun Activity.testResourceId(name: String): Int {
    val id = resources.getIdentifier(name, "id", packageName)
    check(id != 0) { "View resource missing from the optimized app: $name" }
    return id
}
