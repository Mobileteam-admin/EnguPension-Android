package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResendotpResponse(

	@field:SerializedName("detail")
	val detail: com.example.engu_pension_verification_application.model.response.ResendDetail? = null
)

data class ResendDetail(

	@field:SerializedName("userdetails")
	val userdetails: com.example.engu_pension_verification_application.model.response.ResendUserdetails? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class ResendUserdetails(

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
