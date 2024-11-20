package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref

class RetireeServiceViewModel : ViewModel() {
    private val _onMoveToNextTab = MutableLiveData<Unit>()
    val onMoveToNextTab : LiveData<Unit>
        get() = _onMoveToNextTab

    val currentTabPos = MutableLiveData(0)

    val enableDocTab = MutableLiveData(false)
    val enableBankTab = MutableLiveData(false)

    fun refreshTabsState() {
        val onboardingStage = SharedPref.onboardingStage
        enableDocTab.value = onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS || onboardingStage == OnboardingStage.ACTIVE_BANK_INFO
        enableBankTab.value = onboardingStage == OnboardingStage.ACTIVE_BANK_INFO
    }
    fun moveToNextTab() {
        _onMoveToNextTab.value = Unit
    }
}