package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputLogout(
    @field:SerializedName("email")  // userid
    val email: String? = null
)
