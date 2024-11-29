package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class BookingDateRangeResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("booking_date_range") var bookingDateRange: ArrayList<BookingDateRange> = arrayListOf()

    ) {
        data class BookingDateRange(
            @SerializedName("month") var month: String? = null,
            @SerializedName("year") var year: String? = null,
            @SerializedName("start_day") var startDay: String? = null,
            @SerializedName("end_day") var endDay: String? = null,
            @SerializedName("holidays") var holidays: ArrayList<Holiday> = arrayListOf()
        ) {
            data class Holiday(
                @SerializedName("date") var date: String? = null,
                @SerializedName("name") var name: String? = null,
                @SerializedName("holiday_for_government_office") var holidayForGovernmentOffice: Boolean? = null,
            )
        }
    }
}
