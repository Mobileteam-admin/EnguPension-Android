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
import com.example.engu_pension_verification_application.databinding.LogoutDialogBinding
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel

class LogoutConfirmDialog : BaseDialog() {
    private lateinit var binding:LogoutDialogBinding
    private val viewModel by activityViewModels<LogoutConfirmViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LogoutDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.tvLogoutCancel.setOnClickListener {
            dismiss()
        }
        binding.tvLogoutConfirm.setOnClickListener {
            viewModel.logout.value = Unit
        }
    }
}