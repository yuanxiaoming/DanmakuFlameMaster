package master.flame.danmaku.danmaku.util;

public interface IDanmakuLoggers {
    void d(String tag, String msg);

    void v(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void e(String tag, String msg);

    void e(String tag, Throwable t);

    void e(String tag, String format, Throwable t);
}
