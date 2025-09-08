package com.enugu.pension.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class InputResetPassword(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("unique_token")
	val token: String? = null
)
