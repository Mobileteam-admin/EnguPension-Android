package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseForgotPassword(

	@field:SerializedName("detail")
	val forgot_detail: com.example.engu_pension_verification_application.model.response.ForgotPasswordDetail? = null
)

data class ForgotPasswordDetail(

	@field:SerializedName("unique_token")
	val uniqueToken: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
