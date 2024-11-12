package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import com.example.engu_pension_verification_application.model.response.ResponseBankInfo
import com.example.engu_pension_verification_application.model.response.ResponseSwiftBankCode

interface RetireeBankCallBack {

    fun onSwiftBankCodeSuccess(response: ResponseSwiftBankCode)
    fun onSwiftBankCodeFailure(response: ResponseSwiftBankCode)

    fun onRetireeBankInfoSubmitSuccess(response: ResponseBankInfo)
    fun onRetireeBankInfoSubmitFailure(response: ResponseBankInfo)
}