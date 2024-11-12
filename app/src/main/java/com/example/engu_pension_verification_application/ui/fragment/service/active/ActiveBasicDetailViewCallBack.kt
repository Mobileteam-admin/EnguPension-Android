package com.example.engu_pension_verification_application.ui.fragment.service.active

import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicRetrive
import com.example.engu_pension_verification_application.model.response.ResponseCombinationDetails

interface ActiveBasicDetailViewCallBack {
    //submission api
    fun onActiveBasicDetailSuccess(response: ResponseActiveBasicDetails)

    fun onActiveBasicDetailFailure(response: ResponseActiveBasicDetails)
    //listing api
    /*fun onActiveBasicCombinedDetailSuccess(response: ResponseCombinationDetails)

    fun onActiveBasicCombinedDetailFailure(response: ResponseCombinationDetails)*/


    fun onAcombinedDetailSuccess(response: ResponseCombinationDetails)
    fun onAcombinedDetailFail(response: ResponseCombinationDetails)

    fun onActiveRetriveSuccess(response: ResponseActiveBasicRetrive)
    fun onActiveRetriveFailure(response: ResponseActiveBasicRetrive)

}