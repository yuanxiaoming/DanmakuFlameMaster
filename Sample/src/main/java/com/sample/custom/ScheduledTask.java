package com.sample.custom;

import android.os.HandlerThread;
import android.os.Looper;

public final class ScheduledTask {
    private volatile static ScheduledTask inStance;
    private volatile Looper mTaskLooper;
    private volatile SafeDispatchHandler mTaskHandler;
    private HandlerThread thread;

    private volatile SafeDispatchHandler mMainHandler;

    /**
     * 私有构造函数，防止误实例化
     */
    private ScheduledTask() {
        thread = new HandlerThread("ScheduledTask");
        thread.start();
        mTaskLooper = thread.getLooper();
        mTaskHandler = new SafeDispatchHandler(mTaskLooper);
        mMainHandler = new SafeDispatchHandler(Looper.getMainLooper());

    }

    /**
     * 获取单实例
     */
    public static ScheduledTask getInstance() {
        if (inStance == null) {
            synchronized (ScheduledTask.class) {
                if (inStance == null) {
                    inStance = new ScheduledTask();
                }
            }
        }
        return inStance;
    }

    //目前只用在如下场景：当启动过程中，主线程等待该线程的启动异步化过程完成，设置线程优先级为高；
    //其他业务请不要随便使用；
    public void setThreadPriority(int priority) {
        if (thread.getPriority() != priority) {
            thread.setPriority(priority);
        }
    }

    /**
     * 延迟执行任务，单位milliseconds
     */
    public boolean scheduledDelayed(Runnable command, long delay) {
        mTaskHandler.removeCallbacks(command);
        return mTaskHandler.postDelayed(command, delay);
    }

    public boolean scheduledMain(Runnable command) {
        mMainHandler.removeCallbacks(command);
        return mMainHandler.post(command);
    }

    /**
     * 指定时刻执行任务，uptimeMillis(using the SystemClock.uptimeMillis() time-base)
     */
    public boolean scheduledAtTime(Runnable command, long uptimeMillis) {
        mTaskHandler.removeCallbacks(command);
        return mTaskHandler.postAtTime(command, uptimeMillis);
    }

    /**
     * 停止计时器
     */
    public void removeCallbacks(Runnable command) {
        mTaskHandler.removeCallbacks(command);
    }

    public boolean isInterrupted() {
        return thread != null && thread.isInterrupted();
    }
}
