package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class BookingSlotResponse(
    @SerializedName("detail") var detail: com.enugu.pension.data.remote.dto.response.BookingSlotResponse.Detail? = com.enugu.pension.data.remote.dto.response.BookingSlotResponse.Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("slots") var slots: ArrayList<com.enugu.pension.data.remote.dto.response.BookingSlotResponse.Detail.Slots> = arrayListOf()

    ) {
        data class Slots(
            @SerializedName("id") var id: Int? = null,
            @SerializedName("start_time") var startTime: String? = null,
            @SerializedName("end_time") var endTime: String? = null

        )
    }
}
