package com.example.engu_pension_verification_application.ui.activity.WebView

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.engu_pension_verification_application.R

class ActiveDocWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_doc_view)

        /*webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()

        // Enable JavaScript and zoom support
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)

        // Load the URL passed from the intent
        val url = intent.getStringExtra("pdf url")
        if (url != null) {
            webView.loadUrl(url)
        }*/


        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()

        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }
        val url = intent.getStringExtra("file url")

        // Check if the URL is a PDF
        if (url != null) {
            if (url.endsWith(".pdf")) {
                // Use Google's PDF Viewer
                webView.loadUrl("http://docs.google.com/gview?embedded=true&url=$url")
            } else {
                // Assume the URL is an image and load it normally
                webView.loadUrl(url)
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }


}
