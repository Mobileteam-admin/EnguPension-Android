package com.enugu.pension.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class NextOfKinRequest(
    @field:SerializedName("next_of_kin_name") val nextOfKinName: String,
    @field:SerializedName("next_of_kin_email") val nextOfKinEmail: String,
    @field:SerializedName("next_of_kin_phone_number") val nextOfKinPhoneNumber: String,
    @field:SerializedName("next_of_kin_address") val nextOfKinAddress: String,
    @field:SerializedName("next_of_kin_pincode") val nextOfKinPinCode: String,
)
