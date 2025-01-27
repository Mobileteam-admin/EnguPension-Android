package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputRefreshToken(
    @field:SerializedName("refresh_token")  // token
    val refreshtoken: String? = null
)
