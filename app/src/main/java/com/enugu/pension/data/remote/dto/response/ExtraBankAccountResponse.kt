package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ExtraBankAccountResponse(
    @field:SerializedName("detail") val detail: Detail? = null
) {
    data class Detail(

        @field:SerializedName("status") val status: String? = null,

        @field:SerializedName("token_status") val tokenStatus: String? = null,

        @field:SerializedName("message") val message: String? = null,

        @field:SerializedName("bank_detail") val bankDetail: BankDetail? = null,
    ) {
        data class BankDetail(

            @field:SerializedName("id") val id: Int? = null,

            @field:SerializedName("userId") val userId: Int? = null,

            @field:SerializedName("bank_id") val bankId: Int? = null,

            @field:SerializedName("account_holder_name") val accountHolderName: String? = null,

            @field:SerializedName("account_number") val accountNumber: String? = null,

            @field:SerializedName("swift_code") val swiftCode: String? = null,

            @field:SerializedName("bank_code") val bankCode: String? = null,

            @field:SerializedName("account_type") val accountType: String? = null,

            @field:SerializedName("auto_renewal") val autoRenewal: Boolean? = null,

            @field:SerializedName("is_primary") val isPrimary: Boolean? = null,

            )
    }
}
