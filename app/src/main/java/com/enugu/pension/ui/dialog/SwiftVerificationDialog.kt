package com.enugu.pension.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.databinding.DialogSwiftConfirmationBinding

class SwiftVerificationDialog : BaseDialog() {
    companion object {
        const val HAS_CONFIRMED = "hasConfirmed"
        const val DIALOG_RESULT = "swift_code_dialog_result"

        const val ARG_BANK_NAME = "arg_bank_name"
        const val ARG_BRANCH = "arg_branch"
        const val ARG_CITY = "arg_city"
    }

    private lateinit var binding:DialogSwiftConfirmationBinding


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
            val result = Bundle().apply {
                putBoolean(HAS_CONFIRMED, true)
            }
            requireActivity().supportFragmentManager.setFragmentResult(DIALOG_RESULT, result)
            dismiss()
        }
    }


}