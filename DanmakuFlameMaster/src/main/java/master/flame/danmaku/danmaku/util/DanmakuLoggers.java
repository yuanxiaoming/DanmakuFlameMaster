package master.flame.danmaku.danmaku.util;

import android.util.Log;

public class DanmakuLoggers {
    private static final String TAG = "DanmakuLoggers";
    private static IDanmakuLoggers sLoggers;

    public static void setLoggers(IDanmakuLoggers loggers) {
        sLoggers = loggers;
    }

    public static void v(String tag, String msg) {
        if (sLoggers == null) {
            Log.v(tag, msg);
        } else {
            sLoggers.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sLoggers == null) {
            Log.d(tag, msg);
        } else {
            sLoggers.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sLoggers == null) {
            Log.i(tag, msg);
        } else {
            sLoggers.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sLoggers == null) {
            Log.w(tag, msg);
        } else {
            sLoggers.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (sLoggers == null) {
            Log.e(tag, msg);
        } else {
            sLoggers.e(tag, msg);
        }
    }

    public static void e(String tag, String format, Throwable args) {
        if (tag == null || format == null || args == null) {
            w(TAG, "param is null error!!!");
            return;
        }
        if (sLoggers == null) {
            Log.e(tag, format + args.toString());
        } else {
            sLoggers.e(tag, format, args);
        }
    }
}
