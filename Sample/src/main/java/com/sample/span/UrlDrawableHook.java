package com.sample.span;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface UrlDrawableHook {
    Drawable hook(Drawable drawable, Context context);
}
