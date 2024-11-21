package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.ResetPasswordViewModel
import kotlinx.android.synthetic.main.fragment_reset_password.*


class ResetPasswordFragment : BaseFragment() {
    var token: String = ""
    var OTP: String = ""

    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        //resetPasswordViewModel = ViewModelProvider(this).get(ResetPasswordViewModel::class.java)
        val bundle = this.arguments
        if (bundle != null) {
            token = bundle.getString("Token").toString()
            OTP = bundle.getString("OTP").toString()
        }
        ontextWatcher()

        onClicked()
        //observe_resetpass()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        resetPasswordViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ResetPasswordViewModel::class.java)
    }
    private fun observeData() {
        resetPasswordViewModel.resetPassResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.reset_detail?.message, Toast.LENGTH_LONG).show()
            if (response.reset_detail?.status == AppConstants.SUCCESS) {
                findNavController().popBackStack(R.id.navigation_login, false)
            }
        }
    }

    private fun ontextWatcher() {
        et_new_resetpass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!AppUtils.isValidPassword(et_new_resetpass.text.toString())) {
                    txt_reset_newpass_pattern_error.visibility = View.VISIBLE
                } else {
                    txt_reset_newpass_pattern_error.visibility = View.GONE
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() != null) {
                    if (!p0.toString().isEmpty()) {
                        txt_resetnew_password_error.visibility = View.GONE
                    }

                }
            }
        })

        et_confirm_resetpass.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (!et_new_resetpass.text.toString().equals(p0.toString())) {
                    txt_reset_confirmPassword_error.visibility = View.VISIBLE
                } else {
                    txt_reset_confirmPassword_error.visibility = View.GONE
                }
            }
        } )

    }

    private fun onClicked() {
        ll_resetpass_submit.setOnClickListener {
            if (isValidReset()) {

                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    resetPasswordViewModel.doReset(
                        com.example.engu_pension_verification_application.model.input.InputResetPassword(
                            password = et_new_resetpass.text.toString(),
                            token = token,
                            otp = OTP
                        )
                    )
                }


            }
        }

        ll_resetpass_back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun isValidReset(): Boolean {


        if (TextUtils.isEmpty(et_new_resetpass.text)) {
            txt_resetnew_password_error.visibility = View.VISIBLE
            return false
        } else {
            txt_resetnew_password_error.visibility = View.GONE
        }

        if (!AppUtils.isValidPassword(et_new_resetpass.text.toString())) {
            txt_reset_newpass_pattern_error.visibility = View.VISIBLE
            //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
            return false
        } else {
            txt_reset_newpass_pattern_error.visibility = View.GONE
        }



        if (TextUtils.isEmpty(et_confirm_resetpass.text)) {
            txt_reset_confirmPassword_error.visibility = View.VISIBLE
            txt_reset_confirmPassword_error.text =
                getResources().getString(R.string.enter_valid_password)
            return false
        } else {
            txt_reset_confirmPassword_error.visibility = View.GONE
        }

        if (et_new_resetpass.text.toString() != et_confirm_resetpass.text.toString()) {
            txt_reset_confirmPassword_error.visibility = View.VISIBLE
            txt_reset_confirmPassword_error.text =
                getResources().getString(R.string.password_mismatch)
            return false
        } else {
            txt_reset_confirmPassword_error.visibility = View.GONE
        }
        return true
    }
}