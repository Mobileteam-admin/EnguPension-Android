package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseOccupation(

	@field:SerializedName("detail")
	val occupationdetail: com.example.engu_pension_verification_application.model.response.OccupationDetail? = null
)

data class OccupationsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("category")
	val category: String? = null
)

data class OccupationDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("occupations")
	val occupations: List<com.example.engu_pension_verification_application.model.response.OccupationsItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)
