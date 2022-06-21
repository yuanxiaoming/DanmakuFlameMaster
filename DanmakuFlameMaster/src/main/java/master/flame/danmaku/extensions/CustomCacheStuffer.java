package master.flame.danmaku.extensions;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import java.lang.ref.WeakReference;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.AndroidDisplayer;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;

public class CustomCacheStuffer extends SpannedCacheStuffer {
    private Context mContext;
    private WeakReference<IDanmakuView> mDanmakuView;

    public CustomCacheStuffer(Context context, IDanmakuView danmakuView) {
        this.mContext = context;
        this.mDanmakuView = new WeakReference<>(danmakuView);

    }

    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
        if (danmaku != null && danmaku.tag instanceof ICustomPower) {
            ICustomPower customPower = (ICustomPower) danmaku.tag;
            if (customPower.getBitmap() == null) {
                customPower.createPowerToShell(mContext);
            }
            danmaku.paintHeight = customPower.getBitmap().getHeight();
            danmaku.paintWidth = customPower.getBitmap().getWidth();
            //DanmakuLoggers.i(TAG, "measure " + danmaku.text)
        } else {
            super.measure(danmaku, paint, fromWorkerThread);
        }
    }

    @Override
    public void drawDanmaku(final BaseDanmaku danmaku, Canvas canvas, float left, float top,
                            boolean fromWorkerThread,
                            AndroidDisplayer.DisplayerConfig displayerConfig) {
        if (danmaku != null && danmaku.tag instanceof ICustomPower) {
            ICustomPower customPower = (ICustomPower) danmaku.tag;
            if (customPower.getBitmap() == null) {
                customPower.createPowerToShell(mContext);
            }
            Rect rect = new Rect(0, 0, customPower.getBitmap().getWidth(),
                    customPower.getBitmap().getHeight());
            canvas.drawBitmap(customPower.getBitmap(), rect, rect,
                    new Paint(Paint.ANTI_ALIAS_FLAG));

            if (customPower.needInvalidate() &&
                    customPower.getICustomInvalidateCallBack() == null) {
                customPower.setICustomInvalidateCallBack(new ICustomInvalidateDanmaku() {
                    @Override
                    public void invalidateDanmaku(ICustomPower item, boolean remeasure) {
                        if (mDanmakuView != null && mDanmakuView.get() != null) {
                            mDanmakuView.get().invalidateDanmaku(danmaku, remeasure);
                        }
                    }
                });
            }

        } else {
            super.drawDanmaku(danmaku, canvas, left, top, fromWorkerThread, displayerConfig);
        }
    }
}
