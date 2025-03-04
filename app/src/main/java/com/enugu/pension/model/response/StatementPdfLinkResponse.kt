package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class StatementPdfLinkResponse(
    @SerializedName("message") var message: String? = null,
    @SerializedName("download_url") var downloadUrl: String? = null,
)