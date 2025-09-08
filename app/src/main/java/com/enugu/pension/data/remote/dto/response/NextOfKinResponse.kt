package com.enugu.pension.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NextOfKinResponse(
    @field:SerializedName("detail") val detail: Detail? = null,
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("next_of_kin_details") var nextOfKinDetails: NextOfKinDetails = NextOfKinDetails(),
    ) {
        data class NextOfKinDetails(
            @SerializedName("next_of_kin_name") var nextOfKinName: String = "",
            @SerializedName("next_of_kin_email") var nextOfKinEmail: String = "",
            @SerializedName("next_of_kin_phone_number") var nextOfKinPhoneNumber: String = "",
            @SerializedName("next_of_kin_address") var nextOfKinAddress: String = "",
            @SerializedName("next_of_kin_pincode") var nextOfKinPinCode: String = "",
        )
    }
}