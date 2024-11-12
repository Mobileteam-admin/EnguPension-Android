package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseBankList(

	@field:SerializedName("detail")
	val detail: BanksDetail? = null
)

data class AccountTypeItem(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class ListBanksItem(

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("ussd")
	val ussd: String? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("slug")
	val slug: String? = null
)

data class BanksDetail(

	@field:SerializedName("account_type")
	val accountType: ArrayList<AccountTypeItem?>? = null,

	@field:SerializedName("banks")
	val banks: ArrayList<ListBanksItem?>? = null,

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
