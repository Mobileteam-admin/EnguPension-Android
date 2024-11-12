package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class ResponseRetireeDocRetrive(

	@field:SerializedName("detail")
	val detail: RetireeDocRetriveResponse? = null
)

data class RetireeDocRetriveResponse(

	@field:SerializedName("token_status")
	val tokenStatus: String? = null,

	@field:SerializedName("file_url_response")
	val fileUrlResponse: RetireeFileUrlResponse? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("file_present_check")
	val filePresentCheck: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class RetireeFileUrlResponse(

	@field:SerializedName("retirement_notice_file_url")
	val retirementNoticeFileUrl: String? = null,

	@field:SerializedName("payment_authorization_retirement_or_death_file_url")
	val paymentAuthorizationRetirementOrDeathFileUrl: String? = null,

	@field:SerializedName("passport_photo_file_url")
	val passportPhotoFileUrl: String? = null,

	@field:SerializedName("pension_life_certificate_file_url")
	val pensionLifeCertificateFileUrl: String? = null,

	@field:SerializedName("monthly_arrears_payment_appointment_file_url")
	val monthlyArrearsPaymentAppointmentFileUrl: String? = null,

	@field:SerializedName("application_form_file_url")
	val applicationFormFileUrl: String? = null,

	@field:SerializedName("id_card_file_url")
	val idCardFileUrl: String? = null,

	@field:SerializedName("promotion_notification_file_url")
	val promotionNotificationFileUrl: String? = null,

	@field:SerializedName("clearance_form_file_url")
	val clearanceFormFileUrl: String? = null
)



