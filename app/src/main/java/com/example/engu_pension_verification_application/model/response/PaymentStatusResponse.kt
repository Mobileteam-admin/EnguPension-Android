package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(

    @field:SerializedName("detail") val detail: Detail? = null,
) {

    data class Detail(

        @field:SerializedName("status") val status: String? = null,

        @field:SerializedName("message") val message: String? = null,
    )

}
