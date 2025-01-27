package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ResponseGradeLevel(

	@field:SerializedName("detail")
	val gradeLeveldetail: com.enugu.pension.model.response.GradeLevelDetail? = null
)

data class GradeLevelsItem(

	@field:SerializedName("level")
	val level: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class GradeLevelDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("grade_levels")
	val gradeLevels: List<com.enugu.pension.model.response.GradeLevelsItem?>? = null
)
