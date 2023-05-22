@file:Suppress("unused")

package com.huaweikai.androidkotlinutils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.text.NoCopySpan
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.view.drawToBitmap
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * created by william
 * @Date Created in 2023/5/22
 */
inline fun SeekBar.setOnSeekBarChangeListener(
    crossinline onStartTrackingTouch: (seekBar: SeekBar?) -> Unit = { _ -> },
    crossinline onStopTrackingTouch: (seekBar: SeekBar?) -> Unit = { _ -> },
    crossinline onProgressChanged: (seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit
): SeekBar.OnSeekBarChangeListener {
    val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged.invoke(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onStartTrackingTouch.invoke(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onStopTrackingTouch.invoke(seekBar)
        }
    }
    setOnSeekBarChangeListener(seekBarChangeListener)
    return seekBarChangeListener
}

/**
 * 不适用一串文字中有多处点击
 * @param unText 不被点击的文字
 * @param clickText 被点击的文字
 * @param color 设置文本可点击的前景颜色，默认蓝色
 * @param overText 被点击后剩余的文字
 * @param click 点击事件回调
 */
inline fun TextView.setClickText(
    unText: String,
    clickText: String,
    color: Int = Color.BLUE,
    overText: String = "",
    crossinline click: (View) -> Unit
) {
    val annotatedText = buildSpannedString {
        append(unText, clickText)
        // 继承NocopySpan，防止在onSaveState时保存另一份实例，导致内存泄露
        val clickableSpan = object : ClickableSpan(), NoCopySpan {
            override fun onClick(widget: View) {
                click(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        val foreColor = ForegroundColorSpan(color)
        setSpan(clickableSpan, unText.length, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(foreColor, unText.length, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (overText.isNotBlank()) {
            append(overText)
        }
    }
    movementMethod = LinkMovementMethod.getInstance()

    text = annotatedText
}

/**
 * 用于设置文字后，自动将光标移至最后
 */
fun EditText.setTextWithEnd(text: CharSequence) {
    setText(text)
    setSelection(text.length)
}


/**
 * 没有使用invalidate(),不能在设置后自动刷新,所以需要放在viewRootImpl初始化前
 * 通过设置paint属性来达到字体粗细
 * @param needRefresh 是否需要设置后刷新
 */
fun TextView.toBold(weight: Float = 2f, needRefresh: Boolean = false) {
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.strokeWidth = weight
    if (needRefresh) invalidate()
}


/**
 * 用于各种view进行检查白屏，但是最好不要使用activity的decorView,android8以上会drawToBitmap失败
 */
fun View.checkOnSubThread(
    callBack: ((Float) -> Unit)? = null
) {
    findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
        var whitePixelCount = 0
        synchronized(this::class.java) {
            if (callBack == null) return@launch
            val bitmap: Bitmap = try {
                this@checkOnSubThread.drawToBitmap(Bitmap.Config.ARGB_8888)
            } catch (e: Exception) {
                null
            } ?: return@launch
            val width = bitmap.width
            val height = bitmap.height / 2
            // 改为切上方，因为blog的左下方会有一大片空白，services页面会有右边留白，切一半保险
            val cutBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, width, height)
            bitmap.recycle()
            for (i in 0 until cutBitmap.width) {
                for (j in 0 until cutBitmap.height) {
                    if (cutBitmap.getPixel(i, j) == -1) {
                        whitePixelCount++
                    }
                }
            }
            if (whitePixelCount > 0) {
                val rate = whitePixelCount * 100f / width / height
                Log.d("TAG", "checkOnSubThread: $rate")
                runOnUI { callBack.invoke(rate) }
            }
            cutBitmap.recycle()
        }
    }
}