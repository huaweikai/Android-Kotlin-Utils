package com.huaweikai.androidkotlinutils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

/**
 * created by william
 * @Date Created in 2023/5/22
 */
/**
 * 获取文件长度
 */
fun Uri.getFileLength(context: Context): Long {
    return getFileInfo(context).second
}

/**
 * 获取文件名字和长度信息
 * @return 返回的是Pair first为文件名字，second为文件长度
 */
fun Uri.getFileInfo(context: Context): Pair<String, Long> {
    return when (this.scheme) {
        ContentResolver.SCHEME_FILE -> {
            val file = this.path?.let { File(it) } ?: return EMPTY_FILE_INFO
            Pair(file.name, file.length())
        }

        ContentResolver.SCHEME_CONTENT -> {
            var fileName = ""
            var length: Long
            context.contentResolver.query(this, null, null, null, null).use { cursor ->
                length = if (cursor != null && cursor.moveToFirst()) {
                    val lengthIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    val fileNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (fileNameIndex >= 0) {
                        fileName = cursor.getString(fileNameIndex)
                    }
                    if (lengthIndex >= 0) {
                        cursor.getLong(lengthIndex)
                    } else 0L
                } else {
                    0L
                }
            }
            return Pair(fileName, length)
        }

        else -> EMPTY_FILE_INFO
    }
}

private val EMPTY_FILE_INFO = Pair("", -1L)