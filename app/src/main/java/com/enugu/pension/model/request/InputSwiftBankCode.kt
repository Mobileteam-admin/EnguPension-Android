package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputSwiftBankCode(

	@field:SerializedName("swift_code")
	val swiftCode: String? = null
)
