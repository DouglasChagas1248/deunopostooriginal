package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.ContentValues
import android.content.Intent
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Run on a test device; also run against the optimized release signed for local testing. */
@RunWith(AndroidJUnit4::class)
class OptimizationSmokeInstrumentedTest {
    @Test fun lotteryButtonsStillResolveTheirXmlClickHandler() {
        ActivityScenario.launch(PrincipalActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val names = listOf("rbPTM", "rbPT", "rbPTV", "rbPTN", "rbCOR", "rbPPT")
                names.forEach { name ->
                    val button = activity.findViewById<RadioButton>(activity.testResourceId(name))
                    assertTrue("The XML click handler was not invoked", button.performClick())
                    assertTrue("The lottery was not selected", button.isChecked)
                }
            }
        }
    }

    @Test fun cachedDreamMeaningAndShareActionSurviveOptimization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val key = "TESTE R8 ${System.nanoTime()}"
        val meaning = "Significado local para verificar a compilação otimizada."
        val prediction = "Grupo de teste"
        DataBase(context).use { helper ->
            helper.writableDatabase.insertOrThrow(DataBase.ZDB, null, ContentValues().apply {
                put("ZDB_CHAVE", key)
                put("ZDB_TEXTO1", meaning)
                put("ZDB_TEXTO2", prediction)
            })
        }
        try {
            val intent = Intent(context, SignificadoActivity::class.java).putExtra("indice", key)
            ActivityScenario.launch<SignificadoActivity>(intent).use { scenario ->
                val loaded = CountDownLatch(1)
                scenario.onActivity { activity ->
                    val share = activity.findViewById<Button>(activity.testResourceId("btCompPalpite"))
                    val text = activity.findViewById<TextView>(activity.testResourceId("tvSignificado"))
                    // The screen loads its SQLite cache asynchronously. Wait for the rendered result.
                    val observer = object : android.view.ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            if (share.isEnabled && text.text.toString() == meaning) {
                                text.viewTreeObserver.removeOnPreDrawListener(this)
                                loaded.countDown()
                            }
                            return true
                        }
                    }
                    text.viewTreeObserver.addOnPreDrawListener(observer)
                    text.invalidate()
                }
                assertTrue("The cached meaning was not loaded", loaded.await(15, TimeUnit.SECONDS))
                scenario.onActivity { activity ->
                    assertEquals(key, activity.findViewById<TextView>(activity.testResourceId("tvIndiceS")).text.toString())
                    assertEquals(prediction, activity.findViewById<TextView>(activity.testResourceId("tvPalpite")).text.toString())
                    assertTrue(activity.findViewById<Button>(activity.testResourceId("btCompPalpite")).isEnabled)
                }
            }
        } finally {
            DataBase(context).use { helper ->
                helper.writableDatabase.delete(DataBase.ZDB, "ZDB_CHAVE = ?", arrayOf(key))
            }
        }
    }
}
