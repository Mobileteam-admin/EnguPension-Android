package com.enugu.pension.ui.fragment.signup.forgotpassword

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentForgotPasswordBinding
import com.enugu.pension.model.response.ResponseForgotPassword
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.AppUtils
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.ForgotPasswordViewModel


class ForgotPasswordFragment : BaseFragment() {
    private lateinit var binding:FragmentForgotPasswordBinding
    var Ph_no: String = ""
    var email_Phn: String = ""
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()

        //forgotPasswordViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        binding.forgtpassCcp.registerPhoneNumberTextView(binding.etForgtpassPhone)
        onClicked()
        //observeforgotPassword()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        forgotPasswordViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ForgotPasswordViewModel::class.java)
    }
    private fun observeData() {
        forgotPasswordViewModel.forgotPassResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.forgot_detail?.message, Toast.LENGTH_LONG).show()
            if (response.forgot_detail?.status == AppConstants.SUCCESS) {
                onForgotPassSuccess(response)
            }
        }
    }

    private fun onClicked() {
        binding.llForgotpassReq.setOnClickListener {

            if (isValidLogin()) {

                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    forgotPasswordViewModel.doForgotPass(
                        com.enugu.pension.model.request.InputForgotPassword(
                            email_Phn
                        )
                    )
                } else {
                    dismissLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.llForgotpassBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun isValidLogin(): Boolean {
        if (TextUtils.isEmpty(binding.edForgotpassEmail.text)) {

            if (TextUtils.isEmpty(binding.etForgtpassPhone.text)) {
                Toast.makeText(context, "Please enter email or phone number", Toast.LENGTH_LONG)
                    .show()
                return false
            } else if ((!binding.forgtpassCcp.isValid)) {
                Toast.makeText(context, "binding.forgtpassCcp", Toast.LENGTH_LONG)
                    .show()
                binding.txtForgotpassPhoneError.visibility = View.VISIBLE
                return false
            } else {
                binding.txtForgotpassPhoneError.visibility = View.GONE
                email_Phn = "+" + binding.forgtpassCcp.fullNumber
                return true
            }
            return false

        } else {

            if (!AppUtils.isValidEmailAddress(binding.edForgotpassEmail.text.toString())) {
                binding.txtForgotpassError.visibility = View.VISIBLE
                return false
            } else {
                binding.txtForgotpassError.visibility = View.GONE
                email_Phn = binding.edForgotpassEmail.text.toString()
                return true
            }


        }

        return true
    }

    private fun onForgotPassSuccess(response: ResponseForgotPassword) {
        val bundle = Bundle()
        bundle.putSerializable("screen", "ForgotPassword")
        bundle.putSerializable("Email/Phone", email_Phn)
        bundle.putSerializable("Token", response.forgot_detail?.uniqueToken)
        navigate(R.id.action_forgotpassword_to_otpscreen, bundle,
            popUpTo = R.id.navigation_forgotpassword,
            popUpToInclusive = true
            )

    }

}