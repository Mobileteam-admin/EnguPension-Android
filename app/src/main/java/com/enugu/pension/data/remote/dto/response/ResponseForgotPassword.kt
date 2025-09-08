package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ResponseForgotPassword(

	@field:SerializedName("detail")
	val forgot_detail: ForgotPasswordDetail? = null
)

data class ForgotPasswordDetail(

	@field:SerializedName("unique_token")
	val uniqueToken: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
