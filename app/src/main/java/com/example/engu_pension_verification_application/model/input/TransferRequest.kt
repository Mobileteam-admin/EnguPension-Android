package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class TransferRequest(
    @field:SerializedName("amount") val amount: Float,
    @field:SerializedName("currency") val currency: String,
    @field:SerializedName("description") val description: String,
)