package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputLogin(
	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("email_or_phone_number")
	val emailOrPhoneNumber: String? = null
)

