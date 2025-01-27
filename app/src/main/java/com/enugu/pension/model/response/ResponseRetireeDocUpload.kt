package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ResponseRetireeDocUpload(

	@field:SerializedName("detail")
	val detail: RetireeDocDetail? = null
)

data class RetireeDocDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
