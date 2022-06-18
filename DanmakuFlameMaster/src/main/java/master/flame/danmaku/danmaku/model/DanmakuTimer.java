/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package master.flame.danmaku.danmaku.model;

/**
 * 不更改现有接口和外部调用的情况下修改timer源码来支持弹幕动态倍速。
 * currMillisecond用来确定弹幕的时间，如在外部updateTimer回调中手动更改currMillisecond来更改弹幕时间，
 * 即把currMillisecond加上lastInterval乘以视频速度-1的差（因为在回调之前已经加上了一个lastInterval了），
 * 并不能实现弹幕倍速，因为会出现一个问题：下次回调时，由于lastInterval是根据新的currMillisecond减去之前的currMillisecond得出的，
 * 但之前的currMillisecond是经过修改的，故lastInterval并不是真实的现实间隔时间，currMillisecond向前或向后修改，
 * 之后的lastInterval就会相应的增加或减少，导致弹幕的速度会慢慢的趋近于正常速度。
 * 我构想的解决方法是，lastInterval并不由currMillisecond计算得出，而是由类内获取系统时间，算出现实时间的间隔，
 * 并且currMillisecond由这个间隔和视频倍速算出弹幕正确的时间。
 * 这样，传入的curr参数就只剩一个作用：判断视频是否进行了跳转。当curr
 * 的间隔和类内获取当前时间的间隔差值超过一定值时，就可以认为视频进行了跳转，弹幕也需要跳转到新的时间。
 * 在弹幕开始播放时，我储存了第一个curr的数值，由新的curr减去第一个curr，就能得出视频当前时间，直接让currMillisecond等于这个值就行。
 * 如何设置弹幕倍速可以参考以下方式：
 */
public class DanmakuTimer {
    public long currMillisecond = 0L;
    private long lastInterval;

    private float videoSpeed = 1.0f;
    private long lastTimeStamp = 0L;
    private long lastCurr;
    private long firstCurr;

    public DanmakuTimer() {
    }

    public DanmakuTimer(long curr) {
        update(curr);
    }

    public long update(long curr) {
        if (lastTimeStamp == 0) {
            lastTimeStamp = System.currentTimeMillis();
            firstCurr = curr;
        }
        long t = System.currentTimeMillis();
        lastInterval = t - lastTimeStamp;

        if ((lastInterval - curr + lastCurr) > 2000 || (lastInterval - curr + lastCurr) < -2000) {
            currMillisecond = curr - firstCurr;
        } else {
            currMillisecond += lastInterval * videoSpeed;
        }

        lastCurr = curr;
        lastTimeStamp = t;
        return lastInterval;
    }

    public long add(long mills) {
        return update(currMillisecond + mills);
    }

    public long lastInterval() {
        return lastInterval;
    }

    public void setSpeed(float speed) {
        videoSpeed = speed;
    }

    public float getSpeed() {
        return videoSpeed;
    }
}
