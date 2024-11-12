package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseBankVerify(

	@field:SerializedName("detail")
	val detail: BankVerifyDetail? = null
)

data class BankVerifyDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("data")
	val data: BankVerifyData? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class BankVerifyData(

	@field:SerializedName("account_number")
	val accountNumber: String? = null,

	@field:SerializedName("account_name")
	val accountName: String? = null
)
