package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseLogout(

	@field:SerializedName("detail")
	val logout_detail: com.example.engu_pension_verification_application.model.response.LogoutDetail? = null
)

data class LogoutDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
