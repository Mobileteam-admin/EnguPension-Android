package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class TransferRequest(
    @field:SerializedName("amount") val amount: Float,
    @field:SerializedName("currency") val currency: String,
    @field:SerializedName("description") val description: String,
)