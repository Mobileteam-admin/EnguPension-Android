package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.response.AccountDetailsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _accountDetailsResult = MutableLiveData<AccountDetailsResponse>()
    val accountDetailsResult: LiveData<AccountDetailsResponse>
        get() = _accountDetailsResult

    fun fetchDashboardDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _accountDetailsResult.postValue(networkRepo.fetchAccountDetails())
            } catch (e: Exception) {
                _accountDetailsResult.postValue(
                    AccountDetailsResponse(AccountDetailsResponse.Detail(message = "Something went wrong"))
                )
            }
        }
    }
}