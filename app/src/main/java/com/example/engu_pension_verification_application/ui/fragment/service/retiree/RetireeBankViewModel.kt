package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.util.Log
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RetireeBankViewModel(var retireeBankCallBack: RetireeBankFragment) {

    private val prefs = SharedPref

    /*private val _banklistStatus = MutableLiveData<ResponseBankList>()
    val BanklistStatus: LiveData<ResponseBankList>
        get() = _banklistStatus

    private val _SwiftBankCodeStatus = MutableLiveData<ResponseSwiftBankCode>()
    val SwiftBankCodeStatus: LiveData<ResponseSwiftBankCode>
        get() = _SwiftBankCodeStatus

    private val _RetireeBankInfoStatus = MutableLiveData<ResponseActiveBankInfo>()
    val RetireeBankInfoStatus : LiveData<ResponseActiveBankInfo>
        get() = _RetireeBankInfoStatus*/

    /*  init {
          application.let { prefs.with(it) }
      }*/
    fun RetireeBankinformation(inputActiveBankInfo: InputActiveBankInfo) {
        GlobalScope.launch(Dispatchers.Main) {
            try {

                Log.d("Inputs", "FinishFnCall: " + inputActiveBankInfo)
                Log.d("token", "FinishFnCall: " + prefs.access_token.toString())
                val response = ApiClient.getApiInterface().getActiveBankInfo(
                    "Bearer " + prefs.access_token.toString(),
                    inputActiveBankInfo
                )

                /*        _RetireeBankInfoStatus.value = response*/
                if (response!!.detail?.status.equals("success")) {
                    retireeBankCallBack.onRetireeBankInfoSubmitSuccess(response)/*  prefs.user_id = response.detail.toString()*/
                    Log.d("activeBankinfo", "$response.detail?.message.toString()")
                } else {
                    retireeBankCallBack.onRetireeBankInfoSubmitFailure(response)
                }

            } catch (e: java.lang.Exception) {
                Loader.hideLoader()

                retireeBankCallBack.onRetireeBankInfoSubmitFailure(
                    ResponseBankInfo(
                        BankDetail(message = "Something went wrong in Bank Submit Api ")
                    )
                )

                Log.d("TAG", "RetireeBankinformation: " + e.localizedMessage)
//                _ActiveBankInfoStatus.value = ResponseActiveBankInfo(ResBankDetail( "Something Went Wrong "))
            }
        }
    }


    /*    fun getBankList() {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val response = ApiClient.getRetrofit().getAddedBanks(
                        "Bearer " + prefs.access_token.toString())
                    // Log.d("Model", "getBankList: "+response.banks)
                    if (response.detail?.status.equals("success")) {
                        // Log.d("Model", "getBankList: "+response.banks)
                        _banklistStatus.value = response
                    } else {
                        _banklistStatus.value = response
                    }

                } catch (e: java.lang.Exception) {
                    Log.d("Model", "getBankList: "+e.localizedMessage)
    //                _banklistStatus.value = ResponseBankList(BankDetail(message = "Something went wrong"))
                }
            }
        }*/
    fun getswiftBankCode(inputswiftcode: InputSwiftBankCode) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getApiInterface().getSwiftBankCode(
                    "Bearer " + prefs.access_token.toString(), inputswiftcode
                )

                if (response.swiftbankdetail?.status.equals("success")) {
                    // Log.d("Model", "getBankList: "+response.banks)
                    retireeBankCallBack.onSwiftBankCodeSuccess(response)
                } else {
                    retireeBankCallBack.onSwiftBankCodeFailure(response)
                }

            } catch (e: java.lang.Exception) {
                Log.d("bank_swift_error", "${ResponseSwiftBankCode(SwiftBankDetail())}")

                retireeBankCallBack.onSwiftBankCodeFailure(
                    ResponseSwiftBankCode(
                        SwiftBankDetail(message = "Something went wrong in swift api")
                    )
                )

                // _SwiftBankCodeStatus.value = ResponseSwiftBankCode(SwiftBankDetail(message = "Something went wrong"))
            }
        }
    }
}