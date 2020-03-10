package com.fstronin.weardoro.interval;

public class FocusInterval extends Interval
{
    public FocusInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder) {
        super(DEFAULT_DURATION_FOCUS_INTERVAL, alarmPendingIntentBuilder);
    }

    protected FocusInterval(AlarmPendingIntentBuilder alarmPendingIntentBuilder, int focusIntervalsBeenInChain)
    {
        super(DEFAULT_DURATION_REST_INTERVAL, alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }

    @Override
    public IInterval getNext() {
        AlarmPendingIntentBuilder alarmPendingIntentBuilder = this.getAlarmPendingIntentBuilder();
        int focusIntervalsBeenInChain = this.getFocusIntervalsBeenInChain() + 1;
        return focusIntervalsBeenInChain > 0 && focusIntervalsBeenInChain % IInterval.DEFAULT_LONG_REST_INTERVAL_POSITION == 0
                ? new LongRestInterval(alarmPendingIntentBuilder, focusIntervalsBeenInChain)
                : new RestInterval(alarmPendingIntentBuilder, focusIntervalsBeenInChain);
    }
}
