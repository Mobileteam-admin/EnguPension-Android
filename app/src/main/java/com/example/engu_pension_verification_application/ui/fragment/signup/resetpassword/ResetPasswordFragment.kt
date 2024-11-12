package com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
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
import com.example.engu_pension_verification_application.model.input.InputResetPassword
import com.example.engu_pension_verification_application.model.response.ResponseResetPassword
import com.example.engu_pension_verification_application.ui.activity.ServiceActivity
import com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword.ForgotPasswordViewModel
import com.example.engu_pension_verification_application.ui.fragment.signup.sign_up.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_reset_password.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.util.regex.Pattern


class ResetPasswordFragment : Fragment(), ResetPassCallBack {
    var token: String = ""
    var OTP: String = ""

    val PASSWORD_PATTERN =
        Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")

    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resetpassword_to_forgotpassword)
                //findNavController().popBackStack()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //resetPasswordViewModel = ViewModelProvider(this).get(ResetPasswordViewModel::class.java)
        resetPasswordViewModel = ResetPasswordViewModel(this)
        val bundle = this.arguments
        if (bundle != null) {
            token = bundle.getString("Token").toString()
            OTP = bundle.getString("OTP").toString()
        }
        ontextWatcher()

        onClicked()
        //observe_resetpass()
    }

    private fun observe_resetpass() {
        resetPasswordViewModel.resetPassStatus.observe(viewLifecycleOwner, Observer { resetdata ->
            Loader.hideLoader()

            if (resetdata.reset_detail?.status.equals("success")) {
                Toast.makeText(
                    context,
                    resetdata.reset_detail!!.message,
                    Toast.LENGTH_LONG
                )
                    .show()
                 findNavController().navigate(R.id.action_resetpassword_to_login)

            }
            else {
                Toast.makeText(
                    context,
                    resetdata.reset_detail!!.message,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        })
    }

    private fun ontextWatcher() {
        et_new_resetpass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if ((!PASSWORD_PATTERN.matcher(
                        newText
                    ).matches())
                ) {
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

                Loader.showLoader(requireContext())
                if (context?.isConnectedToNetwork()!!) {
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
           // activity?.onBackPressed()
            findNavController().navigate(R.id.action_resetpassword_to_forgotpassword)
        }
    }

    private fun isValidReset(): Boolean {


        if (TextUtils.isEmpty(et_new_resetpass.text)) {
            txt_resetnew_password_error.visibility = View.VISIBLE
            return false
        } else {
            txt_resetnew_password_error.visibility = View.GONE
        }

        if ((!PASSWORD_PATTERN.matcher(
                et_new_resetpass.text.toString()
            ).matches())
        ) {
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

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun onResetPassSuccess(response: ResponseResetPassword) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.reset_detail!!.message,
            Toast.LENGTH_LONG
        )
            .show()
        findNavController().navigate(R.id.action_resetpassword_to_login)
    }

    override fun onResetPassFailure(response: ResponseResetPassword) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.reset_detail!!.message,
            Toast.LENGTH_LONG
        )
            .show()
    }
}