package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ResponseEinNumber(

	@field:SerializedName("detail")
	val detail: EinNumberDetail? = null
)

data class EinNumberDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
