package com.example.engu_pension_verification_application.ui.fragment.signup.otp_verify

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputForgotVerify
import com.example.engu_pension_verification_application.model.input.InputSignupVerify
import com.example.engu_pension_verification_application.model.response.VerifyResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.NetworkUtils
import com.example.engu_pension_verification_application.utils.SharedPref
import com.example.engu_pension_verification_application.view_models.EnguViewModelFactory
import kotlinx.android.synthetic.main.fragment_o_t_p.*

@RequiresApi(Build.VERSION_CODES.O)
class OTPFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_o_t_p, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (screen.equals("Signup")) {
                    findNavController().navigate(R.id.action_otp_to_signup)
                }else{
                    findNavController().navigate(R.id.action_otp_to_forgotpassword)
                }
                //activity?.onBackPressedDispatcher!!.onBackPressed()
            }
        })
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()

        cl_click_login.visibility = View.GONE
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

            tv_verify_note.text =
                "Please enter the 6 digit verification code sent to \n " + email + "/" + phone + " to confirm."
        } else {
            if (bundle != null) {
               // screen = bundle.getString("screen").toString()
                email_Phn = bundle.getString("Email/Phone").toString()
                token = bundle.getString("Token").toString()
            }
            tv_verify_note.text =
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
            Loader.hideLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
            if (response.detail?.status == AppConstants.SUCCESS) {
                onOtpVerifySuccess(response)
            } else {
                onOtpVerifyFailure()
            }
        }
        otpViewModel.verifyForgotPassResponse.observe(viewLifecycleOwner) { response ->
            Loader.hideLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
            if (response.detail?.status == AppConstants.SUCCESS) {
                onOtpVerifySuccess(response)
            } else {
                onOtpVerifyFailure()
            }
        }
        otpViewModel.resendOTPResponse.observe(viewLifecycleOwner) { response ->
            Loader.hideLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
        }
    }

   /* private fun observeVerification() {
        otpViewModel.verificationStatus.observe(viewLifecycleOwner, Observer { verifyresponse ->
            Loader.hideLoader()
            if (verifyresponse.detail?.status.equals("success")) {
                Toast.makeText(context, verifyresponse.detail?.message, Toast.LENGTH_LONG).show()

                if (screen.equals("Signup")) {
                    findNavController().navigate(R.id.action_otp_to_login)
                   *//* val intent = Intent(context, ServiceActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)*//*
                } else {
                    val bundle = Bundle()
                    bundle.putSerializable("Token", token)
                    bundle.putSerializable("OTP", final_otp)
                    findNavController().navigate(R.id.action_otp_to_resetpassword,bundle)
                }

            } else {
                Toast.makeText(context, verifyresponse.detail?.message, Toast.LENGTH_LONG).show()
                et_otp_1.setText("")
                et_otp_2.setText("")
                et_otp_3.setText("")
                et_otp_4.setText("")
                et_otp_5.setText("")
                et_otp_6.setText("")
                focusValidation()
            }
        })

        otpViewModel.resendotpStatus.observe(viewLifecycleOwner, Observer { otpresendresponse ->
            Log.d("TAG _ 4", "onClicked: " + otpresendresponse)

            Loader.hideLoader()
            if (otpresendresponse.detail?.status.equals("success")) {
                Toast.makeText(context, otpresendresponse.detail?.message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, otpresendresponse.detail?.message, Toast.LENGTH_LONG).show()
            }
        })

    }*/

    private fun onClicked() {
        ll_verify.setOnClickListener {

            if (isValidOTP()) {
                val a_otp = StringBuilder(100)
                a_otp.append(et_otp_1.text.toString())
                a_otp.append(et_otp_2.text.toString())
                a_otp.append(et_otp_3.text.toString())
                a_otp.append(et_otp_4.text.toString())
                a_otp.append(et_otp_5.text.toString())
                a_otp.append(et_otp_6.text.toString())
                final_otp = a_otp.toString()
                Log.d("otp", "onClicked: " + final_otp)

                Loader.showLoader(requireContext())
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
                    Loader.hideLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }

            /* if (screen.equals("Signup")) {
                 val intent = Intent(context, ServiceActivity::class.java)
                 intent.flags =
                     Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                 startActivity(intent)
             } else {
                 findNavController().navigate(R.id.action_otp_to_resetpassword)
             }*/

        }

        ll_resend_otp.setOnClickListener {
            Loader.showLoader(requireContext())
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
                Loader.hideLoader()
                Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
            }
        }


        ll_verify_back.setOnClickListener {
            // activity?.onBackPressedDispatcher?.onBackPressed()
            if (screen.equals("Signup")) {
                findNavController().navigate(R.id.action_otp_to_signup)
            }else{
                findNavController().navigate(R.id.action_otp_to_forgotpassword)
            }


        }

        ll_click_login.setOnClickListener {
            findNavController().navigate(R.id.action_otp_to_login)
        }

    }

    private fun isValidOTP(): Boolean {
        if ((TextUtils.isEmpty(et_otp_1.text))
            || (TextUtils.isEmpty(et_otp_2.text))
            || (TextUtils.isEmpty(et_otp_3.text))
            || (TextUtils.isEmpty(et_otp_4.text))
            || (TextUtils.isEmpty(et_otp_5.text))
            || (TextUtils.isEmpty(et_otp_6.text))
        ) {
            Toast.makeText(context, "Please enter valid OTP", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


    private fun focusValidation(): Boolean {
        et_otp_1.isFocusedByDefault = true
        et_otp_1.requestFocus()
        et_otp_1.doAfterTextChanged {
            if (et_otp_1.text.length == 1) {
                et_otp_2.requestFocus()
            }
        }
        et_otp_2.doAfterTextChanged {
            if (et_otp_2.text.length == 1) {
                et_otp_3.requestFocus()
            } else if (TextUtils.isEmpty(et_otp_2.text)) {
                et_otp_1.requestFocus()
            }
        }
        et_otp_3.doAfterTextChanged {
            if (et_otp_3.text.length == 1) {
                et_otp_4.requestFocus()
            } else if (TextUtils.isEmpty(et_otp_3.text)) {
                et_otp_2.requestFocus()
            }
        }
        et_otp_4.doAfterTextChanged {
            if (et_otp_4.text.length == 1) {
                et_otp_5.requestFocus()
            } else if (TextUtils.isEmpty(et_otp_4.text)) {
                et_otp_3.requestFocus()
            }
        }
        et_otp_5.doAfterTextChanged {
            if (et_otp_5.text.length == 1) {
                et_otp_6.requestFocus()
            } else if (TextUtils.isEmpty(et_otp_5.text)) {
                et_otp_4.requestFocus()
            }
        }
        et_otp_6.doAfterTextChanged {
            if (TextUtils.isEmpty(et_otp_6.text)) {
                et_otp_5.requestFocus()
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

            prefs.lastActivityDashboard = false

            ll_verify_buttons.visibility = View.GONE
            cl_click_login.visibility = View.VISIBLE
            //findNavController().navigate(R.id.action_otp_to_login)
            /* val intent = Intent(context, ServiceActivity::class.java)
             intent.flags =
                 Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)*/
        } else {
            val bundle = Bundle()
            bundle.putSerializable("Token", token)
            bundle.putSerializable("OTP", final_otp)
            findNavController().navigate(R.id.action_otp_to_resetpassword,bundle)
        }
    }

    fun onOtpVerifyFailure() {
        et_otp_1.setText("")
        et_otp_2.setText("")
        et_otp_3.setText("")
        et_otp_4.setText("")
        et_otp_5.setText("")
        et_otp_6.setText("")
        focusValidation()
    }
}