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
            @SerializedName("current_month_transaction_status") var currentMonthStatus: String? = null,
            @SerializedName("verification_record") var verificationRecord: String? = null,
            @SerializedName("next_of_kin") var nextOfKin: NextOfKin = NextOfKin(),
            @SerializedName("gratuity") var gratuity: List<Gratuity> = emptyList()
        ) {
            data class TransactionHistory(
                @SerializedName("amount") var amount: Float,
                @SerializedName("date") var date: String,
                @SerializedName("description") var description: String? = "",
                @SerializedName("type") var type: String? = "",
                @SerializedName("status") var status: String? = "",
                @SerializedName("stripe_transaction_id") var stripeTransactionId: String? = null,
            )
            data class NextOfKin(
                @SerializedName("next_of_kin_name") var name: String? = null,
                @SerializedName("next_of_kin_email") var email: String? = null,
                @SerializedName("next_of_kin_address") var address: String? = null,
                @SerializedName("next_of_kin_phone_number") var phoneNumber: String? = null,
                @SerializedName("next_of_kin_pincode") var pinCode: String? = null,
            )
            data class Gratuity(
                @SerializedName("id") var id: Int? = null,
                @SerializedName("amount") var amount: Float? = null,
                @SerializedName("payment_date") var paymentDate: String? = null,
                @SerializedName("payment_status") var paymentStatus: String? = null,
                @SerializedName("description") var description: String? = null,
            )
        }
    }
}
