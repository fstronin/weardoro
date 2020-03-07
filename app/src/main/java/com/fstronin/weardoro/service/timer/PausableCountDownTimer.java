package com.fstronin.weardoro.service.timer;

import android.os.SystemClock;

import com.fstronin.weardoro.logging.LoggerInterface;

public abstract class PausableCountDownTimer extends CustomCountDownTimer {
    private final String CLASS_NAME;
    private final int MSG = 1;
    private LoggerInterface mLogger;
    private boolean mPaused = false;
    private long mPausedTime;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public PausableCountDownTimer(
            long millisInFuture,
            long countDownInterval,
            LoggerInterface logger
    ) {
        super(millisInFuture, countDownInterval);
        CLASS_NAME = PausableCountDownTimer.class.toString();
        mLogger = logger;
    }

    public void pause()
    {
        if (mPaused) {
            mLogger.e(CLASS_NAME, "The timer already paused, cannot pause");
            return;
        }
        mPaused = true;
        mPausedTime = SystemClock.elapsedRealtime();
        getHandler().removeMessages(MSG);
    }

    public void resume()
    {
        if (!mPaused) {
            mLogger.e(CLASS_NAME, "The timer is not paused, cannot resume");
            return;
        }
        mPaused = false;
        long diff = SystemClock.elapsedRealtime() - mPausedTime;
        addToStopTimeInFuture(diff);
        getHandler().sendMessage(getHandler().obtainMessage(MSG));
    }
}
