package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveProcessingVerify(

	@field:SerializedName("detail")
	val detail: ProcessVerifyDetail? = null
)

data class ProcessVerifyDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user_govt_verified")
	val userGovtVerified: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
