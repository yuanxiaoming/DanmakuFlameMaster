package master.flame.danmaku.danmaku.model.android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.ICustomPower

class CustomCacheStuffer(val context: Context) : SpannedCacheStuffer() {

    //private val TAG = "CustomCacheStuffer"

    override fun measure(danmaku: BaseDanmaku?, paint: TextPaint?, fromWorkerThread: Boolean) {
        val customPower = danmaku?.tag as? ICustomPower
        if (customPower != null) {
            if (customPower.bitmap == null) {
                customPower.createPowerToShell(context)
            }
            danmaku.paintWidth = customPower.bitmap.width.toFloat()
            danmaku.paintHeight = customPower.bitmap.height.toFloat()
            //MLog.info(TAG, "measure " + danmaku.text)
        } else {
            super.measure(danmaku, paint, fromWorkerThread);
        }
    }

    override fun drawDanmaku(
        danmaku: BaseDanmaku?, canvas: Canvas?, left: Float, top: Float, fromWorkerThread: Boolean,
        displayerConfig: AndroidDisplayer.DisplayerConfig?
    ) {
        val gunNewPower = danmaku?.tag as? ICustomPower
        if (gunNewPower != null) {
            if (gunNewPower.bitmap == null) {
                gunNewPower.createPowerToShell(context)
            }
            val rect = Rect(0, 0, gunNewPower.bitmap.width,
                gunNewPower.bitmap.height)
            canvas?.drawBitmap(gunNewPower.bitmap, rect, rect, Paint(Paint.ANTI_ALIAS_FLAG))
            //MLog.info(TAG, "drawDanmaku " + danmaku.text)
        } else {
            super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig)
        }
    }
}