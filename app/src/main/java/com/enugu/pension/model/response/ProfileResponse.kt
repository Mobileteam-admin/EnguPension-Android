package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("user_profile_details") var userProfileDetails: UserProfileDetails? = null,
    ) {
        data class UserProfileDetails(
            @SerializedName("EIN") var ein: String? = "",
            @SerializedName("employment_status") var employmentStatus: String? = "",
            @SerializedName("designation") var designation: String? = "",
            @SerializedName("state") var state: String? = "",
            @SerializedName("region") var region: String? = "",
            @SerializedName("department") var department: String? = "",
            @SerializedName("status") var status: String? = "",
            @SerializedName("duration") var duration: String? = "",
            @SerializedName("file_url") var imageUrl: String? = null,
        )
    }
}