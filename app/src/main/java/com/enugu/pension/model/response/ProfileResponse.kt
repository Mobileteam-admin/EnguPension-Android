package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var data: Data? = null,
    ) {
        data class Data(
            @SerializedName("ein") var ein: String? = "",
            @SerializedName("employment_status") var employmentStatus: String? = "",
            @SerializedName("designation") var designation: String? = "",
            @SerializedName("profile_picture") var profilePicture: ProfilePicture? = null,

        ) {
            data class ProfilePicture(
                @SerializedName("file_url") val fileUrl: String? = null,
                @SerializedName("file_name") val fileName: String? = "",
                @SerializedName("file_size") val fileSize: Long? = null,
                @SerializedName("file_type") val fileType: String? = "",
            )
        }
    }
}
