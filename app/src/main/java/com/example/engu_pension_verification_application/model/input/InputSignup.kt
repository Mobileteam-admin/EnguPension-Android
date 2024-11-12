package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputSignup(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null,

	@field:SerializedName("confirm_password")
	val confirmPassword: String? = null
)
