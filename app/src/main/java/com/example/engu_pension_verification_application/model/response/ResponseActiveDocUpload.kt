package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveDocUpload(

	@field:SerializedName("detail")
	val detail: ActiveDocDetail? = null
)

data class ActiveDocDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
