package com.sample.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import master.flame.danmaku.danmaku.util.DanmakuLoggers;

/**
 * create by chenrenzhan
 * 自定义 ImageSpan
 * 可以设置图文居中对齐 {ALIGN_VERTICAL_CENTER}，默认居中对齐
 * {mMarginLeft} 设置左边距， {mMarginRight} 设置右边距， 默认为0
 * {mMargin} 设置左右边距相等，默认边距为0
 */
public class CustomImageSpan extends ImageSpan {
    public static final int ALIGN_VERTICAL_CENTER = 2; // SpannableString 图文居中对齐
    public float mMarginLeft = 0; // 左边距
    public float mMarginRight = 0; // 右边距
    public float mMargin = 0; // 左右边距

    protected int width = 0;
    protected int height = 0;

    public void setOriginSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setOriginSize(Drawable drawable) {
        if (drawable != null && drawable.getBounds() != null) {
            this.width = drawable.getBounds().width();
            this.height = drawable.getBounds().height();
        }
    }

    /**
     * 用于公屏放大获取原始size，乘以放大比例的，在公屏会用到的都要调用这个方法
     *
     * @return
     */
    public int[] getOriginSize() {
        if (width <= 0 || height <= 0) {
            if (getDrawable() != null && getDrawable().getBounds() != null) {
                width = getDrawable().getBounds().width();
                height = getDrawable().getBounds().height();
            }
        }
        return new int[]{width, height};
    }

    public CustomImageSpan(Drawable d, int verticalAlignment, float marginLeft, float marginRight) {
        super(d, verticalAlignment);
        mMarginLeft = marginLeft;
        mMarginRight = marginRight;
        setOriginSize(d);
    }

    public CustomImageSpan(Drawable d, int verticalAlignment, float margin) {
        super(d, verticalAlignment);
        mMargin = margin;
        setOriginSize(d);
    }

    public CustomImageSpan(Drawable d, float marginLeft, float marginRight) {
        super(d, ALIGN_VERTICAL_CENTER); // 默认居中
        mMarginLeft = marginLeft;
        mMarginRight = marginRight;
        setOriginSize(d);
    }

    public CustomImageSpan(Drawable d, float margin) {
        super(d, ALIGN_VERTICAL_CENTER); // 默认居中
        mMargin = margin;
        setOriginSize(d);
    }

    public CustomImageSpan(Context context, int resourceId) {
        super(context, resourceId, ALIGN_VERTICAL_CENTER);
        setOriginSize(getDrawable());
    }

    public CustomImageSpan(Drawable d) {
        super(d, ALIGN_VERTICAL_CENTER); // 默认居中
        setOriginSize(d);
    }

    public CustomImageSpan(Drawable d, String source) {
        super(d, source, ALIGN_VERTICAL_CENTER);
        setOriginSize(d);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        if (mVerticalAlignment != ALIGN_VERTICAL_CENTER) {
            return super.getSize(paint, text, start, end, fm);
        }

        Drawable d = getDrawable();
        // vivo手机在7.1的room下，这里可能拿到NUL， 因为如果非ui线程在openRawResource的时候可能会报indexOutOfBunds的崩溃，super里面又try住了
        if (d == null) {
            DanmakuLoggers.e("CustomImageSpan", "vivo7.1 crash protected");
            return 0;
        }
        Rect rect = d.getBounds();
        if (fm != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;

            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;

            fm.ascent = -bottom;
            fm.top = -bottom;
            fm.bottom = top;
            fm.descent = top;
        }
        if (mMarginLeft == 0 && mMarginRight == 0) {
            return (int) (rect.right + mMargin * 2);
        }
        return (int) (rect.right + mMarginLeft + mMarginRight);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        if (mVerticalAlignment != ALIGN_VERTICAL_CENTER) {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            return;
        }
        Drawable d = getDrawable();
        if (d == null) {
            // drawable 已经为NULL了 没有继续draw的意义了
            return;
        }
        canvas.save();
        if (mMarginLeft == 0 && mMarginRight == 0) {
            x += mMargin;
        }
        x += mMarginLeft;
        int transY = 0;
        transY = ((bottom - top) - d.getBounds().bottom) / 2 + top;
        canvas.translate(x, transY);
        if (d instanceof BitmapDrawable) {
            BitmapDrawable drawable = (BitmapDrawable) d;
            if (drawable.getBitmap() != null && !drawable.getBitmap().isRecycled()) {
                d.draw(canvas);
            }
        } else {
            d.draw(canvas);
        }
        canvas.restore();
    }
}  
