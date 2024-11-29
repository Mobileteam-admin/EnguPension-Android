package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class BookingSlotResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("slots") var slots: ArrayList<Slots> = arrayListOf()

    ) {
        data class Slots(
            @SerializedName("id") var id: Int? = null,
            @SerializedName("start_time") var startTime: String? = null,
            @SerializedName("end_time") var endTime: String? = null

        )
    }
}
