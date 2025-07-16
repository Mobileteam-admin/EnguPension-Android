package com.enugu.pension.model.response

import com.google.gson.annotations.SerializedName

data class ProfileUpdateResponse(
    @SerializedName("detail") var detail: Detail? = Detail()
) {
    data class Detail(
        @SerializedName("status") var status: String? = null,
        @SerializedName("token_status") var tokenStatus: String? = null,
        @SerializedName("message") var message: String? = null,
        @SerializedName("updated_fields") var updatedFields: UpdatedFields? = null,
        @SerializedName("file_info") var fileInfo: FileInfo? = null,
    ) {
        data class UpdatedFields(
            @SerializedName("ein") var ein: String? = "",
            @SerializedName("employment_status") var employmentStatus: String? = "",
            @SerializedName("designation") var designation: String? = "",
            @SerializedName("profile_picture") var profilePicture: Boolean? = false,
        )

        data class FileInfo(
            @SerializedName("file_url") val fileUrl: String? = null,
            @SerializedName("file_name") val fileName: String? = "",
            @SerializedName("file_size") val fileSize: Long? = null,
            @SerializedName("file_type") val fileType: String? = "",
        )
    }
}