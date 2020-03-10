package com.fstronin.weardoro.interval;

public class LongRestInterval extends Interval
{
    public LongRestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_LONG_REST_INTERVAL, alarmPendingIntentBuilder);
    }

    protected LongRestInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_LONG_REST_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    @Override
    public IInterval getNext() {
        return new FocusInterval(this.getAlarmPendingIntentBuilder(), this.getFocusIntervalsBeenInChain());
    }
}
