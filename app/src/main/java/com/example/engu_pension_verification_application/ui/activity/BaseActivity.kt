package com.example.engu_pension_verification_application.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.engu_pension_verification_application.ui.dialog.LoaderDialog
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel


open class BaseActivity : AppCompatActivity() {
    private val loaderViewModel by viewModels<LoaderViewModel>()
    private val loaderDialog by lazy { LoaderDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeData()
    }

    private fun observeData() {
        loaderViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                if (!loaderDialog.isAdded) {
                    loaderDialog.show(supportFragmentManager, LoaderDialog.TAG)
                }
            } else {
                if (loaderDialog.isAdded) {
                    loaderDialog.dismiss()
                }
            }
        }
    }

    fun showLoader() {
        loaderViewModel.show()
    }

    fun dismissLoader() {
        loaderViewModel.dismiss()
    }
}