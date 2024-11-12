package com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number

import com.example.engu_pension_verification_application.model.response.ResponseEinNumber

interface EIN_Number_CallBack {

//Ein CallBack
    fun onEinNumberSubmitSuccess(response: ResponseEinNumber)
    fun onEinNumberSubmitFailure(response: ResponseEinNumber)
}