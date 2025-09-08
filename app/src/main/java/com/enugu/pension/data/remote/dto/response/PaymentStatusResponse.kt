package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @field:SerializedName("token_status") val tokenStatus: String? = null,
        @field:SerializedName("status") val status: String? = null,
        @field:SerializedName("message") val message: String? = null,
    )
}
