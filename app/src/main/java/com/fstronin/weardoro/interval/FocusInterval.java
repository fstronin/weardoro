package com.fstronin.weardoro.interval;

import android.content.Context;
import android.os.Parcel;

import com.fstronin.weardoro.R;

public class FocusInterval extends Interval
{
    public FocusInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_FOCUS_INTERVAL, alarmPendingIntentBuilder);
    }

    protected FocusInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_FOCUS_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    protected FocusInterval(Parcel in) {
        super(in);
    }

    @Override
    public IInterval getNext() {
        AlarmPendingIntentBuilder alarmPendingIntentBuilder = this.getAlarmPendingIntentBuilder();
        int focusIntervalsBeenInChain = this.getFocusIntervalsBeenInChain() + 1;
        return focusIntervalsBeenInChain > 0 && focusIntervalsBeenInChain % IInterval.DEFAULT_LONG_REST_INTERVAL_POSITION == 0
                ? new LongRestInterval(alarmPendingIntentBuilder, focusIntervalsBeenInChain)
                : new RestInterval(alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    @Override
    public String getDisplayName(Context ctx)
    {
        return ctx.getString(R.string.text_timer_mode_focus);
    }

    public static final Creator<FocusInterval> CREATOR = new Creator<FocusInterval>() {
        @Override
        public FocusInterval createFromParcel(Parcel in) {
            return new FocusInterval(in);
        }

        @Override
        public FocusInterval[] newArray(int size) {
            return new FocusInterval[size];
        }
    };
}
