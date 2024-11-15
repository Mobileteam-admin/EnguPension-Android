package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveBasicDetailViewModel

class ActiveBasicDetailViewModelFactory (private val networkRepo: NetworkRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveBasicDetailViewModel::class.java)) {
            return ActiveBasicDetailViewModel(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}