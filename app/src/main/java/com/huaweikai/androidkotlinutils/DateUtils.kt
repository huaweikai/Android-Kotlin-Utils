@file:Suppress("unused")
package com.huaweikai.androidkotlinutils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * created by william
 * @Date Created in 2023/4/19
 */
/**
 * 生成对应的dateFormat，如 yyyy/MM/dd 等
 * 默认使用手机所带的时区
 */
fun String.dateFormat(context: Context): SimpleDateFormat = SimpleDateFormat(this, context.configLocale)

fun String.getDateFormat(locale: Locale? = null, context: Context): SimpleDateFormat {
    if (locale == null) return dateFormat(context)
    return SimpleDateFormat(this, locale)
}

var Calendar.year
    get() = get(Calendar.YEAR)
    set(value) = set(Calendar.YEAR, value)

var Calendar.month
    get() = get(Calendar.MONTH)
    set(value) = set(Calendar.MONTH, value)

var Calendar.dayOfMonth
    get() = get(Calendar.DAY_OF_MONTH)
    set(value) = set(Calendar.DAY_OF_MONTH, value)

var Calendar.hourOfDay
    get() = get(Calendar.HOUR_OF_DAY)
    set(value) = set(Calendar.HOUR_OF_DAY, value)

var Calendar.minute
    get() = get(Calendar.MINUTE)
    set(value) = set(Calendar.MINUTE, value)