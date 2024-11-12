package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseResetPassword(

	@field:SerializedName("detail")
	val reset_detail: com.example.engu_pension_verification_application.model.response.ResetDetail? = null
)

data class ResetDetail(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
