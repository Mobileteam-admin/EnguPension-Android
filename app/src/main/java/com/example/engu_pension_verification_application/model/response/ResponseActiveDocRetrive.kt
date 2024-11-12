package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseActiveDocRetrive(

	@field:SerializedName("detail")
	val detail: ActiveDocRetriveDetail? = null
)

data class FileUrlResponse(

	@field:SerializedName("promotion_letter_transfer_letter_file_url")
	val promotionLetterTransferLetterFileUrl: String? = null,

	@field:SerializedName("passport_photo_file_url")
	val passportPhotoFileUrl: String? = null,

	@field:SerializedName("application_form_file_url")
	val applicationFormFileUrl: String? = null,

	@field:SerializedName("id_card_file_url")
	val idCardFileUrl: String? = null,

	@field:SerializedName("clearance_form_file_url")
	val clearanceFormFileUrl: String? = null
)

data class ActiveDocRetriveDetail(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("file_url_response")
	val fileUrlResponse: FileUrlResponse? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("file_present_check")
	val filePresentCheck: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
