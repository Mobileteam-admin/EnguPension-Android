package com.enugu.pension.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.enugu.pension.R
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.databinding.ActivityVideoCallBinding

class VideoCallActivity : BaseActivity() {
    companion object {
        const val EXTRA_URL = "url"
    }

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var appName: String


    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appName = getString(R.string.app_name)
        initBackNav()
        checkPermissions()
    }

    private fun initBackNav() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBackAlert()
            }
        })
        binding.ivBack.setOnClickListener{ showBackAlert() }
    }
    private fun showBackAlert() {
        showAlertDialog(
            message = getString(R.string.discard_video_call_message),
            positiveTextId = R.string.yes,
            negativeTextId = R.string.no,
            onPositiveClick = ::finish,
            onNegativeClick = {},
            isCancellable = false
        )
    }
    private fun checkPermissions() {
        if (hasAllPermissions()) {
            initWebView()
        } else {
            showAlertDialog(
                title = getString(R.string.permissions_required),
                message = getString(R.string.video_call_permissions_request, appName),
                positiveTextId = R.string.allow,
                negativeTextId = R.string.deny,
                onPositiveClick = ::requestPermissions,
                onNegativeClick = ::onDeny,
                isCancellable = false
            )
        }
    }

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (shouldShowRationale()) {
            showRationaleDialog()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun showRationaleDialog() {
        showAlertDialog(
            title = getString(R.string.permissions_required),
            message = getString(R.string.video_call_permissions_rationale),
            positiveTextId = R.string.grant_permissions,
            negativeTextId = R.string.deny,
            onPositiveClick = { permissionLauncher.launch(requiredPermissions) },
            onNegativeClick = ::onDeny,
            isCancellable = false
        )
    }

    private fun shouldShowRationale(): Boolean {
        return requiredPermissions.any {
            shouldShowRequestPermissionRationale(it)
        }
    }

    private fun handlePermissionDenied() {
        if (shouldShowRationale()) {
            showRationaleDialog()
        } else {
            showAlertDialog(
                title = getString(R.string.permissions_denied),
                message = getString(R.string.video_call_permissions_denied_message),
                positiveTextId = R.string.go_to_settings,
                negativeTextId = R.string.go_back,
                onPositiveClick = ::openAppSettings,
                onNegativeClick = ::onDeny,
                isCancellable = false
            )
        }
    }

    private fun openAppSettings() {
        finish()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun onDeny() {
//        showToast("denied,.)
        finish()
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            initWebView()
        } else {
            handlePermissionDenied()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView() {
        val url = intent.getStringExtra(EXTRA_URL)!!
        binding.webView.settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            loadWithOverviewMode = false
            useWideViewPort = false
            builtInZoomControls = true
            displayZoomControls = false
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
        }
        /*binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.i("VideoCallActivity", "onPageStarted: $url")
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("document.body.style.zoom = '100%';", null)
                Log.i("VideoCallActivity", "onPageFinished: $url")
            }
        }*/
        binding.webView.loadUrl(url)
    }
}