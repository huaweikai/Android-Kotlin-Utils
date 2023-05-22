@file:Suppress("DEPRECATION")

package com.huaweikai.androidkotlinutils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import java.util.Locale

//将dp值转为px值
inline val <reified T: Number> T.px: T
    get() {
        val result = (this.toFloat() * Resources.getSystem().displayMetrics.density) + 0.5f
        return when (T::class) {
            Float::class -> result as T
            Int::class -> result.toInt() as T
            Double::class -> result.toDouble() as T
            else -> throw RuntimeException("Type not Support")
        }
    }

val Context.configLocale: Locale
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else resources.configuration.locale
    }