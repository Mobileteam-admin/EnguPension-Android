package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogoutConfirmViewModel : ViewModel() {
    val logout = MutableLiveData<Unit>()
}