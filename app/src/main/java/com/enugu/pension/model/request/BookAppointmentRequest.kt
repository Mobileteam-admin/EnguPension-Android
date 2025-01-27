package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class BookAppointmentRequest(
    @field:SerializedName("booking_date")
    val bookingDate: String,
    @field:SerializedName("booking_slot_id")
    val bookingSlotId: Int,
)