package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseSwiftBankCode(

	@field:SerializedName("detail")
	val swiftbankdetail: SwiftBankDetail? = null
)

data class SwiftBankDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("swift_code_response")
	val swiftCodeResponse: SwiftCodeResponse? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class SwiftCodeResponse(

	@field:SerializedName("bank_code")
	val bankCode: String? = null,

	@field:SerializedName("city_name")
	val cityName: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("bank_id")
	val bankId: String? = null,

	@field:SerializedName("branch_name")
	val branchName: String? = null,

	@field:SerializedName("postcode")
	val postcode: String? = null,

	@field:SerializedName("country_name")
	val countryName: String? = null,

	@field:SerializedName("bank_name")
	val bankName: String? = null,

	@field:SerializedName("city_id")
	val cityId: String? = null
)
