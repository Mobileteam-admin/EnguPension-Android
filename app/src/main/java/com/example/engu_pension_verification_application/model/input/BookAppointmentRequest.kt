package com.example.engu_pension_verification_application.model.input

import com.google.gson.annotations.SerializedName

data class BookAppointmentRequest(
    @field:SerializedName("booking_date")
    val bookingDate: String,
    @field:SerializedName("booking_slot_id")
    val bookingSlotId: Int,
)