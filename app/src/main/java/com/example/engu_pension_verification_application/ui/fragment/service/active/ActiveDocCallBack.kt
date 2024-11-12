package com.example.engu_pension_verification_application.ui.fragment.service.active

import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload

interface ActiveDocCallBack {

    fun onDocUploadSuccess(response: ResponseActiveDocUpload)

    fun onDocUploadFailure(response: ResponseActiveDocUpload)

    fun onDocRetriveSuccess(response: ResponseActiveDocRetrive)

    fun onDocRetriveFailure(response: ResponseActiveDocRetrive)
}