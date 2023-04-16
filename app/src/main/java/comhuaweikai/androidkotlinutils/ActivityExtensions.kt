package comhuaweikai.androidkotlinutils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.palette.graphics.Palette


fun Activity.setLightStatusBar(isLightBar: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            if (isLightBar) {
                it.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                it.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    }
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val decorView = window.decorView
        val systemUiVisibility = decorView.systemUiVisibility
        if (isLightBar) {
            decorView.systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

/**
 * 自动截图，然后解析状态栏是否亮色
 */
fun AppCompatActivity.fixStatusBarColor() {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        screenShotWithO()
    } else {
        screenShotMinO()
    }
    Palette.from(bitmap)
        .setRegion(
            0,
            0,
            resources.configuration.screenWidthDp.px,
            statusHeight
        )
        .generate {
            it?.let { palette ->
                var mostPopularSwatch: Palette.Swatch? = null
                for (swatch in palette.swatches) {
                    if (mostPopularSwatch == null || swatch.population > mostPopularSwatch.population) {
                        mostPopularSwatch = swatch
                    }
                }
                mostPopularSwatch?.let { swatch ->
                    val luminance = androidx.core.graphics.ColorUtils.calculateLuminance(swatch.rgb)
                    setLightStatusBar(luminance > 0.5)
                }
            }
            bitmap.recycle()
        }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun AppCompatActivity.screenShotWithO(): Bitmap {
    val view = window.decorView
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val rect = Rect(0, 0, view.height, view.bottom)
    PixelCopy.request(window,
        rect ,
        bitmap, {
            if (it == PixelCopy.SUCCESS) {
                Log.d("TAG", "fixStatusBarColor: 截图正确")
            }
        }, mainHandler
    )
    return bitmap
}

private fun AppCompatActivity.screenShotMinO(): Bitmap {
    return window.decorView.drawToBitmap()
}