package com.example.engu_pension_verification_application.ui.fragment.signup.otp_verify

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentOTPBinding
import com.example.engu_pension_verification_application.model.input.InputForgotVerify
import com.example.engu_pension_verification_application.model.input.InputSignupVerify
import com.example.engu_pension_verification_application.model.response.VerifyResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.OTPViewModel

@RequiresApi(Build.VERSION_CODES.O)
class OTPFragment : BaseFragment() {
    private lateinit var binding:FragmentOTPBinding
    private lateinit var otpViewModel: OTPViewModel
    var screen: String = ""
    var email: String = "  "
    var phone: String = "  "
    var final_otp: String = ""
    var email_Phn: String = ""
    var token: String = ""

    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()

        binding.clClickLogin.visibility = View.GONE
        //otpViewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        val bundle = this.arguments
        if (bundle != null) {
            screen = bundle.getString("screen").toString()
        }

        if (screen.equals("Signup")) {
            if (bundle != null) {
              //  screen = bundle.getString("screen").toString()
                email = bundle.getString("Email").toString()
                phone = bundle.getString("Phone").toString()
            }

            binding.tvVerifyNote.text =
                "Please enter the 6 digit verification code sent to \n " + email + "/" + phone + " to confirm."
        } else {
            if (bundle != null) {
               // screen = bundle.getString("screen").toString()
                email_Phn = bundle.getString("Email/Phone").toString()
                token = bundle.getString("Token").toString()
            }
            binding.tvVerifyNote.text =
                "Please enter the 6 digit verification code sent to \n " + email_Phn + " to confirm."
        }

        onClicked()
        //observeVerification()
        focusValidation()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        otpViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(OTPViewModel::class.java)
    }
    private fun observeData() {
        otpViewModel.otpVerifyResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
            if (response.detail?.status == AppConstants.SUCCESS) {
                onOtpVerifySuccess(response)
            } else {
                onOtpVerifyFailure()
            }
        }
        otpViewModel.verifyForgotPassResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
            if (response.detail?.status == AppConstants.SUCCESS) {
                onOtpVerifySuccess(response)
            } else {
                onOtpVerifyFailure()
            }
        }
        otpViewModel.resendOTPResponse.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onClicked() {
        binding.llVerify.setOnClickListener {

            if (isValidOTP()) {
                val a_otp = StringBuilder(100)
                a_otp.append(binding.etOtp1.text.toString())
                a_otp.append(binding.etOtp2.text.toString())
                a_otp.append(binding.etOtp3.text.toString())
                a_otp.append(binding.etOtp4.text.toString())
                a_otp.append(binding.etOtp5.text.toString())
                a_otp.append(binding.etOtp6.text.toString())
                final_otp = a_otp.toString()
                Log.d("otp", "onClicked: " + final_otp)

                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {



                    if (screen.equals("Signup")) {
                        otpViewModel.doVerifyReg(
                            InputSignupVerify(
                                otp = final_otp,
                                email = email
                            )
                        )
                    } else {
                        otpViewModel.doVerifyForgot(
                            InputForgotVerify(
                                otp = final_otp,
                                email = email_Phn
                            )
                        )
                    }

                } else {
                    dismissLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.llResendOtp.setOnClickListener {
            showLoader()
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                Log.d("TAG _ 1", "onClicked: " + email)
                Log.d("TAG _ 2", "onClicked:Email/Phone " + email_Phn)

                if (screen.equals("Signup")) {
                    otpViewModel.doResendOtp(
                        com.example.engu_pension_verification_application.model.input.InputResendotp(
                            email = email
                        )
                    )
                }else{
                    otpViewModel.doResendOtp(
                        com.example.engu_pension_verification_application.model.input.InputResendotp(
                            email = email_Phn
                        )
                    )
                }
            } else {
                dismissLoader()
                Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
            }
        }


        binding.llVerifyBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.llClickLogin.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_login, false)
        }

    }

    private fun isValidOTP(): Boolean {
        if ((TextUtils.isEmpty(binding.etOtp1.text))
            || (TextUtils.isEmpty(binding.etOtp2.text))
            || (TextUtils.isEmpty(binding.etOtp3.text))
            || (TextUtils.isEmpty(binding.etOtp4.text))
            || (TextUtils.isEmpty(binding.etOtp5.text))
            || (TextUtils.isEmpty(binding.etOtp6.text))
        ) {
            Toast.makeText(context, "Please enter valid OTP", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


    private fun focusValidation(): Boolean {
        binding.etOtp1.isFocusedByDefault = true
        binding.etOtp1.requestFocus()
        binding.etOtp1.doAfterTextChanged {
            if (binding.etOtp1.text.length == 1) {
                binding.etOtp2.requestFocus()
            }
        }
        binding.etOtp2.doAfterTextChanged {
            if (binding.etOtp2.text.length == 1) {
                binding.etOtp3.requestFocus()
            } else if (TextUtils.isEmpty(binding.etOtp2.text)) {
                binding.etOtp1.requestFocus()
            }
        }
        binding.etOtp3.doAfterTextChanged {
            if (binding.etOtp3.text.length == 1) {
                binding.etOtp4.requestFocus()
            } else if (TextUtils.isEmpty(binding.etOtp3.text)) {
                binding.etOtp2.requestFocus()
            }
        }
        binding.etOtp4.doAfterTextChanged {
            if (binding.etOtp4.text.length == 1) {
                binding.etOtp5.requestFocus()
            } else if (TextUtils.isEmpty(binding.etOtp4.text)) {
                binding.etOtp3.requestFocus()
            }
        }
        binding.etOtp5.doAfterTextChanged {
            if (binding.etOtp5.text.length == 1) {
                binding.etOtp6.requestFocus()
            } else if (TextUtils.isEmpty(binding.etOtp5.text)) {
                binding.etOtp4.requestFocus()
            }
        }
        binding.etOtp6.doAfterTextChanged {
            if (TextUtils.isEmpty(binding.etOtp6.text)) {
                binding.etOtp5.requestFocus()
            }
        }

        return true
    }

    fun onOtpVerifySuccess(response: VerifyResponse) {
        prefs.isLogin = true
        prefs.user_id = response.detail?.userdetails?.id
        prefs.user_name = response.detail?.userdetails?.username
        prefs.email = response.detail?.userdetails?.email

        if (screen.equals("Signup")) {
            binding.llVerifyButtons.visibility = View.GONE
            binding.clClickLogin.visibility = View.VISIBLE
        } else {
            val bundle = Bundle()
            bundle.putSerializable("Token", token)
            bundle.putSerializable("OTP", final_otp)
            navigate(R.id.action_otp_to_resetpassword,bundle, popUpTo = R.id.navigation_otp)
        }
    }

    fun onOtpVerifyFailure() {
        binding.etOtp1.setText("")
        binding.etOtp2.setText("")
        binding.etOtp3.setText("")
        binding.etOtp4.setText("")
        binding.etOtp5.setText("")
        binding.etOtp6.setText("")
        focusValidation()
    }
}