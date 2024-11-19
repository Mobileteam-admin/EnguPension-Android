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
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_cancel
import kotlinx.android.synthetic.main.logout_dialog.tv_logout_confirm


class LogoutConfirmDialog : DialogFragment() {
    private val viewModel by activityViewModels<LogoutConfirmViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.logout_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        tv_logout_cancel.setOnClickListener {
            dismiss()
        }
        tv_logout_confirm.setOnClickListener {
            viewModel.logout.value = Unit
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}