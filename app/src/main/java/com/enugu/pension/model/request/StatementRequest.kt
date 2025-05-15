package com.enugu.pension.model.request

import com.google.gson.annotations.SerializedName

data class StatementRequest(
    @field:SerializedName("start_date") val startDate: String,
    @field:SerializedName("end_date") val endDate: String,
)