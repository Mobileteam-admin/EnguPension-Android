package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class VerificationHistoryResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("verification_history") var verificationHistory: List<VerificationHistory> = emptyList(),
    ) {
        data class VerificationHistory(
            @SerializedName("booking_id") var bookingId: Int? = null,
            @SerializedName("government_official_id") var governmentOfficialId: Int? = null,
            @SerializedName("verified_at") var verifiedAt: String? = null,
            @SerializedName("verification_status") var verificationStatus: String? = null,
            @SerializedName("verification_method") var verificationMethod: String? = null,
        )
    }
}
