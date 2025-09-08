package com.enugu.pension.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class InputForgotPassword(
    @field:SerializedName("email_or_phone_number")
    val email: String? = null
)
