package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class SignupResponse(

	@field:SerializedName("detail")
	val detail: com.enugu.pension.model.response.Detail? = null
)

data class Detail(

	@field:SerializedName("userdetails")
	val userdetails: com.enugu.pension.model.response.Userdetails? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Userdetails(

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
