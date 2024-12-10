package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo

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
        } else if (modelClass.isAssignableFrom(RetireeBasicDetailsViewModel::class.java)) {
            return RetireeBasicDetailsViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(RetireeDocumentsViewModel::class.java)) {
            return RetireeDocumentsViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(RetireeBankViewModel::class.java)) {
            return RetireeBankViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(AddBankViewModel::class.java)) {
            return AddBankViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            return WalletViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            return AppointmentViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(BookingDetailsViewModel::class.java)) {
            return BookingDetailsViewModel(networkRepo) as T
        } else if (modelClass.isAssignableFrom(WalletHistoryViewModel::class.java)) {
            return WalletHistoryViewModel(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}