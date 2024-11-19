package com.example.engu_pension_verification_application.ui.fragment.signup.sign_up

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputSignup
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.SignUpViewModel
import kotlinx.android.synthetic.main.fragment_sign_up.*


class SignUpFragment : Fragment() {
    var Ph_no: String = ""
    private lateinit var signUpViewModel: SignUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        initViewModel()
        observeData()
        ccp.registerPhoneNumberTextView(et_signup_phone)

       /* et_signup_username.text =  Editable.Factory.getInstance().newEditable("Reema")
        et_signup_email.text =  Editable.Factory.getInstance().newEditable("r@gmail.com")
        et_signup_phone.text =  Editable.Factory.getInstance().newEditable("9495745781")
        et_signup_password.text =  Editable.Factory.getInstance().newEditable("Reema@123")
        et_signup_confirmPassword.text =  Editable.Factory.getInstance().newEditable("Reema@123")*/

        ontextWatcher()

        onClicked()
        //observeSignUp()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        signUpViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(SignUpViewModel::class.java)
    }
    private fun observeData() {
        signUpViewModel.signupStatus.observe(viewLifecycleOwner) { response ->
            Loader.hideLoader()
            Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
            if (response.detail?.status == AppConstants.SUCCESS) {
                onSignUpSuccess()
            }
        }
    }
    private fun ontextWatcher() {
        et_signup_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!AppUtils.isValidPassword(newText.toString())) {
                    txt_passwordpattern_error.visibility = View.VISIBLE
                } else {
                    txt_passwordpattern_error.visibility = View.GONE
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (!AppUtils.isValidPassword(et_signup_password.text.toString())) {
                        txt_passwordpattern_error.visibility = View.VISIBLE
                        //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
                    } else {
                        txt_passwordpattern_error.visibility = View.GONE
                    }


                }
            }
        })


        et_signup_confirmPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(confirm: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!et_signup_password.text.toString().equals(confirm.toString())) {
                    Log.d("TAG", "onTextChanged: "+et_signup_password.text.toString()+" , "+confirm)
                    txt_signup_confirmPassword_error.visibility = View.VISIBLE
                    txt_signup_confirmPassword_error.text =
                        getResources().getString(R.string.password_mismatch)
                } else {
                    txt_signup_confirmPassword_error.visibility = View.GONE
                }
            }

            override fun afterTextChanged(confirmm: Editable?) {
                if (et_signup_password.text.toString().equals(confirmm.toString())) {
                    Log.d("After", "onTextChanged: "+et_signup_password.text.toString()+" , "+confirmm)
                    txt_signup_confirmPassword_error.visibility = View.GONE

                } else {
                    txt_signup_confirmPassword_error.visibility = View.VISIBLE
                }
            }
        } )

    }

    private fun onClicked() {

        ll_signup_signup.setOnClickListener {


            if (isValidSignup()) {
                Ph_no = "+" + ccp.fullNumber

                //final call
                Loader.showLoader(requireContext())
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    signUpViewModel.doSignup(
                        InputSignup(
                            username = et_signup_username.text.toString(),
                            email = et_signup_email.text.toString(),
                            phoneNumber = Ph_no,
                            password = et_signup_password.text.toString(),
                            confirmPassword = et_signup_confirmPassword.text.toString()
                        )
                    )

                } else {
                    Loader.hideLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }

        ll_signup_login.setOnClickListener { findNavController().navigate(R.id.action_signup_to_login) }
    }

    private fun isValidSignup(): Boolean {
        if (TextUtils.isEmpty(et_signup_username.text)) {
            txt_signup_uname_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_uname_error.visibility = View.GONE
        }

        if (et_signup_username.text.toString().contains(" ")){

        /*Toast.makeText(
                context,
                "Enter valid username",
                Toast.LENGTH_LONG
            ).show()*/
        }

        if (TextUtils.isEmpty(et_signup_email.text)) {
            txt_signup_email_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_email_error.visibility = View.GONE
        }

        if (!AppUtils.isValidEmailAddress(et_signup_email.text.toString())) {
            txt_signup_email_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_email_error.visibility = View.GONE
        }


        if (TextUtils.isEmpty(et_signup_phone.text)) {
            txt_signup_phone_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_phone_error.visibility = View.GONE
        }

        if ((!ccp.isValid)) {
            Log.d("ccp.isValid", "isValidLogin: " + ccp.isValid)
            txt_signup_phone_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_phone_error.visibility = View.GONE
        }

        if (TextUtils.isEmpty(et_signup_password.text)) {
            txt_signup_password_error.visibility = View.VISIBLE
            return false
        } else {
            txt_signup_password_error.visibility = View.GONE
        }
        if (!AppUtils.isValidPassword(et_signup_password.text.toString())) {
            txt_passwordpattern_error.visibility = View.VISIBLE
                 //"Password must have at least 8 characters, include uppercase and lowercase letters, a digit, and a special character",
            return false
        } else {
            txt_passwordpattern_error.visibility = View.GONE
        }



        if (TextUtils.isEmpty(et_signup_confirmPassword.text)) {
            txt_signup_confirmPassword_error.visibility = View.VISIBLE
            txt_signup_confirmPassword_error.text =
                getResources().getString(R.string.enter_valid_password)
            return false
        } else {
            txt_signup_confirmPassword_error.visibility = View.GONE
        }

        if (!et_signup_password.text.toString().equals(et_signup_confirmPassword.text.toString())) {
            txt_signup_confirmPassword_error.visibility = View.VISIBLE
            txt_signup_confirmPassword_error.text =
                getResources().getString(R.string.password_mismatch)
            return false
        } else {
            txt_signup_confirmPassword_error.visibility = View.GONE
        }



        return true
    }

    private fun onSignUpSuccess() {
        val bundle = Bundle()
        bundle.putSerializable("screen", "Signup")
        bundle.putSerializable("Email", et_signup_email.text.toString())
        bundle.putSerializable("Phone", Ph_no)
        findNavController().navigate(R.id.action_signup_to_otp, bundle)
    }


}