package com.fstronin.weardoro.interval;

import android.content.Context;

import com.fstronin.weardoro.App;

abstract public class Interval implements IInterval
{
    private State mState = State.IDLE;
    private long mDuration;
    private long mStartedAt;
    private long mPausedAt;
    private AlarmPendingIntentBuilder mAlarmPendingIntentBuilder;
    private int mFocusIntervalsBeenInChain;

    public Interval(long duration, AlarmPendingIntentBuilder alarmPendingIntentBuilder)
    {
        mDuration = duration;
        mAlarmPendingIntentBuilder = alarmPendingIntentBuilder;
    }

    protected Interval(long duration, AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        mDuration = duration;
        mAlarmPendingIntentBuilder = alarmPendingIntentBuilder;
        mFocusIntervalsBeenInChain = focusIntervalsBeenInChain;
    }

    public State getState()
    {
        return mState;
    }

    public long getDuration()
    {
        return mDuration;
    }

    public long getStartedAt()
    {
        return mStartedAt;
    }

    public long getPausedAt()
    {
        return mPausedAt;
    }

    public void start(Context ctx) throws IntervalException
    {
        if (mState != State.IDLE) {
            throw new IntervalException("Can not start from state \"%s\"", mState.name());
        }
        mStartedAt = System.currentTimeMillis();
        App
                .getAlarmManager(ctx)
                .setExactAndAllowWhileIdle(
                        App.getAlarmType(),
                        getAlarmTimeInMillis(System.currentTimeMillis()),
                        mAlarmPendingIntentBuilder.build(ctx, this)
                );
        mState = State.RUNNING;
    }

    public void pause(Context ctx) throws IntervalException
    {
        if (mState != State.RUNNING) {
            throw new IntervalException("Can not pause from state \"%s\"", mState.name());
        }
        mPausedAt = System.currentTimeMillis();
        App
                .getAlarmManager(ctx)
                .cancel(mAlarmPendingIntentBuilder.build(ctx, this));
        mState = State.PAUSED;
    }

    public void resume(Context ctx) throws IntervalException
    {
        if (mState != State.PAUSED) {
            throw new IntervalException("Can not resume from state \"%s\"", mState.name());
        }
        mState = State.IDLE;
        start(ctx);
    }

    private long getAlarmTimeInMillis(long currentTime)
    {
        return currentTime + getDuration() - getPausedAt();
    }

    public AlarmPendingIntentBuilder getAlarmPendingIntentBuilder()
    {
        return new AlarmPendingIntentBuilder(mAlarmPendingIntentBuilder);
    }

    protected int getFocusIntervalsBeenInChain()
    {
        return mFocusIntervalsBeenInChain;
    }

    abstract public IInterval getNext();
}
