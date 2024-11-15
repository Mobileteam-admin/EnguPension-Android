package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo

class TokenRefreshViewModel2Factory (private val networkRepo: NetworkRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TokenRefreshViewModel2::class.java)) {
            return TokenRefreshViewModel2(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}