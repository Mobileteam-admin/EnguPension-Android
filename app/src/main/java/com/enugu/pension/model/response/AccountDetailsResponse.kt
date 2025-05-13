package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class AccountDetailsResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("account_data") var accountData: AccountData = AccountData(),
    ) {
        data class AccountData(
            @SerializedName("current_balance") var currentBalance: Float = 0f,
            @SerializedName("transaction_history") var transactionHistory: List<TransactionHistory> = emptyList(),
        ) {
            data class TransactionHistory(
                @SerializedName("amount") var amount: Float,
                @SerializedName("date") var date: String,
                @SerializedName("description") var description: String? = "",
                @SerializedName("type") var type: String? = "",
                @SerializedName("status") var status: String? = "",
                @SerializedName("stripe_transaction_id") var stripeTransactionId: Int? = null,
            )
        }
    }
}
