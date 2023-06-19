package com.huaweikai.androidkotlinutils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.huaweikai.androidkotlinutils.setAlpha
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 多边行战士
 */
class PolygonView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    /**
     * 可以定义是几边形
     */
    var parts = 5
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 定义分数，但是该列表长度不能超过parts
     */
    var fractions = List(parts) { Random.nextInt(0, 100) }
        set(value) {
            if (value.size > parts) {
                throw RuntimeException("分数太多了")
            }
            field = value
            invalidate()
        }

    /**
     * 线条的颜色
     */
    @ColorInt
    var lineColor: Int = Color.RED
        set(value) {
            field = value
            linePaint.color = value
            invalidate()
        }

    /**
     * 分数填充的颜色
     */
    @ColorInt
    var fractionColor: Int = Color.CYAN.setAlpha(0.5f)
        set(value) {
            field = value
            fractionPaint.color = value
            invalidate()
        }

    private val linePaint = Paint().apply {
        color = lineColor
        style = Paint.Style.FILL
        strokeWidth = 1f
    }

    private val fractionPaint = Paint().apply {
        color = fractionColor
        style = Paint.Style.FILL
    }

    /**
     * 半径，设置为view宽度的1/3
     */
    private val radius: Float
        get() = width / 3f

    /**
     * 圆点的中心X的位置
     */
    private val circleX: Float
        get() = width / 2f

    /**
     * 圆点的中心Y的位置
     */
    private val circleY: Float
        get() = height / 2f

    /**
     * 数字的扩展函数，返回对应的度数
     */
    private val Number.du: Double
        get() {
            val onePi = Math.PI / 180
            return this.toDouble() * onePi
        }

    /**
     * 获取对应下标存在的位置
     * @param multiple 倍数，对应的想要几倍的坐标，用来做几层的嵌套
     * @return 返回的是Pair，first为X轴坐标，second为Y轴坐标
     */
    private fun Int.getPointXY(multiple: Float): Pair<Float, Float> {
        val angle = ((360 / parts) * this).du
        val thisRadius = radius * multiple
        val cX = if (angle in 90f..270f) -thisRadius * cos(angle) else thisRadius * cos(angle)
        val x = (circleX + cX).toFloat()
        val cY = if (angle in 180f..360f) thisRadius * sin(angle) else -thisRadius * sin(angle)
        val y = (circleY + cY).toFloat()
        return Pair(x, y)
    }

    private fun getPointList(multiple: Float): List<Pair<Float, Float>> {
        val array = mutableListOf<Pair<Float, Float>>()
        repeat(parts) {
            array.add(it.getPointXY(multiple))
        }
        return array
    }

    private fun drawLine(canvas: Canvas, pointList: List<Pair<Float, Float>>) {
        pointList.forEachIndexed { index, pair ->
            val nextIndex = if (index == parts - 1) 0 else index + 1
            val nextPair = pointList[nextIndex]
            canvas.drawLine(
                pair.first,
                pair.second,
                nextPair.first,
                nextPair.second,
                linePaint
            )
        }
    }

    private fun drawFraction(canvas: Canvas) {
        val pointList = mutableListOf<Pair<Float, Float>>()
        fractions.forEachIndexed { index, i ->
            val ratio = i / 100f
            pointList.add(index.getPointXY(ratio))
        }
        val path = Path()
        // 交合区域填充颜色
        path.fillType = Path.FillType.WINDING
        path.moveTo(pointList[0].first, pointList[0].second)
        pointList.forEachIndexed { index, _ ->
            if (index == parts - 1) return@forEachIndexed
            val nextIndex = if (index == parts - 1) 0 else index + 1
            val nextPair = pointList[nextIndex]
            path.lineTo(nextPair.first, nextPair.second)
        }
        path.close()
        canvas.drawPath(path, fractionPaint)
    }

    /**
     * 将图形分割
     */
    private fun drawLineToClip(canvas: Canvas) {
        val pointList = getPointList(1f)
        pointList.forEach {
            canvas.drawLine(circleX, circleY, it.first, it.second, linePaint)
        }
    }


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.run {
            // 将画布旋转90度
            rotate(-90f, circleX, circleY)
            // 多画几个线，界面层次分明
            drawLine(this, getPointList(1f))
            drawLine(this, getPointList(0.8f))
            drawLine(this, getPointList(0.6f))
            drawLine(this, getPointList(0.4f))
            drawLine(this, getPointList(0.2f))
            drawLineToClip(this)
            drawFraction(this)
        }
    }

}