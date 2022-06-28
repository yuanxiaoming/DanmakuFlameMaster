package com.sample.custom;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class SafeDispatchHandler extends Handler {
    private static final String TAG = "SafeDispatchHandler";

    public SafeDispatchHandler(Looper looper) {
        super(looper);
    }

    public SafeDispatchHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    public SafeDispatchHandler() {
        super();
    }

    public SafeDispatchHandler(Callback callback) {
        super(callback);
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
        } catch (Error error) {
        }
    }
}
