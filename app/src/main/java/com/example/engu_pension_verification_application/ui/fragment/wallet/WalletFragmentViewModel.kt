package com.example.engu_pension_verification_application.ui.fragment.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.engu_pension_verification_application.utils.SharedPref

class WalletFragmentViewModel(application: Application) : AndroidViewModel(application) {
    fun getBankList() {

    }

    private val prefs = SharedPref

    init {
        application.let { prefs.with(it) }
    }
}