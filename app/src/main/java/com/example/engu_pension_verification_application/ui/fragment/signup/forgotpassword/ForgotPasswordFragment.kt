package com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentForgotPasswordBinding
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.ForgotPasswordViewModel


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
                    Log.d(
                        "forgotpass",
                        "onClicked: " + com.example.engu_pension_verification_application.model.input.InputForgotPassword(
                            email_Phn
                        )
                    )
                    forgotPasswordViewModel.doForgotPass(
                        com.example.engu_pension_verification_application.model.input.InputForgotPassword(
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