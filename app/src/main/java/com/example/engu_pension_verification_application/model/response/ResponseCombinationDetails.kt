package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseCombinationDetails(

	@field:SerializedName("detail")
	val combinedetails: CombinationDetail? = null
)

data class CombineGradeLevelsItem(

	@field:SerializedName("level")
	val level: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class CombineSubTreasuriesItem(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("state")
	val state: String? = null
)

data class CombineOccupationsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("category")
	val category: String? = null
)

data class CombineLgasItem(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("state")
	val state: String? = null
)

data class CombinationDetail(


	@field:SerializedName("sub_treasuries")
	val combinesubTreasuries: List<CombineSubTreasuriesItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("occupations")
	val combineoccupations: List<CombineOccupationsItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("lgas")
	val combinelgas: List<CombineLgasItem?>? = null,

	@field:SerializedName("grade_levels")
	val combinegradeLevels: List<CombineGradeLevelsItem?>? = null,

	@field:SerializedName("positions")
	val combinelastPositions: List<CombineLastPositions?>? = null,

	@field:SerializedName("local_govenment_pension_boards")
	val combinelocalGovenmentPensionBoards: List<CombineLocalGovenmentPensionBoardsItem?>? = null
)

data class CombineLastPositions(

	@field:SerializedName("position_name")
	val positionname: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class CombineLocalGovenmentPensionBoardsItem(

	@field:SerializedName("position_name")
	val positionName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)



