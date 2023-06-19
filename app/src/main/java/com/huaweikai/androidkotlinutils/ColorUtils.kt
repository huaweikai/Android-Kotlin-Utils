package com.huaweikai.androidkotlinutils

import android.graphics.Color
import androidx.annotation.FloatRange
import androidx.annotation.IntRange


/**
 * 为颜色设置透明度
 * @param alpha 输入0到1的透明度，1为不透明
 */
fun Int.setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
    return setAlpha((255 * alpha).toInt())
}

/**
 * 为颜色设置透明度
 * @param alpha 输入0到255的透明度
 */
fun Int.setAlpha(@IntRange(from = 0, to = 255) alpha: Int): Int {
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}