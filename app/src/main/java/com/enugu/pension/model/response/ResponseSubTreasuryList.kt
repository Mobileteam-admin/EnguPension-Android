package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ResponseSubTreasuryList(

	@field:SerializedName("detail")
	val subtreasurydetail: com.enugu.pension.model.response.SubTreasuryDetail? = null
)

data class SubTreasuryDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("lgas")
	val subtreasurylist: List<com.enugu.pension.model.response.SubTreasuryItem?>? = null
)

data class SubTreasuryItem(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("state")
	val state: String? = null
)
