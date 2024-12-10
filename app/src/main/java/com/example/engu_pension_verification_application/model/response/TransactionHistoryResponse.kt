package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class TransactionHistoryResponse(
    @field:SerializedName("detail") val detail: Detail,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var data: Data? = null,
    ) {
        data class Data(
            @field:SerializedName("page") val page: Int,
            @field:SerializedName("limit") val limit: Int,
            @field:SerializedName("total_count") val totalCount: Int,
            @field:SerializedName("transactions") val transactions: List<Transaction>
        ) {
            data class Transaction(
                @field:SerializedName("id") val id: Int,
                @field:SerializedName("amount") val amount: Double,
                @field:SerializedName("transaction_date") val transactionDate: String,
                @field:SerializedName("description") val description: String,
                @field:SerializedName("stripe_transaction_id") val stripeTransactionId: String
            )
        }
    }
}