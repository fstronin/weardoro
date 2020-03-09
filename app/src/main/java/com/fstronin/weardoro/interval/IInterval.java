package com.fstronin.weardoro.interval;

import android.content.Context;

public interface IInterval
{
    // long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 60L * 25L;
    long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 5L;
    long DEFAULT_DURATION_REST_INTERVAL = 1000L * 60L * 5L;
    long DEFAULT_DURATION_LONG_REST_INTERVAL = 1000L * 60L * 15L;

    State getState();
    long getDuration();
    long getStartedAt();
    long getPausedAt();
    void start(Context ctx) throws IntervalException;
    void pause(Context ctx) throws IntervalException;
    void resume(Context ctx) throws IntervalException;
}
