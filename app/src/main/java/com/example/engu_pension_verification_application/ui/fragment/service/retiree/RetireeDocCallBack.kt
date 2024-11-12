package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocUpload

interface RetireeDocCallBack {

    fun onDocUploadSuccess(response: ResponseRetireeDocUpload)

    fun onDocUploadFailure(response: ResponseRetireeDocUpload)


    fun onRetireeDocRetriveSuccess(response: ResponseRetireeDocRetrive)
    fun onRetireeDocRetriveFailure(response: ResponseRetireeDocRetrive)
}