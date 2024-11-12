package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputSignupVerify(

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)