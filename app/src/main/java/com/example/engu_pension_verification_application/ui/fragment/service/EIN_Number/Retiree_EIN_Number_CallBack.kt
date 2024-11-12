package com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number

import com.example.engu_pension_verification_application.model.response.ResponseEinNumber

interface Retiree_EIN_Number_CallBack {

    //Retiree Ein CallBack
    fun onRetireeEinNumberSubmitSuccess(response: ResponseEinNumber)
    fun onRetireeEinNumberSubmitFailure(response: ResponseEinNumber)
}