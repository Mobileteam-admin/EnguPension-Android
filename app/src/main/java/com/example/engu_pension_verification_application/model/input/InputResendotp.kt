package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputResendotp(

	@field:SerializedName("email")
	val email: String? = null
)
