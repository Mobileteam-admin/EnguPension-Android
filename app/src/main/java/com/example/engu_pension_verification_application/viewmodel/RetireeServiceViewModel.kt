package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RetireeServiceViewModel : ViewModel() {
    private val _onMoveToNextTab = MutableLiveData<Unit>()
    val onMoveToNextTab : LiveData<Unit>
        get() = _onMoveToNextTab

    val currentTabPos = MutableLiveData(0)

    val enableTab0 = MutableLiveData(true)
    val enableTab1 = MutableLiveData(false)
    val enableTab2 = MutableLiveData(false)


    fun setTabsEnabledState(isTab0Enabled: Boolean, isTab1Enabled: Boolean,isTab2Enabled: Boolean) {
        enableTab0.value = isTab0Enabled
        enableTab1.value = isTab1Enabled
        enableTab2.value = isTab2Enabled
    }
    fun moveToNextTab() {
        _onMoveToNextTab.value = Unit
    }
}