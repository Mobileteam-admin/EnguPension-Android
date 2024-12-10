package com.example.engu_pension_verification_application.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.dialog.LoaderDialog
import com.example.engu_pension_verification_application.viewmodel.LoaderViewModel


open class BaseActivity : AppCompatActivity() {
    private val loaderViewModel by viewModels<LoaderViewModel>()
    private val loaderDialog by lazy { LoaderDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransitionAnimation()
        observeData()
    }

    private fun setTransitionAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun observeData() {
        loaderViewModel.isLoading.observe(this) { isLoading ->
            try {
                if (isLoading) {
                    try {
                        supportFragmentManager.executePendingTransactions()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (!loaderDialog.isAdded && supportFragmentManager.findFragmentByTag(
                            LoaderDialog.TAG
                        ) == null
                    ) {
                        loaderDialog.show(supportFragmentManager, LoaderDialog.TAG)
                    }
                } else {
                    if (loaderDialog.isAdded) {
                        loaderDialog.dismiss()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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