package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputForgotVerify(

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("email_or_phone_number")
	val email: String? = null
)