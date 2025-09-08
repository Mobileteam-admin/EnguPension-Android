package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveProcessingVerify(

	@field:SerializedName("detail")
	val detail: com.enugu.pension.data.remote.dto.response.ProcessVerifyDetail? = null
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
