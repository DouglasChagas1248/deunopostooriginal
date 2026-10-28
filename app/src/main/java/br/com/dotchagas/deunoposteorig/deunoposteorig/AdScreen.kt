package br.com.dotchagas.deunoposteorig.deunoposteorig

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/** Applies system insets once, above both the banner and the scrollable content. */
@Suppress("DEPRECATION")
internal fun AppCompatActivity.configureAdScreen() {
    val root = findViewById<View>(R.id.adScreenRoot)
    setSupportActionBar(findViewById<Toolbar>(R.id.toolbarAnuncios))
    supportActionBar?.apply {
        setDisplayShowHomeEnabled(true)
        setIcon(R.drawable.ic_launcher)
    }

    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Color.TRANSPARENT
    } else {
        Color.BLACK
    }
    WindowCompat.getInsetsController(window, root).apply {
        isAppearanceLightStatusBars = true
        isAppearanceLightNavigationBars = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    val left = root.paddingLeft
    val top = root.paddingTop
    val right = root.paddingRight
    val bottom = root.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val safeArea = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(
            left + safeArea.left, top + safeArea.top,
            right + safeArea.right, bottom + safeArea.bottom
        )
        WindowInsetsCompat.CONSUMED
    }
    root.doOnAttach { ViewCompat.requestApplyInsets(it) }

    MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR, "B3EEABB8EE11C2BE770B684D95219ECB"))
            .build()
    )
    AdaptiveBanner(this, findViewById(R.id.adContainer))
}

/** Reserves the full SDK height before loading, including when the request fails. */
private class AdaptiveBanner(
    private val activity: AppCompatActivity,
    private val container: FrameLayout
) : DefaultLifecycleObserver {
    private var adView: AdView? = null
    private var requestedWidthDp = 0
    private var resumed = false
    private val layoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        loadForAvailableWidth()
    }

    init {
        container.addOnLayoutChangeListener(layoutListener)
        activity.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        resumed = true
        adView?.resume()
        loadForAvailableWidth()
    }

    override fun onPause(owner: LifecycleOwner) {
        resumed = false
        adView?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        container.removeOnLayoutChangeListener(layoutListener)
        container.removeAllViews()
        adView?.destroy()
        adView = null
        owner.lifecycle.removeObserver(this)
    }

    private fun loadForAvailableWidth() {
        if (!resumed) return
        val widthPx = container.width - container.paddingLeft - container.paddingRight
        val widthDp = (widthPx / container.resources.displayMetrics.density).toInt()
        if (widthDp <= 0 || widthDp == requestedWidthDp) return

        val size = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, widthDp)
        if (size.getHeightInPixels(activity) <= 0) return
        requestedWidthDp = widthDp

        container.removeAllViews()
        adView?.destroy()
        container.minimumHeight = size.getHeightInPixels(activity) +
                container.paddingTop + container.paddingBottom

        val debuggable = activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val banner = AdView(activity).apply {
            setBackgroundColor(Color.WHITE)
            setAdSize(size)
            adUnitId = if (debuggable) {
                "ca-app-pub-3940256099942544/9214589741"
            } else {
                activity.getString(R.string.banner_ad_unit_id)
            }
        }
        adView = banner
        container.addView(
            banner,
            FrameLayout.LayoutParams(
                size.getWidthInPixels(activity), size.getHeightInPixels(activity), Gravity.CENTER
            )
        )
        banner.loadAd(AdRequest.Builder().build())
    }
}
