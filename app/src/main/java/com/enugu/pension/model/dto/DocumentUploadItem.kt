package com.enugu.pension.model.dto

import android.net.Uri
import java.io.File

data class DocumentUploadItem(
    var multipartFileName:String,
    var cacheFileName:String,
    var index:Int,
    var isMandatory:Boolean,
    var title: String,
    var isUploading:Boolean = false,
    var progress:Int = 0,
    var uploadedUrl: String? = null,
    var pickedUri: Uri? = null,
    var file: File? = null,
    var fileName:String = "",
    var mime:String? = null,
) {
    fun hasDocumentAdded() = !uploadedUrl.isNullOrEmpty() || pickedUri != null

}