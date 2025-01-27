package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputVerify(

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("email_or_phone_number")
	val email: String? = null
)