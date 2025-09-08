package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class StatementResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("pension_data") var pensionData: PensionData = PensionData(),
    ) {
        data class PensionData(
            @SerializedName("salary_received") val salaryReceived: List<SalaryReceived> = emptyList(),
            @SerializedName("pension_withdrawn") val pensionWithdrawn: List<PensionWithdrawn> = emptyList(),
        ) {
            data class SalaryReceived(
                @SerializedName("id") val id: Int,
                @SerializedName("amount") val amount: Double,
                @SerializedName("payment_date") val paymentDate: String,
            )
            data class PensionWithdrawn(
                @SerializedName("id") val id: Int,
                @SerializedName("amount") val amount: Double,
                @SerializedName("transaction_date") val transactionDate: String,
                @SerializedName("description") val description: String? = "",
            )
        }
    }
}