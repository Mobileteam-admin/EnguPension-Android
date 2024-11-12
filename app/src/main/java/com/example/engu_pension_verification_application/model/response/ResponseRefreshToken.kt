package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseRefreshToken(

	@field:SerializedName("detail")
	val token_detail: TokenDetail? = null
)

data class TokenDetail(

	@field:SerializedName("access")
	val access: String? = null,

	@field:SerializedName("refresh")
	val refresh: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
