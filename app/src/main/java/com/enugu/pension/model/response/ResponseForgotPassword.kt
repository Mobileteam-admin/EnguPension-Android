package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ResponseForgotPassword(

	@field:SerializedName("detail")
	val forgot_detail: com.enugu.pension.model.response.ForgotPasswordDetail? = null
)

data class ForgotPasswordDetail(

	@field:SerializedName("unique_token")
	val uniqueToken: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
