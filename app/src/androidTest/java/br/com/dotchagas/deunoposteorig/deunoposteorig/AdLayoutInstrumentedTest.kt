package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.ads.AdView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Checks actual measured geometry without depending on a successful ad/network request. */
@RunWith(AndroidJUnit4::class)
class AdLayoutInstrumentedTest {
    @Test fun resultadosReserveAnEntireBanner() = verifyScreen(PrincipalActivity::class.java)
    @Test fun sonhosReserveAnEntireBanner() = verifyScreen(SonhosActivity::class.java)
    @Test fun significadoReservesAnEntireBannerEvenWithoutCachedData() =
        verifyScreen(SignificadoActivity::class.java)

    private fun <T : Activity> verifyScreen(activityClass: Class<T>) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(instrumentation.targetContext, activityClass)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ActivityScenario.launch<T>(intent).use { scenario ->
            val drawn = CountDownLatch(1)
            val failure = AtomicReference<Throwable?>()
            scenario.onActivity { activity ->
                val root = activity.findViewById<View>(activity.testResourceId("adScreenRoot"))
                val slot = activity.findViewById<FrameLayout>(activity.testResourceId("adContainer"))
                root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        val banner = slot.getChildAt(0) as? AdView
                        if (banner == null || banner.width == 0 || banner.height == 0) return true
                        root.viewTreeObserver.removeOnPreDrawListener(this)
                        try {
                            val toolbar = activity.findViewById<View>(activity.testResourceId("toolbarAnuncios"))
                            val content = activity.findViewById<View>(activity.testResourceId("contentArea"))
                            assertTrue("Banner overlaps the toolbar", slot.top >= toolbar.bottom)
                            assertTrue("Content overlaps the banner", content.top >= slot.bottom)
                            assertTrue("Content has no usable space", content.height > 0)
                            val visible = Rect()
                            assertTrue("Banner is hidden", banner.getGlobalVisibleRect(visible))
                            assertEquals("Banner width is clipped", banner.width, visible.width())
                            assertEquals("Banner height is clipped", banner.height, visible.height())
                            assertTrue(banner.top >= slot.paddingTop)
                            assertTrue(banner.bottom <= slot.height - slot.paddingBottom)
                        } catch (error: Throwable) {
                            failure.set(error)
                        } finally {
                            drawn.countDown()
                        }
                        return true
                    }
                })
                root.invalidate()
            }
            assertTrue("The banner layout was not measured", drawn.await(15, TimeUnit.SECONDS))
            failure.get()?.let { throw it }
            instrumentation.waitForIdleSync()
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            if (screenshot != null) {
                val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "admob-validation")
                directory.mkdirs()
                File(directory, activityClass.simpleName + ".png").outputStream().use {
                    screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                screenshot.recycle()
            }
        }
    }
}
