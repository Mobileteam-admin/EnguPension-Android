package com.example.engu_pension_verification_application.ui.fragment.service

import com.example.engu_pension_verification_application.model.response.ResponseBankList

interface ActiveServiceViewCallBack {
    fun onBankDetailsSuccess(response: ResponseBankList)
    fun onBankDetailsFailure(response: ResponseBankList)
}