package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engu_pension_verification_application.data.ApiResult
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.BankDetail
import com.example.engu_pension_verification_application.model.response.BankVerifyDetail
import com.example.engu_pension_verification_application.model.response.BanksDetail
import com.example.engu_pension_verification_application.model.response.EinNumberDetail
import com.example.engu_pension_verification_application.model.response.ResponseBankInfo
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseBankVerify
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber
import com.example.engu_pension_verification_application.model.response.ResponseSwiftBankCode
import com.example.engu_pension_verification_application.model.response.SwiftBankDetail
import com.example.engu_pension_verification_application.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveBankViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _bankListApiResult = MutableLiveData<ResponseBankList>()
    val bankListApiResult: LiveData<ResponseBankList>
        get() = _bankListApiResult

    private val _bankDetailsApiResult = MutableLiveData<Pair<String, ResponseSwiftBankCode>>()
    val bankDetailsApiResult: LiveData<Pair<String, ResponseSwiftBankCode>>
        get() = _bankDetailsApiResult

    private val _bankInfoSubmissionResult =
        MutableLiveData<Pair<InputActiveBankInfo, ResponseBankInfo>>()
    val bankInfoSubmissionResult: LiveData<Pair<InputActiveBankInfo, ResponseBankInfo>>
        get() = _bankInfoSubmissionResult

    private val _bankVerificationResult =
        MutableLiveData<Pair<InputBankVerification, ResponseBankVerify>>()
    val bankVerificationResult: LiveData<Pair<InputBankVerification, ResponseBankVerify>>
        get() = _bankVerificationResult

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
                        ResponseSwiftBankCode(
                            SwiftBankDetail(message = "Something went wrong with fetching bank details")
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