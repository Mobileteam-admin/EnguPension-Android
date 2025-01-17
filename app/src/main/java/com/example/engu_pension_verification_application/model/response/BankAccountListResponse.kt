package com.example.engu_pension_verification_application.model.response

import com.google.gson.annotations.SerializedName

data class BankAccountListResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("bank_accounts") var bankAccounts: ArrayList<BankAccount> = arrayListOf()
    ) {
        data class BankAccount(
            @SerializedName("id") var id: Int? = null,
            @SerializedName("user_id") var userId: Int? = null,
            @SerializedName("bank_id") var bankId: Int? = null,
            @SerializedName("bank_name") var bankName: String? = null,
            @SerializedName("account_holder_name") var accountHolderName: String? = null,
            @SerializedName("account_number") var accountNumber: String? = null,
            @SerializedName("swift_code") var swiftCode: String? = null,
            @SerializedName("bank_code") var bankCode: String? = null,
            @SerializedName("account_type") var accountType: String? = null,
            @SerializedName("auto_renewal") var autoRenewal: Boolean? = null,
            @SerializedName("is_primary") var isPrimary: Boolean? = null,
            @SerializedName("logo_url") var logoUrl: String? = null
        )
    }
}