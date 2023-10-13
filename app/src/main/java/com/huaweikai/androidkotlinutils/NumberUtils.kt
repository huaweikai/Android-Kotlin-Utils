package com.huaweikai.androidkotlinutils

import java.util.Locale

/**
 * 为数字优化小数点后的位数
 */
private fun Number.reservePoint(index: Int): String {
    // 不输入locale，就会按照系统的，导致异常，统一为US，使用阿拉伯数字
    return "%.${index}f".format(Locale.US, this)
}