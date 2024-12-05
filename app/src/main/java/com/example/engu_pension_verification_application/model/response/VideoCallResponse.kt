package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class VideoCallResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("room_name") var roomName: String? = null
    )
}