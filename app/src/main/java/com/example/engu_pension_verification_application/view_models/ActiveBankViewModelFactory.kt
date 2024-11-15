package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBankViewModel

class ActiveBankViewModelFactory(private val networkRepo: NetworkRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveBankViewModel::class.java)) {
            return ActiveBankViewModel(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}