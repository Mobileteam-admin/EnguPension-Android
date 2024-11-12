package com.example.engu_pension_verification_application.ui.fragment.service.ProcessingVerify

import com.example.engu_pension_verification_application.model.response.ResponseActiveProcessingVerify
import com.example.engu_pension_verification_application.model.response.ResponseEinNumber

interface Processing_Verify_CallBack {
    //ProcessingVerify CallBack
    fun onProcessingVerifySuccess(response: ResponseActiveProcessingVerify)
    fun onProcessingVerifyFailure(response: ResponseActiveProcessingVerify)
}