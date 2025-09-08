package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NextOfKinUpdateResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
    )
}