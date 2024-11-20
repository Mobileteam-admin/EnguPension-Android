package com.example.engu_pension_verification_application.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_cancel
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_confirm


open class BaseDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnShowListener {
            val window = dialog.window ?: return@setOnShowListener
            val background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.dimen_16))
                    .build()
            ).apply {
                fillColor = context?.getColorStateList(R.color.white)
            }
            window.setBackgroundDrawable(background)
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                (resources.displayMetrics.widthPixels * 0.9f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}