package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicRetrive
import com.example.engu_pension_verification_application.model.response.ResponseCombinationDetails
import com.example.engu_pension_verification_application.model.response.ResponseRetireeBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseRetireeBasicRetrive

interface RetireeBasicDetailsViewCallBack {
    fun onRcombinedDetailSuccess(response: ResponseCombinationDetails)
    fun onRcombinedDetailFail(response: ResponseCombinationDetails)

    fun onRetireeBasicDetailSuccess(response: ResponseRetireeBasicDetails)

    fun onRetireeBasicDetailFailure(response: ResponseRetireeBasicDetails)

    fun onRetireeRetriveSuccess(response: ResponseRetireeBasicRetrive)
    fun onRetireeRetriveFailure(response: ResponseRetireeBasicRetrive)
}