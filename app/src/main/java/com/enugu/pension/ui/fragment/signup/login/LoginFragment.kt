package com.enugu.pension.ui.fragment.signup.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.FragmentLoginBinding
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.activity.DashboardActivity
import com.enugu.pension.ui.activity.ServiceActivity
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.common.util.NetworkUtils
import com.enugu.pension.common.util.AppUtils
import com.enugu.pension.common.util.OnboardingStage
import com.enugu.pension.data.local.SharedPref
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.LoginViewModel


//var loginGovResponse : String? = null

@Suppress("UNREACHABLE_CODE")
class LoginFragment : BaseFragment() {
    private lateinit var binding:FragmentLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    var Ph_no: String = ""
    var email_Phn: String = ""

    //var loginGovResponse : String? = null

    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginCcp.registerPhoneNumberTextView(binding.etLoginPhone)
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
            if (response != null) {
                dismissLoader()
                Toast.makeText(context, response.login_detail?.message, Toast.LENGTH_LONG).show()
                if (response.login_detail?.status == AppConstants.SUCCESS) {
                    onLoginSuccess()
                }
                loginViewModel.resetLoginStatus()
            }
        }
    }

    private fun onClicked() {
        binding.llLogSignup.setOnClickListener {
            navigate(R.id.action_login_to_signup)
        }
        binding.llLogLogin.setOnClickListener {
            if (isValidLogin()) {
                if (isValidate_password()) {
                    if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                        showLoader()
                        loginViewModel.doLogin(
                            com.enugu.pension.data.remote.dto.request.InputLogin(
                                binding.edPassword.text.toString(),
                                email_Phn
                            )
                        )
                    } else {
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
        binding.textForgotPass.setOnClickListener {
            navigate(R.id.action_login_to_forgotpassword)
        }
    }

    private fun isValidate_password(): Boolean {
        if (TextUtils.isEmpty(binding.edPassword.text)) {
            binding.txtPasswordError.visibility = View.VISIBLE
            return false
        } else {
            binding.txtPasswordError.visibility = View.GONE
            return true
        }
        return true
    }

    private fun isValidLogin(): Boolean {
        if (TextUtils.isEmpty(binding.edEmailPhn.text)) {
            if (TextUtils.isEmpty(binding.etLoginPhone.text)) {
                Toast.makeText(context, "Please enter email or phone number", Toast.LENGTH_LONG)
                    .show()
                return false
            } else if ((!binding.loginCcp.isValid)) {
                binding.txtLoginPhoneError.visibility = View.VISIBLE
                return false
            } else {
                binding.txtLoginPhoneError.visibility = View.GONE
                email_Phn = "+" + binding.loginCcp.fullNumber
                return true
            }
            return false
        } else {
            if (!AppUtils.isValidEmailAddress(binding.edEmailPhn.text.toString())) {
                binding.txtLoginemailError.visibility = View.VISIBLE
                return false
            } else {
                binding.txtLoginemailError.visibility = View.GONE
                email_Phn = binding.edEmailPhn.text.toString()
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