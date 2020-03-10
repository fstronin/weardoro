package com.fstronin.weardoro.interval;

import android.content.Context;
import android.os.Parcelable;

public interface IInterval extends Parcelable
{
    // long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 60L * 25L;
    long DEFAULT_DURATION_FOCUS_INTERVAL = 1000L * 25L;
    // long DEFAULT_DURATION_REST_INTERVAL = 1000L * 60L * 5L;
    long DEFAULT_DURATION_REST_INTERVAL = 1000L * 5L;
    // long DEFAULT_DURATION_LONG_REST_INTERVAL = 1000L * 60L * 15L;
    long DEFAULT_DURATION_LONG_REST_INTERVAL = 1000L * 15L;
    int DEFAULT_LONG_REST_INTERVAL_POSITION = 4;

    final String ALARM_INTENT_ACTION_INTERVAL_FINISHED = "com.fstronin.weardoro.INTERVAL_FINISHED";
    final String ALARM_INTENT_ACTION_INTERVAL_STARTED = "com.fstronin.weardoro.INTERVAL_STARTED";
    final String ALARM_INTENT_ACTION_INTERVAL_PAUSED = "com.fstronin.weardoro.INTERVAL_PAUSED";
    final String ALARM_INTENT_ACTION_INTERVAL_STOPPED = "com.fstronin.weardoro.INTERVAL_STOPPED";
    final String ALARM_INTENT_ACTION_INTERVAL_START_REQUESTED = "com.fstronin.weardoro.INTERVAL_START_REQUESTED";
    final String ALARM_INTENT_ACTION_INTERVAL_PAUSE_REQUESTED = "com.fstronin.weardoro.INTERVAL_PAUSE_REQUESTED";
    final String ALARM_INTENT_ACTION_INTERVAL_RESUME_REQUESTED = "com.fstronin.weardoro.INTERVAL_RESUME_REQUESTED";
    final String ALARM_INTENT_ACTION_INTERVAL_STOP_REQUESTED = "com.fstronin.weardoro.INTERVAL_STOP_REQUESTED";
    final String ALARM_INTENT_INTERVAL_INSTANCE_KEY = "interval";
    final String PREF_KEY_INTERVAL_CLASS = "INTERVAL_CLASS";
    final String PREF_KEY_INTERVAL_DATA = "INTERVAL_DATA";

    State getState();
    long getDuration();
    long getStartedAt();
    long getPausedAt();
    void start(Context ctx) throws IntervalException;
    void pause(Context ctx) throws IntervalException;
    void resume(Context ctx) throws IntervalException;
    void stop(Context ctx) throws IntervalException;
    IInterval getNext();
    long getMillisInFuture();
    String getDisplayName(Context ctx);
}
