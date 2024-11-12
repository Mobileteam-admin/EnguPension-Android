package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class InputSwiftBankCode(

	@field:SerializedName("swift_code")
	val swiftCode: String? = null
)
