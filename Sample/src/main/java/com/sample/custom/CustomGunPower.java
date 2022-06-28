package com.sample.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import master.flame.danmaku.extensions.ICustomInvalidateDanmaku;
import master.flame.danmaku.extensions.ICustomPower;

public class CustomGunPower implements ICustomPower {
    //进行该判断 说明需要动态刷新bitmap bitmap不能 recycle
    protected Bitmap mBitmap;
    public String content;
    public String nickname;

    @Override
    public void createPowerToShell(Context context) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        int sp2px12 = DimenConverter.sp2px(context, 12);
        int dip2px12 = DimenConverter.dip2px(context, 12);
        int dip2px11 = DimenConverter.dip2px(context, 11);
        Paint paint = new Paint();
        paint.setTextSize(sp2px12);
        paint.setColor(Color.parseColor("#4DF5181D"));
        paint.setStyle(Paint.Style.FILL);
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int dy = (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.bottom;
        int baseLine = (DimenConverter.dip2px(context, 20) / 2) + dy;

        int indexName = content.indexOf("[name]");
        if (indexName != -1) {
            content = content.replace("[name]", nickname);
        }
        float textWidth = paint.measureText(content);
        float width = textWidth + DimenConverter.dip2px(context, 24);
        mBitmap = Bitmap.createBitmap((int) width, DimenConverter.dip2px(context, 22),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(mBitmap);
        int saved = canvas.saveLayer(0f, 0f, mBitmap.getWidth(), mBitmap.getHeight(),
                null);
        canvas.drawRoundRect(0f, 0f, mBitmap.getWidth(), mBitmap.getHeight(),
                dip2px11, dip2px11, paint);
        if (indexName != -1) {
            if (indexName == 0) {
                paint.setColor(Color.parseColor("#FFE078"));
                canvas.drawText(nickname, dip2px12, baseLine, paint);
                paint.setColor(Color.WHITE);
                float x = dip2px12 + paint.measureText(nickname);
                String txt2 = content.substring(nickname.length());
                canvas.drawText(txt2, x, baseLine, paint);
            } else {
                paint.setColor(Color.parseColor("#FFE078"));
                String txt1 = content.substring(0, indexName);
                float tWidth1 = paint.measureText(txt1);
                canvas.drawText(nickname, dip2px12 + tWidth1, baseLine,
                        paint);
                paint.setColor(Color.WHITE);
                canvas.drawText(txt1, dip2px12, baseLine, paint);
                float x =
                        dip2px12 + tWidth1 + paint.measureText(nickname);
                String txt2 = content.substring(indexName + nickname.length());
                canvas.drawText(txt2, x, baseLine, paint);
            }
        } else {
            paint.setColor(Color.WHITE);
            canvas.drawText(content, dip2px12, baseLine, paint);
        }
        canvas.restoreToCount(saved);
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void setICustomInvalidateCallBack(ICustomInvalidateDanmaku iCustomInvalidateDanmaku) {

    }

    @Override
    public ICustomInvalidateDanmaku getICustomInvalidateCallBack() {
        return null;
    }

    @Override
    public boolean needInvalidate() {
        return false;
    }

    @Override
    public void clear() {

    }
}
