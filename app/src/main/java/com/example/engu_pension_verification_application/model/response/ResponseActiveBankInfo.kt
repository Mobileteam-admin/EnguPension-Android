package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveBankInfo(

	@field:SerializedName("detail")
	val detail: String? = null,

	@field:SerializedName("bank_detail")
	val bankDetail: ResBankDetail? = null
)

data class ResBankDetail(

	@field:SerializedName("account_number")
	val accountNumber: String? = null,

	@field:SerializedName("bank_code")
	val bankCode: String? = null,

	@field:SerializedName("account_type")
	val accountType: String? = null,

	@field:SerializedName("bank_id")
	val bankId: String? = null,

	@field:SerializedName("swift_code")
	val swiftCode: String? = null,

	@field:SerializedName("account_holder_name")
	val accountHolderName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
