package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputBankVerification(

    @field:SerializedName("account_number")
    val accountNumber: String? = null,

    @field:SerializedName("bank_code")
    val bankCode: String? = null
)

