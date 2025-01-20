package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class StatementLinkResponse(
    @SerializedName("download_link") var downloadLink: String? = null
)