package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.TopUpRequest
import com.example.engu_pension_verification_application.model.response.BanksDetail
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.TopUpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletFragmentViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    val bankItems = mutableListOf<ListBanksItem?>()

    private val _bankListApiResult = MutableLiveData<ResponseBankList>()
    val bankListApiResult: LiveData<ResponseBankList>
        get() = _bankListApiResult

    private val _topUpApiResult = MutableLiveData<Pair<TopUpRequest, TopUpResponse>>()
    val topUpApiResult: LiveData<Pair<TopUpRequest, TopUpResponse>>
        get() = _topUpApiResult

    fun fetchBankList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bankListApiResult.postValue(networkRepo.fetchBankList())
            } catch (e: Exception) {
                _bankListApiResult.postValue(
                    ResponseBankList(
                        BanksDetail(message = "Something went wrong with fetching bank list")
                    )
                )
            }
        }
    }

    fun fetchTopUp(topUpRequest: TopUpRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _topUpApiResult.postValue(Pair(topUpRequest, networkRepo.topUp(topUpRequest)))
            } catch (e: Exception) {
                _topUpApiResult.postValue(
                    Pair(topUpRequest, TopUpResponse(AppConstants.FAIL))
                )
            }
        }
    }
}