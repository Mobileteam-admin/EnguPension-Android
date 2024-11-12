package com.example.engu_pension_verification_application.ui.fragment.service.active

import com.example.engu_pension_verification_application.model.response.ResponseBankInfo
import com.example.engu_pension_verification_application.model.response.ResponseBankList
import com.example.engu_pension_verification_application.model.response.ResponseSwiftBankCode

interface ActiveBankCallBack {

  /*  fun onBankListSuccess(response: ResponseBankList)
    fun onBankListFailure(response: ResponseBankList)*/

    fun onSwiftBankCodeSuccess(response: ResponseSwiftBankCode)
    fun onSwiftBankCodeFailure(response: ResponseSwiftBankCode)

    fun onActiveBankInfoSubmitSuccess(response: ResponseBankInfo)
    fun onActiveBankInfoSubmitFailure(response: ResponseBankInfo)

}