package com.fstronin.weardoro.interval;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import com.fstronin.weardoro.App;

abstract public class Interval implements IInterval
{
    private State mState = State.IDLE;
    private long mDuration;
    private long mElapsed;
    private long mStartedAt;
    private long mResumedAt;
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

    protected Interval(Parcel in) {
        mState = State.valueOf(in.readString());
        mDuration = in.readLong();
        mElapsed = in.readLong();
        mStartedAt = in.readLong();
        mPausedAt = in.readLong();
        mResumedAt = in.readLong();
        mAlarmPendingIntentBuilder = in.readParcelable(AlarmPendingIntentBuilder.class.getClassLoader());
        mFocusIntervalsBeenInChain = in.readInt();
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

    public void save(Context ctx)
    {
        App.getSharedPreferences(ctx)
                .edit()
                .putString(IInterval.PREF_KEY_INTERVAL_CLASS, this.getClass().getName())
                .putString(IInterval.PREF_KEY_INTERVAL_DATA, App.getGson().toJson(this))
                .apply();
    }

    private void notifySubscribers(Context ctx, String action)
    {
        ctx.sendBroadcast(
            (new Intent())
                    .setAction(action)
                    .putExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY, this)
        );
    }

    private void start(Context ctx, long startedAt) throws IntervalException
    {
        if (mState != State.IDLE) {
            throw new IntervalException("Can not start from state \"%s\"", mState.name());
        }
        mStartedAt = startedAt;
        App
                .getAlarmManager(ctx)
                .setExactAndAllowWhileIdle(
                        App.getAlarmType(),
                        getAlarmTimeInMillis(),
                        mAlarmPendingIntentBuilder.build(ctx, this)
                );
        mState = State.RUNNING;
        notifySubscribers(ctx, IInterval.ALARM_INTENT_ACTION_INTERVAL_STARTED);
        save(ctx);
    }

    public void start(Context ctx) throws IntervalException
    {
        start(ctx, System.currentTimeMillis());
    }

    private void cancelAlarm(Context ctx)
    {
        App
                .getAlarmManager(ctx)
                .cancel(mAlarmPendingIntentBuilder.build(ctx, this));
    }

    public void pause(Context ctx) throws IntervalException
    {
        if (mState != State.RUNNING) {
            throw new IntervalException("Can not pause from state \"%s\"", mState.name());
        }
        mPausedAt = System.currentTimeMillis();
        mElapsed += mPausedAt - (mResumedAt > 0 ? mResumedAt : mStartedAt);
        cancelAlarm(ctx);
        mState = State.PAUSED;
        notifySubscribers(ctx, IInterval.ALARM_INTENT_ACTION_INTERVAL_PAUSED);
        save(ctx);
    }

    public void resume(Context ctx) throws IntervalException
    {
        if (mState != State.PAUSED) {
            throw new IntervalException("Can not resume from state \"%s\"", mState.name());
        }
        mState = State.IDLE;
        mResumedAt = System.currentTimeMillis();
        start(ctx, mStartedAt);
    }

    public void stop(Context ctx) throws IntervalException
    {
        if (mState != State.RUNNING && mState != State.PAUSED) {
            throw new IntervalException(
                    "Can stop only from states \"%s\" and \"%s\"",
                    State.RUNNING.name(),
                    State.PAUSED.name()
            );
        }
        cancelAlarm(ctx);
        mState = State.IDLE;
        mElapsed = 0;
        mStartedAt = 0;
        mPausedAt = 0;
        mResumedAt = 0;
        mFocusIntervalsBeenInChain = 0;
        save(ctx);
        notifySubscribers(ctx, IInterval.ALARM_INTENT_ACTION_INTERVAL_STOPPED);
    }

    public long getElapsed()
    {
        return mElapsed + System.currentTimeMillis() - (mResumedAt > 0 ? mResumedAt : mStartedAt);
    }

    public boolean isFinished()
    {
        return getElapsed() >= mDuration;
    }

    public long getMillisInFuture()
    {
        return mDuration - getElapsed();
    }

    private long getAlarmTimeInMillis()
    {
        return mElapsed > 0L
                ? System.currentTimeMillis() + (mDuration - getElapsed())
                : System.currentTimeMillis() + mDuration;
    }

    public AlarmPendingIntentBuilder getAlarmPendingIntentBuilder()
    {
        return mAlarmPendingIntentBuilder;
    }

    protected int getFocusIntervalsBeenInChain()
    {
        return mFocusIntervalsBeenInChain;
    }

    abstract public IInterval getNext();

    abstract public String getDisplayName(Context ctx);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mState.name());
        dest.writeLong(mDuration);
        dest.writeLong(mElapsed);
        dest.writeLong(mStartedAt);
        dest.writeLong(mPausedAt);
        dest.writeLong(mResumedAt);
        dest.writeParcelable(mAlarmPendingIntentBuilder, 0);
        dest.writeInt(mFocusIntervalsBeenInChain);
    }
}
