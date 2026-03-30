package com.syf.testkuikly

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.WHITE)

        // Material 3 风格标题栏
        val titleBarHeight = (56 * resources.displayMetrics.density).toInt()
        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                titleBarHeight
            )
            setPadding(4, 0, 16, 0)
            elevation = 2f
        }

        // 返回按钮
        val backButton = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.parseColor("#1C1B1F"))
            val size = (48 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setPadding(12, 12, 12, 12)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setOnClickListener {
                if (webView.canGoBack()) webView.goBack()
                else finish()
            }
        }
        titleBar.addView(backButton)

        // 标题
        val titleView = TextView(this).apply {
            this.text = title
            textSize = 16f
            setTextColor(Color.parseColor("#1C1B1F"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setSingleLine(true)
            setPadding(0, 0, 0, 0)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
            )
        }
        titleBar.addView(titleView)
        root.addView(titleBar)

        // 分隔线
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#E0E0E0"))
        }
        root.addView(divider, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 1
        ))

        // WebView
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            if (url.isNotEmpty()) loadUrl(url)
        }
        root.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        setContentView(root)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
