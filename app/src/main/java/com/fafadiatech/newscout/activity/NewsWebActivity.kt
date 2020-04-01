package com.fafadiatech.newscout.activity

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import com.fafadiatech.newscout.R

class NewsWebActivity : BaseActivity() {

    lateinit var urlLink: String
    val activity: Activity = this
    lateinit var pBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        pBar = findViewById(R.id.progress_bar)
        pBar.max = 100
        pBar.progress = 1
        var browser: CustomWebView = findViewById(R.id.webView) as CustomWebView
        var webSettings: WebSettings = browser.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.pluginState = WebSettings.PluginState.ON
        urlLink = intent.getStringExtra("url_link")?:""

        browser.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                pBar.progress = progress
                if (progress == 100) {
                    pBar.visibility = View.GONE
                }
            }
        }

        browser.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                pBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pBar.visibility = View.GONE
            }
        }

        browser.loadUrl(urlLink)
        browser.setGestureDetector(GestureDetector(this, CustomeGestureDetector()))
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    private inner class MyWebViewClient : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return false
        }
    }


    inner class CustomeGestureDetector : SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null) return false
            if (e1.pointerCount > 1 || e2.pointerCount > 1)
                return false
            else {
                try {
                    if (e1.x - e2.x > 100 && Math.abs(velocityX) > 800) {

                        return true
                    } else if (e2.x - e1.x > 100 && e1.y - e2.y < 200 && Math.abs(velocityX) > 300) {
                        onBackPressed()
                        return true
                    } else if (e2.y - e1.y > 100 && Math.abs(velocityY) > 800) {

                        return true
                    }
                } catch (e: Exception) {
                }

                return false
            }
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        super.onNetworkConnectionChanged(isConnected)
    }
}