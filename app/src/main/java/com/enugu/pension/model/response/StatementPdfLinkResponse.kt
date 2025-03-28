package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class StatementPdfLinkResponse(
    @SerializedName("detail") var detail: Detail,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("file_url") var fileUrl: String? = null,
    )
}