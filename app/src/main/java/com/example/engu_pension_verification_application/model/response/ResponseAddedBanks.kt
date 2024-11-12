package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseAddedBanks(

	@field:SerializedName("detail")
	val detail: BankDetails? = null
)

data class BanksItem(

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("ussd")
	val ussd: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("slug")
	val slug: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,
)

data class BankDetails(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("token_validity_time_remaining")
	val tokenValidityTimeRemaining: String? = null,

	@field:SerializedName("Banks")
	val banks: List<com.example.engu_pension_verification_application.model.response.BanksItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
