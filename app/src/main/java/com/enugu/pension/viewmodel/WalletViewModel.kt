package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.TopUpRequest
import com.enugu.pension.model.response.BanksDetail
import com.enugu.pension.model.response.ListBanksItem
import com.enugu.pension.model.response.PaymentStatusResponse
import com.enugu.pension.model.response.ResponseBankList
import com.enugu.pension.model.response.TopUpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WalletViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    var selectedBankItemPosition: Int? = null
    val bankItems = mutableListOf<ListBanksItem?>()

    private val _bankListApiResult = MutableLiveData<ResponseBankList>()
    val bankListApiResult: LiveData<ResponseBankList>
        get() = _bankListApiResult

    private val _topUpApiResult = MutableLiveData<Pair<TopUpRequest, TopUpResponse>?>(null)
    val topUpApiResult: LiveData<Pair<TopUpRequest, TopUpResponse>?>
        get() = _topUpApiResult

    private val _paymentResult = MutableLiveData<Pair<String,PaymentStatusResponse>?>(null)
    val paymentResult: LiveData<Pair<String,PaymentStatusResponse>?>
        get() = _paymentResult

    fun resetTopUpApiResult() {
        _topUpApiResult.value = null
    }

    fun resetPaymentResult() {
        _paymentResult.value = null
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

    fun fetchTopUp(topUpRequest: TopUpRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = networkRepo.topUpCall(topUpRequest)
            call.enqueue(object : Callback<TopUpResponse> {
                override fun onResponse(
                    call: Call<TopUpResponse>,
                    response: Response<TopUpResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _topUpApiResult.postValue(Pair(topUpRequest, response.body()!!))
                    } else {
                        var message: String? = null
                        try {
                            response.errorBody()?.let {
                                val jsonObject = JSONObject(it.string())
                                message = jsonObject.getJSONObject("detail").getString("message")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            _topUpApiResult.postValue(
                                Pair(
                                    topUpRequest,
                                    TopUpResponse(
                                        TopUpResponse.Detail(
                                            status = AppConstants.FAIL, message = message
                                        )
                                    )
                                )
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<TopUpResponse>, t: Throwable) {
                }
            })
        }
    }

    fun fetchPaymentStatus(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _paymentResult.postValue(Pair(sessionId, networkRepo.getPaymentStatus(sessionId)))
            } catch (e: Exception) {
                val errorResponse = PaymentStatusResponse(
                    PaymentStatusResponse.Detail(
                        status = AppConstants.FAIL,
                        message = "Failed to fetch payment result"
                    )
                )
                _paymentResult.postValue(Pair(sessionId, errorResponse))
            }
        }
    }
}