package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.ApiResult
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.InputActiveBankInfo
import com.enugu.pension.model.request.InputBankVerification
import com.enugu.pension.model.request.InputSwiftBankCode
import com.enugu.pension.model.response.BankDetail
import com.enugu.pension.model.response.BankVerifyDetail
import com.enugu.pension.model.response.BanksDetail
import com.enugu.pension.model.response.EinNumberDetail
import com.enugu.pension.model.response.ResponseBankInfo
import com.enugu.pension.model.response.ResponseBankList
import com.enugu.pension.model.response.ResponseBankVerify
import com.enugu.pension.model.response.ResponseEinNumber
import com.enugu.pension.model.response.SwiftCodeVerificationResponse
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.VerificationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveBankViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    val swiftCodeState = MutableLiveData(VerificationState.VERIFY)
    val bankCodeState = MutableLiveData(VerificationState.VERIFY)

    private val _bankListApiResult = MutableLiveData<ResponseBankList>()
    val bankListApiResult: LiveData<ResponseBankList>
        get() = _bankListApiResult

    private val _bankDetailsApiResult = MutableLiveData<Pair<String, SwiftCodeVerificationResponse>?>()
    val bankDetailsApiResult: MutableLiveData<Pair<String, SwiftCodeVerificationResponse>?>
        get() = _bankDetailsApiResult
    fun resetBankDetailsApiResult() {
        _bankDetailsApiResult.value = null
    }

    private val _bankInfoSubmissionResult =
        MutableLiveData<Pair<InputActiveBankInfo, ResponseBankInfo>>()
    val bankInfoSubmissionResult: LiveData<Pair<InputActiveBankInfo, ResponseBankInfo>>
        get() = _bankInfoSubmissionResult

    private val _bankVerificationResult =
        MutableLiveData<Pair<InputBankVerification, ResponseBankVerify>?>()
    val bankVerificationResult: MutableLiveData<Pair<InputBankVerification, ResponseBankVerify>?>
        get() = _bankVerificationResult
    fun resetBankVerificationResult() {
        _bankVerificationResult.value = null
    }

    private val _einSubmissionResult =
        MutableLiveData<Pair<String, ResponseEinNumber>>()
    val einSubmissionResult: LiveData<Pair<String, ResponseEinNumber>>
        get() = _einSubmissionResult

    fun fetchBankDetails(swiftCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bankDetailsApiResult.postValue(
                    Pair(swiftCode, networkRepo.fetchBankDetails(InputSwiftBankCode(swiftCode)))
                )
            } catch (e: Exception) {
                _bankDetailsApiResult.postValue(
                    Pair(
                        swiftCode,
                        SwiftCodeVerificationResponse(
                            SwiftCodeVerificationResponse.Detail(message = "Something went wrong with verifying swift code.")
                        )
                    )
                )
            }
        }
    }

    fun submitBankInfo(inputActiveBankInfo: InputActiveBankInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _bankInfoSubmissionResult.postValue(
                    Pair(inputActiveBankInfo, networkRepo.submitBankInfo(inputActiveBankInfo))
                )
            } catch (e: Exception) {
                _bankInfoSubmissionResult.postValue(
                    Pair(
                        inputActiveBankInfo,
                        ResponseBankInfo(
                            BankDetail(message = "Something went wrong with bank info submission")
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

    fun submitEin(ein: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _einSubmissionResult.postValue(
                    Pair(ein, networkRepo.submitEin(ein))
                )
            } catch (e: Exception) {
                _einSubmissionResult.postValue(
                    Pair(
                        ein,
                        ResponseEinNumber(
                            EinNumberDetail(message = "Something went wrong with EIN submission")
                        )
                    )
                )
            }
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