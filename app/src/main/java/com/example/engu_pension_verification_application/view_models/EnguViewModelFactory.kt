package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBankViewModel
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBasicDetailViewModel
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveDocumentsViewModel
import com.example.engu_pension_verification_application.ui.fragment.signup.forgotpassword.ForgotPasswordViewModel
import com.example.engu_pension_verification_application.ui.fragment.signup.otp_verify.OTPViewModel
import com.example.engu_pension_verification_application.ui.fragment.signup.resetpassword.ResetPasswordViewModel
import com.example.engu_pension_verification_application.ui.fragment.signup.sign_up.SignUpViewModel

@Suppress("UNCHECKED_CAST")
class EnguViewModelFactory(private val networkRepo: NetworkRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(OTPViewModel::class.java)) {
            return OTPViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            return ForgotPasswordViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {
            return ResetPasswordViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ActiveBankViewModel::class.java)) {
            return ActiveBankViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ActiveBasicDetailViewModel::class.java)) {
            return ActiveBasicDetailViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ActiveDocumentsViewModel::class.java)) {
            return ActiveDocumentsViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(TokenRefreshViewModel2::class.java)) {
            return TokenRefreshViewModel2(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ProcessDashboardViewModel::class.java)) {
            return ProcessDashboardViewModel(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}