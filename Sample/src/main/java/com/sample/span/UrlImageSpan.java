package com.sample.span;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.sample.custom.ScheduledTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import master.flame.danmaku.danmaku.util.DanmakuLoggers;

/**
 * Created by zhp on 2018/2/5.
 * url ImageSpan
 */

public class UrlImageSpan extends CustomImageSpan implements SpanTextView.SpanCallback {
    private static final String TAG = "UrlImageSpan";
    private static final int MAX_PRELOAD_SIZE = 50;
    private static final AtomicInteger sPreloadSize = new AtomicInteger(0);

    private String url;
    private WeakReference<TextView> textViewRef;

    private Rect drawableBounds;
    private Drawable urlDrawable = null;
    private UrlImageSpanCallBack urlImageSpanCallBack;
    private boolean isLoaded = false;
    private UrlDrawableHook hook;
    FutureTarget<Drawable> target = null;
    private LoadRunnable loadRunnable = null;

    public UrlImageSpan(String url) {
        this(url, createEmptyDrawable());
    }

    public UrlImageSpan(String url, Drawable placeHolder) {
        super(placeHolder);
        this.url = url;
    }

    public Rect getDrawableBounds() {
        return drawableBounds;
    }

    public void setDrawableBounds(Rect drawableBounds) {
        if (drawableBounds == null) {
            return;
        }
        this.drawableBounds = drawableBounds;
        if (urlDrawable != null) {
            urlDrawable.setBounds(drawableBounds);
        } else {
            if (getDrawable() != null) {
                getDrawable().setBounds(drawableBounds);
            }
        }
        if (drawableBounds != null) {
            setOriginSize(drawableBounds.width(), drawableBounds.height());
        }
    }

    public void setSingleDrawableBounds(Rect drawableBounds) {
        if (drawableBounds != null) {
            this.drawableBounds = drawableBounds;
            if (urlDrawable != null) {
                urlDrawable.setBounds(drawableBounds);
            }
            setOriginSize(drawableBounds.width(), drawableBounds.height());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void preload(Context context) {
        if (sPreloadSize.get() <= MAX_PRELOAD_SIZE) {
            if (loadDrawable(context, true)) {
                sPreloadSize.incrementAndGet();
            }
        }
    }

    @Override
    public Drawable getDrawable() {
        if (urlDrawable == null) {
            return super.getDrawable();
        } else {
            return urlDrawable;
        }
    }

    @Override
    public void onAttach(TextView textView) {
        textViewRef = new WeakReference<>(textView);
        loadDrawable(textView.getContext(), false);
    }

    @Override
    public void onDetach() {
        isLoaded = false;
        textViewRef = null;
        if (target != null) {
            target.cancel(true);
        }
        clearLoadRunnable(true);
    }

    private synchronized void clearLoadRunnable(boolean removeTask) {
        if (null != loadRunnable) {
            if (removeTask) {
                isLoaded = false;
                ScheduledTask.getInstance().removeCallbacks(loadRunnable);
                if (loadRunnable.isPreload) {
                    sPreloadSize.decrementAndGet();
                }
            }
            loadRunnable = null;
        }
    }

    private abstract class LoadRunnable implements Runnable {

        public boolean isPreload;

        public LoadRunnable(boolean isPreload) {
            this.isPreload = isPreload;
        }
    }

    private boolean loadDrawable(final Context context, boolean isPreload) {
        if (!isLoaded && !TextUtils.isEmpty(url)) {
            isLoaded = true;
            if (!checkContextValid(context)) {
                return false;
            }
            if (null != loadRunnable) {
                return false;
            }
            loadRunnable = new LoadRunnable(isPreload) {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void run() {
                    clearLoadRunnable(false);
                    if (!checkContextValid(context)) {
                        if (isPreload) {
                            sPreloadSize.decrementAndGet();
                        }
                        return;
                    }
                    RequestOptions requestOptions =
                            new RequestOptions().fitCenter().diskCacheStrategy(
                                    DiskCacheStrategy.AUTOMATIC)
                                    .format(DecodeFormat.PREFER_ARGB_8888)
                                    .skipMemoryCache(false);
                    int width = Target.SIZE_ORIGINAL;
                    int height = Target.SIZE_ORIGINAL;
                    if (getDrawableBounds() != null && !getDrawableBounds().isEmpty()) {
                        width = getDrawableBounds().width();
                        height = getDrawableBounds().height();
                    }
                    target = Glide.with(context).load(url)
                            .apply(requestOptions).submit(width, height);
                    try {
                        final Drawable drawable = target.get();
                        if (drawableBounds != null) {
                            drawable.setBounds(drawableBounds);
                        }
                        if (hook != null) {
                            urlDrawable = hook.hook(drawable, context);
                        } else {
                            urlDrawable = drawable;
                        }
                        ScheduledTask.getInstance().scheduledMain(new Runnable() {
                            @Override
                            public void run() {
                                updateSpan(urlDrawable);
                            }
                        });
                    } catch (Throwable e) {
                        DanmakuLoggers.e(TAG, "e:" + e);
                    } finally {
                        if (target != null) {
                            target.cancel(true);
                        }
                        if (isPreload) {
                            sPreloadSize.decrementAndGet();
                        }
                    }
                }
            };
            ScheduledTask.getInstance().scheduledDelayed(loadRunnable, 0);
            return true;
        }
        return false;
    }

    private void updateSpan(Drawable urlDrawable) {
        boolean isRefreshed = false;
        if (urlImageSpanCallBack != null) {
            isRefreshed = urlImageSpanCallBack.onReady(urlDrawable);
        }
        if (!isRefreshed && textViewRef != null) {
            TextView tv = textViewRef.get();
            if (tv != null) {
                tv.setText(tv.getText());
            }
        }
    }

    public void setUrlImageSpanCallBack(
            UrlImageSpanCallBack urlImageSpanCallBack) {
        this.urlImageSpanCallBack = urlImageSpanCallBack;
    }

    public void setHook(UrlDrawableHook hook) {
        this.hook = hook;
    }

    private static Drawable createEmptyDrawable() {
        ColorDrawable d = new ColorDrawable(Color.TRANSPARENT);
        d.setBounds(0, 0, 1, 1);
        return d;
    }

    private boolean checkContextValid(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return false;
        }
        return true;
    }
}