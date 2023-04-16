package comhuaweikai.androidkotlinutils

import android.content.res.Resources

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