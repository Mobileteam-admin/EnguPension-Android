package com.enugu.pension.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.databinding.DialogSwiftConfirmationBinding
import com.enugu.pension.util.VerificationState
import com.enugu.pension.viewmodel.SwiftVerificationViewModel

class SwiftVerificationDialog : BaseDialog() {
    companion object {
        const val ARG_BANK_NAME = "arg_bank_name"
        const val ARG_BRANCH = "arg_branch"
        const val ARG_CITY = "arg_city"
    }

    private lateinit var binding:DialogSwiftConfirmationBinding
    private val swiftVerificationViewModel by activityViewModels<SwiftVerificationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogSwiftConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setListener()
    }

    private fun initViews() {
        binding.tvBankName.text = requireArguments().getString(ARG_BANK_NAME)
        binding.tvBranch.text = requireArguments().getString(ARG_BRANCH)
        binding.tvCity.text = requireArguments().getString(ARG_CITY)
    }

    private fun setListener() {
        binding.llConfirm.setOnClickListener {
            swiftVerificationViewModel.swiftCodeState.value = VerificationState.VERIFIED
            dismiss()
        }
    }


}