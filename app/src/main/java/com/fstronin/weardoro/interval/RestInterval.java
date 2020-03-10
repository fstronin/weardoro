package com.fstronin.weardoro.interval;

import android.content.Context;
import android.os.Parcel;

import com.fstronin.weardoro.R;

public class RestInterval extends Interval
{
    public RestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_REST_INTERVAL, alarmPendingIntentBuilder);
    }

    protected RestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_REST_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    protected RestInterval(Parcel in) {
        super(in);
    }

    @Override
    public IInterval getNext() {
        return new FocusInterval(this.getAlarmPendingIntentBuilder(), this.getFocusIntervalsBeenInChain());
    }

    @Override
    public String getDisplayName(Context ctx)
    {
        return ctx.getString(R.string.text_timer_mode_rest);
    }

    public static final Creator<RestInterval> CREATOR = new Creator<RestInterval>() {
        @Override
        public RestInterval createFromParcel(Parcel in) {
            return new RestInterval(in);
        }

        @Override
        public RestInterval[] newArray(int size) {
            return new RestInterval[size];
        }
    };
}
