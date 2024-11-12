package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputEinNumber(

	@field:SerializedName("ein")
	val ein: String? = null
)
