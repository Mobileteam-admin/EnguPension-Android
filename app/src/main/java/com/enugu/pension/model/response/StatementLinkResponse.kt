package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class StatementLinkResponse(
    @SerializedName("download_link") var downloadLink: String? = null
)