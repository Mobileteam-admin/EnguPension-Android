package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseLGAList(

	@field:SerializedName("detail")
	val lgadetail: com.example.engu_pension_verification_application.model.response.lBankDetail? = null
)

data class LgasItem(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("state")
	val state: String? = null
)

data class lBankDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("lgas")
	val lgaslist: List<com.example.engu_pension_verification_application.model.response.LgasItem?>? = null
)
