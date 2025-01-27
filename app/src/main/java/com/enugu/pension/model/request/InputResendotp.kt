package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputResendotp(

	@field:SerializedName("email")
	val email: String? = null
)
