package com.example.engu_pension_verification_application.ui.fragment.signup.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ResponseLogin
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.DashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ServiceActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*


//var loginGovResponse : String? = null

@Suppress("UNREACHABLE_CODE")
class LoginFragment : BaseFragment() {
    private lateinit var loginViewModel: LoginViewModel
    var Ph_no: String = ""
    var email_Phn: String = ""

    //var loginGovResponse : String? = null

    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login_ccp.registerPhoneNumberTextView(et_login_phone)
        initViewModel()
        observeData()
        onClicked()
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        loginViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(LoginViewModel::class.java)
    }
    private fun observeData() {
        loginViewModel.loginStatus.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            Toast.makeText(context, response.login_detail?.message, Toast.LENGTH_LONG).show()
            if (response.login_detail?.status == AppConstants.SUCCESS) {
                onLoginSuccess()
            }
        }
    }

    private fun onClicked() {
        ll_log_signup.setOnClickListener {
            navigate(R.id.action_login_to_signup)
        }
        ll_log_login.setOnClickListener {
            if (isValidLogin()) {
                if (isValidate_password()) {
                    showLoader()
                    if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                        Log.d(
                            "Login",
                            "onClicked: " + com.example.engu_pension_verification_application.model.input.InputLogin(
                                ed_password.text.toString(),
                                email_Phn
                            )
                        )
                        loginViewModel.doLogin(
                            com.example.engu_pension_verification_application.model.input.InputLogin(
                                ed_password.text.toString(),
                                email_Phn
                            )
                        )
                    } else {
                        dismissLoader()
                        Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            /*val intent = Intent(context, DashboardActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)*/
        }
        text_forgotPass.setOnClickListener {
            navigate(R.id.action_login_to_forgotpassword)
        }
    }

    private fun isValidate_password(): Boolean {
        if (TextUtils.isEmpty(ed_password.text)) {
            txt_password_error.visibility = View.VISIBLE
            return false
        } else {
            txt_password_error.visibility = View.GONE
            return true
        }
        return true
    }

    private fun isValidLogin(): Boolean {
        if (TextUtils.isEmpty(ed_email_phn.text)) {
            if (TextUtils.isEmpty(et_login_phone.text)) {
                Toast.makeText(context, "Please enter email or phone number", Toast.LENGTH_LONG)
                    .show()
                return false
            } else if ((!login_ccp.isValid)) {
                txt_login_phone_error.visibility = View.VISIBLE
                return false
            } else {
                txt_login_phone_error.visibility = View.GONE
                email_Phn = "+" + login_ccp.fullNumber
                return true
            }
            return false
        } else {
            if (!AppUtils.isValidEmailAddress(ed_email_phn.text.toString())) {
                txt_loginemail_error.visibility = View.VISIBLE
                return false
            } else {
                txt_loginemail_error.visibility = View.GONE
                email_Phn = ed_email_phn.text.toString()
                return true
            }
        }
        return true
    }

    private fun onLoginSuccess() {
        val intent = if (prefs.onboardingStage == OnboardingStage.DASHBOARD) {
            Intent(context, DashboardActivity::class.java)
        } else {
            Intent(context, ServiceActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}