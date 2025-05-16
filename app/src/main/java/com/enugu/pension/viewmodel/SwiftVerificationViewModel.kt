package com.enugu.pension.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enugu.pension.util.VerificationState

class SwiftVerificationViewModel : ViewModel() {
    val swiftCodeState = MutableLiveData(VerificationState.VERIFY)
}