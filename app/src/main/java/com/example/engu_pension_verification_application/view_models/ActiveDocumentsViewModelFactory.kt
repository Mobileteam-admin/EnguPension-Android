package com.example.engu_pension_verification_application.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.ui.fragment.service.active.ActiveDocumentsViewModel

class ActiveDocumentsViewModelFactory (private val networkRepo: NetworkRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveDocumentsViewModel::class.java)) {
            return ActiveDocumentsViewModel(networkRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}