package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveBasicRetrive(

	@field:SerializedName("detail")
	val detail: ActiveRetriveDetail? = null
)

data class ActiveRetriveDetail(

	@field:SerializedName("user_profile_details")
	val userProfileDetails: ActiveRetriveUserProfileDetails? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("token_status")
	val tokenStatus: String? = null
)

data class ActiveRetriveUserProfileDetails(

	@field:SerializedName("pincode")
	val pincode: String? = null,

	@field:SerializedName("next_of_kin_pincode")
	val kinPincode: String? = null,

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("occupation")
	val occupation: String? = null,

	@field:SerializedName("sex")
	val sex: String? = null,

	@field:SerializedName("lga")
	val lga: String? = null,

	@field:SerializedName("sub_treasury")
	val subTreasury: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("middle_name")
	val middleName: String? = null,

	@field:SerializedName("date_of_appointment")
	val dateOfAppointment: String? = null,

	@field:SerializedName("next_of_kin_address")
	val nextOfKinAddress: String? = null,

	@field:SerializedName("user_type")
	val userType: String? = null,

	@field:SerializedName("next_of_kin_phone_number")
	val nextOfKinPhoneNumber: String? = null,

	@field:SerializedName("grade_level")
	val gradeLevel: String? = null,

//	@field:SerializedName("user_id")
//	val userId: Int? = null,

	@field:SerializedName("dob")
	val dob: String? = null,

	@field:SerializedName("next_of_kin_name")
	val nextOfKinName: String? = null,

	@field:SerializedName("next_of_kin_email")
	val nextOfKinEmail: String? = null,

	@field:SerializedName("first_name")
	val firstName: String? = null
)
