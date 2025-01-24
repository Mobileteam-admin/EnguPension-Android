package com.example.engu_pension_verification_application.ui.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionRequestActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PERMISSION = "extra_permission"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestedPermission = intent.getStringExtra(EXTRA_PERMISSION)
        if (requestedPermission == null) {
            finishWithResult(false)
        } else {
            when(requestedPermission){
                WRITE_EXTERNAL_STORAGE ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) finishWithResult(true)
                    else requestPermission(requestedPermission)
            }

        }
    }

    private fun requestPermission(permission: String) {
        val launcher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                finishWithResult(isGranted)
            }
        launcher.launch(permission)
    }

    private fun finishWithResult(isGranted: Boolean) {
        val resultIntent = Intent()
        setResult(if(isGranted) RESULT_OK else RESULT_CANCELED, resultIntent)
        finish()
    }
}
