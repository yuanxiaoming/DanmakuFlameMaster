package master.flame.danmaku.extensions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer

class CustomCacheStuffer(val context: Context, val danmakuView: IDanmakuView?) : SpannedCacheStuffer
() {

    //private val TAG = "CustomCacheStuffer"

    override fun measure(danmaku: BaseDanmaku?, paint: TextPaint?, fromWorkerThread: Boolean) {
        val customPower = danmaku?.tag as? ICustomPower
        if (customPower != null) {
            if (customPower.bitmap == null) {
                customPower.createPowerToShell(context)
            }
            danmaku.paintWidth = customPower.bitmap.width.toFloat()
            danmaku.paintHeight = customPower.bitmap.height.toFloat()
            //DanmakuLoggers.i(TAG, "measure " + danmaku.text)
        } else {
            super.measure(danmaku, paint, fromWorkerThread);
        }
    }

    override fun drawDanmaku(
        danmaku: BaseDanmaku?, canvas: Canvas?, left: Float, top: Float, fromWorkerThread: Boolean,
        displayerConfig: AndroidDisplayer.DisplayerConfig?
    ) {
        val customPower = danmaku?.tag as? ICustomPower
        if (customPower != null) {
            if (customPower.bitmap == null) {
                customPower.createPowerToShell(context)
            }
            val rect = Rect(0, 0, customPower.bitmap.width,
                customPower.bitmap.height)
            canvas?.drawBitmap(customPower.bitmap, rect, rect, Paint(Paint.ANTI_ALIAS_FLAG))
            if (customPower.needInvalidate() && customPower.iCustomInvalidateCallBack == null) {
                customPower.iCustomInvalidateCallBack = ICustomInvalidateDanmaku { _, remeasure ->
                    danmakuView?.invalidateDanmaku(danmaku, remeasure)

                }
            }
            //DanmakuLoggers.i(TAG, "drawDanmaku " + danmaku.text)
        } else {
            super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig)
        }
    }
}