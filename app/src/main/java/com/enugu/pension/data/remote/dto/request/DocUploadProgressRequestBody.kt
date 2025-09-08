package com.enugu.pension.data.remote.dto.request

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File


class DocUploadProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val listener: DocUploadProgressListener
) : RequestBody() {
    interface DocUploadProgressListener {
        fun onProgressUpdate(percentage: Int)
    }

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val fileSize = contentLength()
        val source = file.source()
        val buffer = okio.Buffer()
        var uploaded = 0L

        while (true) {
            val read = source.read(buffer, DEFAULT_BUFFER_SIZE.toLong())
            if (read == -1L) break
            sink.write(buffer, read)
            uploaded += read
            val progress = (100 * uploaded / fileSize).toInt()
            listener.onProgressUpdate(progress)
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048L
    }
}
