package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoaderViewModel : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun show() {
        _isLoading.value = true
    }

    fun dismiss() {
        _isLoading.value = false
    }
}