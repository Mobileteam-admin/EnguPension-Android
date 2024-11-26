package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class TopUpRequest(

    @SerializedName("user_id")
    var userId: Int,

    @SerializedName("bank_id")
    var bankId: Int,

    @SerializedName("amount")
    var amount: Float,

    @SerializedName("currency")
    var currency: String,

    @SerializedName("description")
    var description: String? = "Top-up for pension wallet",
)