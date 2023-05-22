@file:Suppress("unused")
package com.huaweikai.androidkotlinutils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException

/**
 * created by william
 * @Date Created in 2023/5/22
 */
/**
 * 将Uri直接转为RequestBody，用于okHttp组件上传文件
 */
fun Uri.asRequestBody(
    context: Context,
    length: Long = getFileLength(context),
    contentType: MediaType? = "multipart/form-data".toMediaType(),
): RequestBody {
    if (length < 0) {
        throw IOException("file is error")
    }
    return object : RequestBody() {
        override fun contentType() = contentType

        override fun contentLength() = length

        @SuppressLint("Recycle")
        override fun writeTo(sink: BufferedSink) {
            context.contentResolver.openInputStream(this@asRequestBody)?.source()?.use { source ->
                sink.writeAll(source)
            }
        }
    }
}

/**
 * 检测该路径是否应该上传，当为true时，需要上传
 */
val String.canUpload: Boolean
    get() {
        // 当是空的，肯定不用上传
        if (isBlank()) return false
        // 当使用File判断是本地文件，则需要上传
        if (File(this).exists()) return true
        val uri = Uri.parse(this)
        // 当其host为空时，即为相对路径，上传也会有问题，不上传
        if (uri.host == null) return false
        // 当其scheme为content时，即为本地文件，需要上传
        return uri.scheme == ContentResolver.SCHEME_CONTENT
    }