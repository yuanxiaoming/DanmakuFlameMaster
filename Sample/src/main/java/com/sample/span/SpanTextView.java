package com.sample.span;

import android.content.Context;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

import master.flame.danmaku.danmaku.util.DanmakuLoggers;

/**
 * Created by zhp on 2018/2/5.
 * Span 能收到 onAttach onDetach 的 TextView
 */

public class SpanTextView extends TextView {
    private boolean hasSpanCallback = false;
    private boolean isSpanAttached = false;
    private SpanCallback[] spanCallbacks;

    public SpanTextView(Context context) {
        super(context);
    }

    public SpanTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpanTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        onDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        onAttach();
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        boolean wasSpanAttached = isSpanAttached;
        if (hasSpanCallback && wasSpanAttached) {
            onDetach();
        }
        if (text instanceof Spanned) {
            try {
                spanCallbacks = ((Spanned) text).getSpans(0, text.length(), SpanCallback.class);
                hasSpanCallback = spanCallbacks.length > 0;
            } catch (ArrayIndexOutOfBoundsException e) {
                DanmakuLoggers.w("SpanTextView", e.toString());
            }
        } else {
            spanCallbacks = null;
            hasSpanCallback = false;
        }
        super.setText(text, type);
        if (hasSpanCallback && wasSpanAttached) {
            onAttach();
        }
    }

    private void onAttach() {
        if (spanCallbacks != null) {
            for (SpanCallback callback : spanCallbacks) {
                callback.onAttach(this);
            }
        }
        isSpanAttached = true;
    }

    private void onDetach() {
        if (spanCallbacks != null) {
            for (SpanCallback callback : spanCallbacks) {
                callback.onDetach();
            }
        }
        isSpanAttached = false;
    }

    /**
     * 能收到 SpanTextView onAttach onDetach 的 Span 接口
     */
    public interface SpanCallback {
        void onAttach(TextView textView);

        void onDetach();
    }
}