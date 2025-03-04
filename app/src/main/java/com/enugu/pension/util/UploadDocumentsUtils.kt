package com.enugu.pension.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView

object UploadDocumentsUtils {
    fun getMimeType(filePath: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        val extension2 = filePath.substringAfterLast('.', "")
        var mimeTypeSecondary = ""
        when (extension2) {
            "jpg", "jpeg" -> mimeTypeSecondary = "image/jpeg"
            "png" -> mimeTypeSecondary = "image/png"
            "pdf" -> mimeTypeSecondary = "application/pdf"
        }
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: mimeTypeSecondary
        return mimeType
    }
    fun getFileName(contentResolver:ContentResolver?,selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = contentResolver?.query(selectedImageUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

}

