package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ReservationResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("booking_data") var bookingData: BookingData? = null,
    )
    {
        data class BookingData(
            @SerializedName("booking_date") var bookingDate: String,
            @SerializedName("slot_start_time") var slotStartTime: String,
            @SerializedName("slot_end_time") var slotEndTime: String,
            @SerializedName("total_payable_amount") var totalPayableAmount: Int
        )
    }
}