package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ResponseLogin(

	@field:SerializedName("detail")
	val login_detail: com.enugu.pension.model.response.LoginDetail? = null
)

data class LoginDetail(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("refresh_token")
	val refreshToken: String? = null,

	@field:SerializedName("user_id")
	val user_id: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user_govt_verified")
	val userGovtVerified: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
