package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputLogout(
    @field:SerializedName("email")  // userid
    val email: String? = null
)
