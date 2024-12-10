package com.example.engu_pension_verification_application.ui.activity.WebView

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isInvisible
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.ActivityStripeWebViewBinding
import com.example.engu_pension_verification_application.ui.activity.BaseActivity

class StripeWebViewActivity : BaseActivity() {
    private lateinit var binding:ActivityStripeWebViewBinding
    companion object {
        const val EXTRA_URL = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStripeWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun init() {
        val url = intent.getStringExtra(EXTRA_URL)!!
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val prefix = "session_id="
                if (url?.contains(prefix) == true) {
                    binding.webView.isInvisible = true
                    val sessionId = url.substring(url.indexOf(prefix)).removePrefix(prefix)
                    val resultIntent = Intent()
                    resultIntent.putExtra(AppConstants.SESSION_ID, sessionId)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
        binding.webView.loadUrl(url)
    }
}