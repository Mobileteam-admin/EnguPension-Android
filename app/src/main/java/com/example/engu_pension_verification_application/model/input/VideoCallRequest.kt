package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class VideoCallRequest(
    @field:SerializedName("govt_official_email") val govtOfficialEmail: String,
    @field:SerializedName("user_email") val userEmail: String,
    @field:SerializedName("call_day") val callDay: String,
    @field:SerializedName("slot_id") val slotId: Int,
)