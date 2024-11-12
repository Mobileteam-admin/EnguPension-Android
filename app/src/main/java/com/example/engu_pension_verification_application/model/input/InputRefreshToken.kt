package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputRefreshToken(
    @field:SerializedName("refresh_token")  // token
    val refreshtoken: String? = null
)
