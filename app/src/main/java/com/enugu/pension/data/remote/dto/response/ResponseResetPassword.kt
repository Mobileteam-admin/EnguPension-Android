package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ResponseResetPassword(

	@field:SerializedName("detail")
	val reset_detail: ResetDetail? = null
)

data class ResetDetail(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
