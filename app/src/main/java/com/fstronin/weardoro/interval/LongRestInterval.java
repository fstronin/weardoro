package com.fstronin.weardoro.interval;

import android.content.Context;
import android.os.Parcel;

import com.fstronin.weardoro.R;

public class LongRestInterval extends Interval
{
    public LongRestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_LONG_REST_INTERVAL, alarmPendingIntentBuilder);
    }

    protected LongRestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_LONG_REST_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    protected LongRestInterval(Parcel in) {
        super(in);
    }

    @Override
    public IInterval getNext() {
        return new FocusInterval(this.getAlarmPendingIntentBuilder(), this.getFocusIntervalsBeenInChain());
    }

    @Override
    public String getDisplayName(Context ctx)
    {
        return ctx.getString(R.string.text_timer_mode_long_rest);
    }

    public static final Creator<LongRestInterval> CREATOR = new Creator<LongRestInterval>() {
        @Override
        public LongRestInterval createFromParcel(Parcel in) {
            return new LongRestInterval(in);
        }

        @Override
        public LongRestInterval[] newArray(int size) {
            return new LongRestInterval[size];
        }
    };

    public Type getType()
    {
        return Type.LONG_REST;
    }
}
