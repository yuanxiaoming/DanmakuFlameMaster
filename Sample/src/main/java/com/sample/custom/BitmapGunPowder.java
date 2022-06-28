package com.sample.custom;

/**
 * Created by Administrator on 2016/10/31.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.sample.span.SpanTextView;
import com.sample.span.UrlImageSpan;
import com.sample.span.UrlImageSpanCallBack;

import java.lang.ref.WeakReference;

import master.flame.danmaku.danmaku.util.DanmakuLoggers;
import master.flame.danmaku.extensions.ICustomInvalidateDanmaku;
import master.flame.danmaku.extensions.ICustomPower;

public class BitmapGunPowder implements ICustomPower {
    private static final String TAG = "BitmapGunPowder";
    private static final int itemHeight = 28;
    protected Bitmap mBitmap;
    protected Spannable span;
    protected final Matrix mShaderMatrix = new Matrix();
    private WeakReference<Drawable> mMessageBg = null;
    private TextView mTextView;
    public Drawable bgDrawable;
    public int mItemHeight = itemHeight;
    public int backgroundColor = 0; // 背景配置
    public String content;
    public long senderUid;
    protected ICustomInvalidateDanmaku mICustomInvalidateDanmaku;

    protected Drawable.Callback drawableCB = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            invalidatePowerToShell();
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {

        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {

        }
    };

    public BitmapGunPowder(int backgroundColor, String content, TextView tv, long senderUid) {
        this.content = content;
        this.senderUid = senderUid;
        this.backgroundColor = backgroundColor;
        this.mTextView = tv;
    }

    protected void invalidatePowerToShell() {
        try {
            if (getBitmap() == null || getBitmap().isRecycled() || mTextView == null) {
                return;
            }
            //OpenGL 渲染线程会取 bitmap
            //这里进行线程同步，防止bitmap修改到一半 就被取了
            synchronized (getBitmap()) {
                DanmakuLoggers.i(TAG, "invalidateDrawable");
                Rect rect = new Rect(0, 0, getBitmap().getWidth(), getBitmap().getHeight());
                Bitmap temp = viewToBitmapForSurfaceView(mTextView, backgroundColor);
                Canvas canvas = new Canvas(getBitmap());
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(temp, rect, rect, mTextView.getPaint());
                temp.recycle();
                if (mICustomInvalidateDanmaku != null) {
                    mICustomInvalidateDanmaku.invalidateDanmaku(this, true);
                }
            }
        } catch (Throwable e) {
            DanmakuLoggers.e(TAG, "invalidateDrawable error", e);
        }

    }

    public void createPowerToShell(Context context) {
        createPowerToShell(context, true);
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void setICustomInvalidateCallBack(ICustomInvalidateDanmaku iCustomInvalidateDanmaku) {
        mICustomInvalidateDanmaku = iCustomInvalidateDanmaku;
    }

    @Override
    public ICustomInvalidateDanmaku getICustomInvalidateCallBack() {
        return mICustomInvalidateDanmaku;
    }

    @Override
    public boolean needInvalidate() {
        return true;
    }

    @Override
    public void clear() {
        onDetach();
    }

    public void createPowerToShell(Context context, boolean isSurfaceView) {
        if (context == null || span == null) {
            return;
        }
        try {
            if (mTextView == null) {
                mTextView = new BitmapSpanTextView(context, true);
            }
            mTextView.setText(span);
            mTextView.setGravity(Gravity.BOTTOM);
            // mTextView.setTextSize(mTextSize);
            mTextView.setSingleLine(true);
            mTextView.setGravity(Gravity.CENTER_VERTICAL);
            if (bgDrawable != null) {
                setMessageBg(context, mTextView, bgDrawable);
            } else {
                setMessageBg(context, mTextView);
            }
            //判断span中是否存在gif
            ImageSpan[] imageSpans = span.getSpans(0, span.length(), ImageSpan.class);
            for (ImageSpan span : imageSpans) {
                if (span.getDrawable() instanceof GifDrawable) {
                    final GifDrawable gif = (GifDrawable) span.getDrawable();
                    gif.setLoopCount(10); //这里大概设数值 10次播放完弹幕应该已经滚到屏幕外了
                    gif.setCallback(drawableCB);
                    if (!gif.isRunning()) {
                        ScheduledTask.getInstance().scheduledMain(new Runnable() {
                            @Override
                            public void run() {
                                gif.start();
                            }
                        });
                    }
                } else if (span instanceof UrlImageSpan) {
                    UrlImageSpan urlImageSpan = (UrlImageSpan) span;
                    urlImageSpan.setUrlImageSpanCallBack(new UrlImageSpanCallBack() {
                        @Override
                        public boolean onReady(Drawable drawable) {
                            if (drawableCB != null) {
                                drawableCB.invalidateDrawable(drawable);
                                return true;
                            }
                            return false;
                        }
                    });
                    urlImageSpan.onAttach(mTextView);
                }
            }
            mBitmap = viewToBitmapForSurfaceView(mTextView, backgroundColor);
        } catch (Throwable e) {
            DanmakuLoggers.e(TAG, "createPowerToShell error", e);
        }
    }

    //无背景样式的弹幕
    private void setMessageBg(Context context, TextView tv) {
        if (context == null || tv == null) {
            return;
        }
        tv.setPadding(0, 0, 0, 0);
        tv.setBackground(null);
    }

    /**
     * 设置弹幕背景样式
     *
     * @param context    上下文参数
     * @param tv         文本
     * @param bgDrawable 背景样式 drawable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setMessageBg(Context context, TextView tv, Drawable bgDrawable) {
        if (context == null || tv == null) {
            return;
        }
        tv.setPadding(0, 0, 0, 0);
        tv.setBackground(null);

        mItemHeight = DimenConverter.dip2px(context, 32); //调大弹幕item的高度
        Drawable bg;
        tv.setPadding(DimenConverter.dip2px(context, 10),
                DimenConverter.dip2px(context, 2),
                DimenConverter.dip2px(context, 10),
                DimenConverter.dip2px(context, 2));
        if (mMessageBg == null || mMessageBg.get() == null) {
            bg = bgDrawable;
            mMessageBg = new WeakReference<Drawable>(bg);
        } else {
            bg = mMessageBg.get();
        }
        tv.setBackground(bg);
    }

    private Bitmap viewToBitmapForSurfaceView(TextView view, int color) {
        view.measure(0, View.MeasureSpec.EXACTLY +
                (int) DimenConverter.dip2px(view.getContext(), 28));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        int offsetLeft = (int) DimenConverter.dip2px(view.getContext(), 8);
        int textHeight = (int) DimenConverter.dip2px(view.getContext(), 19);
        int actualWidth = view.getMeasuredWidth() + 2 * offsetLeft;
        Bitmap bitmap = Bitmap.createBitmap(actualWidth, mItemHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        if (color != -1) {
            //利用bitmap生成画布
            RectF powerAvatarRect = new RectF();
            Rect powerAvatarRect1 = new Rect();
            float t = DimenConverter.dip2px(view.getContext(), 27) - textHeight;
            canvas.translate(0, t);
            float radius = (float) textHeight / 2;
            powerAvatarRect.set(0, 0, actualWidth, textHeight);
            powerAvatarRect1.set(0, 0, actualWidth, textHeight);
            Paint mAvatarBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mAvatarBorderPaint.setAntiAlias(true);
            mAvatarBorderPaint.setColor(color);
            BitmapShader shader =
                    new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            updateShaderMatrix(shader, bitmap.getWidth(), bitmap.getHeight(), powerAvatarRect1);
            canvas.drawRoundRect(powerAvatarRect, radius, radius, mAvatarBorderPaint);
            mAvatarBorderPaint.setShader(shader);
            // canvas.drawCircle(0, 0, textHeight / 2, mAvatarBorderPaint);
            canvas.save();
            canvas.translate(offsetLeft, -t - 2);
        }

        view.draw(canvas);
        return bitmap;
    }

    private static class BitmapSpanTextView extends SpanTextView {
        private TextPaint mTextPaint;
        private boolean mBDrawSideLine = true; // 默认采用描边

        private BitmapSpanTextView(Context context, boolean bdRawSideLine) {
            super(context);
            mTextPaint = this.getPaint();
            this.mBDrawSideLine = bdRawSideLine;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mBDrawSideLine) {
                // 弹幕字体颜色都是在 DanMuUtil 中的 spannable 已经设置好，这里不再设置字体颜色
                mTextPaint.setStrokeWidth(
                        getContext().getResources().getDisplayMetrics().density); // 描边宽度
                mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE); // 描边种类
                // 外层text采用粗体
                mTextPaint.setShadowLayer(0, 0, 0, 0); // 字体的阴影效果，可以忽略
                super.onDraw(canvas);
                // 描内层，恢复原先的画笔
                mTextPaint.setStrokeWidth(0);
                mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                setShadowLayer(getContext().getResources().getDisplayMetrics().density + 1,
                        getContext().getResources().getDisplayMetrics().density, 1F, Color.BLACK);

            }
            super.onDraw(canvas);
        }
    }

    public void onDetach() {
        if (span != null) {
            ImageSpan[] imageSpans = span.getSpans(0, span.length(), ImageSpan.class);
            for (ImageSpan imageSpan : imageSpans) {
                if (imageSpan instanceof UrlImageSpan) {
                    UrlImageSpan urlImageSpan = (UrlImageSpan) imageSpan;
                    urlImageSpan.onDetach();
                }
            }
        }
    }

    protected void updateShaderMatrix(BitmapShader bitmapShader, int bitmapWidth, int bitmapHeight,
                                      Rect drawableRect) {
        float scale;
        float dx = 0;
        float dy = 0;
        mShaderMatrix.set(null);
        if (bitmapWidth * drawableRect.height() > drawableRect.width() * bitmapHeight) {
            scale = drawableRect.height() / (float) bitmapHeight;
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = drawableRect.width() / (float) bitmapWidth;
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f;
        }
        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + 2, (int) (dy + 0.5f) + 2);
        bitmapShader.setLocalMatrix(mShaderMatrix);
    }
}