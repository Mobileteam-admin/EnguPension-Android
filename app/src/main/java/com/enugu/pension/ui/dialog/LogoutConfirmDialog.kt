package com.enugu.pension.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.enugu.pension.databinding.LogoutDialogBinding
import com.enugu.pension.viewmodel.LogoutConfirmViewModel

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