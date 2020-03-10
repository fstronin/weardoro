package com.fstronin.weardoro.interval;

public class RestInterval extends Interval
{
    public RestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_REST_INTERVAL, alarmPendingIntentBuilder);
    }

    protected RestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_REST_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    @Override
    public IInterval getNext() {
        return new FocusInterval(this.getAlarmPendingIntentBuilder(), this.getFocusIntervalsBeenInChain());
    }
}
