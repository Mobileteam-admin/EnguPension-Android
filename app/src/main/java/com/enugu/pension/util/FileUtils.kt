package com.enugu.pension.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException

object FileUtils {
    fun getDocumentMimeType(filePath: String): String {
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

    fun getFileName(contentResolver: ContentResolver?, selectedImageUri: Uri): String {
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

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val fileName = getFileName(context.contentResolver, uri) ?: return null
        val file = File(context.cacheDir, fileName)
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            }
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getFileSizeInMB(file: File) = file.length().toDouble() / (1024 * 1024)
}

