package com.example.engu_pension_verification_application.ui.fragment.service

import android.util.Log
import com.example.engu_pension_verification_application.model.response.BanksDetail
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActiveServicePresenter(var activeServiceViewCallBack: ActiveServiceViewCallBack) {

    private val prefs = SharedPref

    fun getBankList() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.getRetrofit().getAddedBanks(
                    "Bearer " + prefs.access_token.toString()
                ) //
                // Log.d("Model", "getBankList: "+response.banks)
                if (response.detail?.status.equals("success")) {

                    // Log.d("Model", "getBankList: "+response.banks)
                    //_banklistStatus.value = response
                    activeServiceViewCallBack.onBankDetailsSuccess(response)
                } else {
                    //_banklistStatus.value = response
                    activeServiceViewCallBack.onBankDetailsFailure(response)
                }

            } catch (e: java.lang.Exception) {
                Log.d("Model", "getBankList: " + e.localizedMessage)
                activeServiceViewCallBack.onBankDetailsFailure(ResponseBankList(BanksDetail(message = "Something went wrong GetBank api")))
//                _banklistStatus.value = ResponseBankList(BankDetail(message = "Something went wrong"))
            }
        }
    }
}