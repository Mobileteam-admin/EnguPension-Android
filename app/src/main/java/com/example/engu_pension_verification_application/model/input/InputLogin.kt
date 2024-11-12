package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputLogin(
	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("email_or_phone_number")
	val emailOrPhoneNumber: String? = null
)

