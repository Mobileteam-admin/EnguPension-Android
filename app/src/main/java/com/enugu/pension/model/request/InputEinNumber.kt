package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class InputEinNumber(

	@field:SerializedName("ein")
	val ein: String? = null
)
