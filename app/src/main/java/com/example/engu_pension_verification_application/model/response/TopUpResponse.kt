package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class TopUpResponse(
    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("token_status")
    val tokenStatus: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("checkout_url")
    val checkoutUrl: String? = null,
)