package com.example.engu_pension_verification_application.ui.activity.WebView

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_stripe_web_view.webView

class StripeWebViewActivity : BaseActivity() {
    companion object {
        const val EXTRA_URL = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stripe_web_view)
        init()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        val url = intent.getStringExtra(EXTRA_URL)!!
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val prefix = "session_id="
                if (url?.contains(prefix) == true) {
                    val sessionId = url.substring(url.indexOf(prefix)).removePrefix(prefix)
                    val resultIntent = Intent()
                    resultIntent.putExtra(AppConstants.SESSION_ID, sessionId)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
        webView.loadUrl(url)
    }
}