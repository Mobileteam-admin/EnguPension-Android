package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.ApiResult
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.ExtraBankAccountRequest
import com.enugu.pension.model.request.InputBankVerification
import com.enugu.pension.model.response.AccountTypeItem
import com.enugu.pension.model.response.BankVerifyDetail
import com.enugu.pension.model.response.BanksDetail
import com.enugu.pension.model.response.ExtraBankAccountResponse
import com.enugu.pension.model.response.ListBanksItem
import com.enugu.pension.model.response.ResponseBankList
import com.enugu.pension.model.response.ResponseBankVerify
import com.enugu.pension.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBankViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    companion object {
        const val BANK_DEFAULT_ITEM_INDEX = 0
        const val ACC_TYPE_DEFAULT_ITEM_INDEX = 0
    }

    enum class VerificationState {
        NOT_VERIFIED,
        VERIFYING,
        FAILED,
        VERIFIED,
    }

    var selectedBankIndex = BANK_DEFAULT_ITEM_INDEX
    var selectedAccountTypeIndex = ACC_TYPE_DEFAULT_ITEM_INDEX
    val bankItems = ArrayList<ListBanksItem?>()
    val accountTypeItems = ArrayList<AccountTypeItem?>()
    val verificationState = MutableLiveData(VerificationState.NOT_VERIFIED)
    private val _bankListApiResult = MutableLiveData<ResponseBankList>()
    val bankListApiResult: LiveData<ResponseBankList>
        get() = _bankListApiResult

    private val _extraBankAccountResult =
        MutableLiveData<Pair<ExtraBankAccountRequest, ExtraBankAccountResponse>>()
    val extraBankAccountResult: LiveData<Pair<ExtraBankAccountRequest, ExtraBankAccountResponse>>
        get() = _extraBankAccountResult

    private val _bankVerificationResult =
        MutableLiveData<Pair<InputBankVerification, ResponseBankVerify>>()
    val bankVerificationResult: LiveData<Pair<InputBankVerification, ResponseBankVerify>>
        get() = _bankVerificationResult


    fun createExtraBankAccount(request: ExtraBankAccountRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _extraBankAccountResult.postValue(
                    Pair(request, networkRepo.createExtraBankAccount(request))
                )
            } catch (e: Exception) {
                _extraBankAccountResult.postValue(
                    Pair(
                        request,
                        ExtraBankAccountResponse(
                            ExtraBankAccountResponse.Detail(message = "Something went wrong with bank account creation")
                        )
                    )
                )
            }
        }
    }

    fun verifyBankAccount(inputBankVerification: InputBankVerification) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = networkRepo.verifyBankAccount(inputBankVerification)
            val response = when (val apiResult = NetworkUtils.handleResponse(call)) {
                is ApiResult.Success -> apiResult.data
                is ApiResult.Error ->
                    ResponseBankVerify(
                        BankVerifyDetail(message = apiResult.message)
                    )
            }
            _bankVerificationResult.postValue(
                Pair(
                    inputBankVerification,
                    response
                )
            )
        }
    }

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
}