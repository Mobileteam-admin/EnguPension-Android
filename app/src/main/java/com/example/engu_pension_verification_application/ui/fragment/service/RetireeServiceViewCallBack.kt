package com.example.engu_pension_verification_application.ui.fragment.service

import com.example.engu_pension_verification_application.model.response.ResponseBankList

interface RetireeServiceViewCallBack {
    fun onBankDetailsSuccess(response: ResponseBankList)
    fun onBankDetailsFailure(response: ResponseBankList)

}
