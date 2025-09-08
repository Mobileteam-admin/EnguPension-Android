package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ResponseRefreshToken(

	@field:SerializedName("detail")
	val tokenDetail: com.enugu.pension.data.remote.dto.response.TokenDetail? = null
)

data class TokenDetail(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("refresh_token")
	val refreshToken: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("token_status")
	val tokenStatus: String? = null
)
