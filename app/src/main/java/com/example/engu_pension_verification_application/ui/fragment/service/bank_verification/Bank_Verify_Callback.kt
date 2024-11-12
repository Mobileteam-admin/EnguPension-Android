package com.example.engu_pension_verification_application.ui.fragment.service.bank_verification

import com.example.engu_pension_verification_application.model.response.ResponseBankVerify
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber

interface Bank_Verify_Callback
{
        fun onBankVerifySubmitSuccess(response: ResponseBankVerify)
        fun onBankVerifySubmitFailure(response: ResponseBankVerify)
}