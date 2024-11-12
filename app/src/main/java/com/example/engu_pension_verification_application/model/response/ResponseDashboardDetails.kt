package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseDashboardDetails(

	@field:SerializedName("detail")
	val detail: DashboardDetails? = null
)

data class DashboardDetails(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("profile_pic")
	val profilePic: String? = null,

	@field:SerializedName("bank_detail")
	val bankDetail: DashboardBankDetails? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("wallet_balance_currency")
	val walletBalanceCurrency: String? = null,

	@field:SerializedName("verification_status")
	val verificationStatus: String? = null,

	@field:SerializedName("wallet_balance_amount")
	val walletBalanceAmount: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DashboardBankDetails(

	@field:SerializedName("bank_code")
	val bankCode: String? = null,

	@field:SerializedName("account_number")
	val accountNumber: String? = null,

	@field:SerializedName("account_type")
	val accountType: String? = null,

	@field:SerializedName("account_holder_name")
	val accountHolderName: String? = null,

	@field:SerializedName("swift_code")
	val swiftCode: String? = null,

	@field:SerializedName("bank_image")
	val bankImage: String? = null,

	@field:SerializedName("bank_name")
	val bankName: String? = null,

	@field:SerializedName("auto_renewal")
	val autoRenewal: Boolean? = null
)
