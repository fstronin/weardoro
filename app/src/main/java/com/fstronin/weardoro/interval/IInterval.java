package com.fstronin.weardoro.interval;

import android.content.Context;

public interface IInterval
{
    // long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 60L * 25L;
    long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 15L;
    // long DEFAULT_DURATION_REST_INTERVAL = 1000L * 60L * 5L;
    long DEFAULT_DURATION_REST_INTERVAL = 1000L * 5L;
    // long DEFAULT_DURATION_LONG_REST_INTERVAL = 1000L * 60L * 15L;
    long DEFAULT_DURATION_LONG_REST_INTERVAL = 1000L * 10L;
    int DEFAULT_LONG_REST_INTERVAL_POSITION = 4;

    final String ALARM_INTENT_ACTION_INTERVAL_FINISHED = "com.fstronin.weardoro.INTERVAL_FINISHED";
    final String ALARM_INTENT_INTERVAL_INSTANCE_KEY = "interval";
    final String ALARM_INTENT_INTERVAL_CLASS_KEY = "interval-class";

    State getState();
    long getDuration();
    long getStartedAt();
    long getPausedAt();
    void start(Context ctx) throws IntervalException;
    void pause(Context ctx) throws IntervalException;
    void resume(Context ctx) throws IntervalException;
    IInterval getNext();
}
