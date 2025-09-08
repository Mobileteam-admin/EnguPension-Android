package com.enugu.pension.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class InputEinNumber(

	@field:SerializedName("ein")
	val ein: String? = null
)
