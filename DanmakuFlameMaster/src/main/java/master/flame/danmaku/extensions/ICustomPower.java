package master.flame.danmaku.extensions;

import android.content.Context;
import android.graphics.Bitmap;

public interface ICustomPower {

    //自定义弹幕绘制方法 最终生成一张Bitmap
    void createPowerToShell(Context context);

    //返回自定义生成的图片绘制
    Bitmap getBitmap();

    void setICustomInvalidateCallBack(ICustomInvalidateDanmaku iCustomInvalidateDanmaku);

    ICustomInvalidateDanmaku getICustomInvalidateCallBack();

    //是否需要动态刷新
    boolean needInvalidate();

    void clear();

}
