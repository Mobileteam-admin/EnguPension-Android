package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class AccountCompletionStatusResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("user_account_details_save_completed") var userAccountDetailsSaveCompleted: Boolean,
        @SerializedName("message") var message: String? = null
    )
}