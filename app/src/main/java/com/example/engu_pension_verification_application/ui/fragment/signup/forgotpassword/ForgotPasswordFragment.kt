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
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.ForgotPasswordViewModel
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.fragment_o_t_p.cl_click_login
import kotlinx.android.synthetic.main.fragment_o_t_p.ll_verify_buttons


class ForgotPasswordFragment : BaseFragment() {
    var Ph_no: String = ""
    var email_Phn: String = ""
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                 navigate(R.id.action_forgotpassword_to_login)
               // isEnabled = false
                //findNavController().popBackStack()
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        //for hiding sign up success login button
        ll_verify_buttons?.visibility = View.VISIBLE
        cl_click_login?.visibility = View.GONE

        //forgotPasswordViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        forgtpass_ccp.registerPhoneNumberTextView(et_forgtpass_phone)
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
            Loader.hideLoader()
            Toast.makeText(context, response.forgot_detail?.message, Toast.LENGTH_LONG).show()
            if (response.forgot_detail?.status == AppConstants.SUCCESS) {
                onForgotPassSuccess(response)
            }
        }
    }

    private fun onClicked() {
        ll_forgotpass_req.setOnClickListener {

            if (isValidLogin()) {

                Loader.showLoader(requireContext())
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
                    Loader.hideLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }
        ll_forgotpass_back.setOnClickListener {
           // activity?.onBackPressed()
           // activity?.onBackPressedDispatcher?.onBackPressed()
            navigate(R.id.action_forgotpassword_to_login, isReverseAnim = true)
        }
    }

    private fun isValidLogin(): Boolean {
        if (TextUtils.isEmpty(ed_forgotpass_email.text)) {

            if (TextUtils.isEmpty(et_forgtpass_phone.text)) {
                Toast.makeText(context, "Please enter email or phone number", Toast.LENGTH_LONG)
                    .show()
                return false
            } else if ((!forgtpass_ccp.isValid)) {
                Toast.makeText(context, "forgtpass_ccp", Toast.LENGTH_LONG)
                    .show()
                txt_forgotpass_phone_error.visibility = View.VISIBLE
                return false
            } else {
                txt_forgotpass_phone_error.visibility = View.GONE
                email_Phn = "+" + forgtpass_ccp.fullNumber
                return true
            }
            return false

        } else {

            if (!AppUtils.isValidEmailAddress(ed_forgotpass_email.text.toString())) {
                txt_forgotpass_error.visibility = View.VISIBLE
                return false
            } else {
                txt_forgotpass_error.visibility = View.GONE
                email_Phn = ed_forgotpass_email.text.toString()
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
        navigate(R.id.action_forgotpassword_to_otpscreen, bundle)

    }

}