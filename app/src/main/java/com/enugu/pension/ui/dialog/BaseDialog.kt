package com.enugu.pension.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.SystemClock
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.enugu.pension.R
import com.enugu.pension.common.util.BaseUtils
import com.enugu.pension.viewmodel.LoaderViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel


open class BaseDialog : DialogFragment(), BaseUtils {
    companion object {
        const val BACK_PRESS_THRESHOLD = 2000
    }
    private val loaderViewModel by activityViewModels<com.enugu.pension.viewmodel.LoaderViewModel>()
    override fun provideContext() = requireContext()
    override fun provideActivity() = requireActivity()
    override fun provideFragmentManager() = parentFragmentManager
    override fun provideLoaderViewModel() = loaderViewModel
    private var lastBackPressTime: Long = 0L
    var dismissOnDoubleBackPress = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnShowListener {
            val window = dialog.window ?: return@setOnShowListener
            val background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.dimen_16)).build()
            ).apply {
                fillColor = context?.getColorStateList(R.color.white)
            }
            window.setBackgroundDrawable(background)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog?.setOnKeyListener { _, keyCode, event ->
            if (dismissOnDoubleBackPress && keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastBackPressTime < BACK_PRESS_THRESHOLD) {
                    dismiss()
                } else {
                    lastBackPressTime = currentTime
                    showToast(R.string.double_back_press_msg, duration = Toast.LENGTH_SHORT)
                }
                true
            } else {
                false
            }
        }
    }
}