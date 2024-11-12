package com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputForgotPassword
import com.example.engu_pension_verification_application.model.input.InputLogin
import com.example.engu_pension_verification_application.model.response.ResponseForgotPassword
import com.example.engu_pension_verification_application.ui.activity.DashboardActivity
import com.example.engu_pension_verification_application.ui.fragment.signup.sign_up.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_o_t_p.cl_click_login
import kotlinx.android.synthetic.main.fragment_o_t_p.ll_verify_buttons
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.util.regex.Pattern


class ForgotPasswordFragment : Fragment(), ForgotPassViewCallBack {
    val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    val PASSWORD_PATTERN =
        Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")

    var Ph_no: String = ""
    var email_Phn: String = ""
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                 findNavController().navigate(R.id.action_forgotpassword_to_login)
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

        //for hiding sign up success login button
        ll_verify_buttons?.visibility = View.VISIBLE
        cl_click_login?.visibility = View.GONE

        //forgotPasswordViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        forgotPasswordViewModel = ForgotPasswordViewModel(this)
        forgtpass_ccp.registerPhoneNumberTextView(et_forgtpass_phone)
        onClicked()
        //observeforgotPassword()
    }

    private fun observeforgotPassword() {
        forgotPasswordViewModel.forgotPassStatus.observe(
            viewLifecycleOwner,
            Observer { responseforgotPass ->
                Loader.hideLoader()

                if (responseforgotPass.forgot_detail?.status.equals("success")) {
                     Toast.makeText(
                        context,
                        responseforgotPass.forgot_detail!!.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    val bundle = Bundle()
                    bundle.putSerializable("screen", "ForgotPassword")
                    bundle.putSerializable("Email/Phone", email_Phn)
                    bundle.putSerializable("Token", responseforgotPass.forgot_detail.uniqueToken)
                    findNavController().navigate(R.id.action_forgotpassword_to_otpscreen, bundle)

                }
                else {
                    Toast.makeText(
                        context,
                        responseforgotPass.forgot_detail!!.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }


            })
    }

    private fun onClicked() {
        ll_forgotpass_req.setOnClickListener {

            if (isValidLogin()) {

                Loader.showLoader(requireContext())
                if (context?.isConnectedToNetwork()!!) {
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
            findNavController().navigate(R.id.action_forgotpassword_to_login)
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

            if (!EMAIL_ADDRESS_PATTERN.matcher(
                    ed_forgotpass_email.text.toString()
                ).matches()
            ) {
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

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun onForgotPassSuccess(response: ResponseForgotPassword) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.forgot_detail!!.message,
            Toast.LENGTH_LONG
        )
            .show()
        val bundle = Bundle()
        bundle.putSerializable("screen", "ForgotPassword")
        bundle.putSerializable("Email/Phone", email_Phn)
        bundle.putSerializable("Token", response.forgot_detail.uniqueToken)
        findNavController().navigate(R.id.action_forgotpassword_to_otpscreen, bundle)

    }

    override fun onForgotPassFail(response: ResponseForgotPassword) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.forgot_detail!!.message,
            Toast.LENGTH_LONG
        )
            .show()


    }

}