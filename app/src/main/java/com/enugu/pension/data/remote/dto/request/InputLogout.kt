package com.enugu.pension.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class InputLogout(
    @field:SerializedName("email")  // userid
    val email: String? = null
)
