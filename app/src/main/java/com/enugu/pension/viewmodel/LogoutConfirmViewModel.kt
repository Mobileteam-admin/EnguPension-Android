package com.enugu.pension.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogoutConfirmViewModel : ViewModel() {
    val logout = MutableLiveData<Unit>()
}