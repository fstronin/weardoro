package com.fstronin.weardoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fstronin.weardoro.interval.IInterval;
import com.fstronin.weardoro.interval.IntervalException;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (null == action) {
            App.getLogger().e(this.getClass().getName(), "Empty action");
            return;
        }
        Bundle intervalContainer = intent.getBundleExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
        if (null == intervalContainer) {
            App.getLogger().e(this.getClass().getName(), "Unable to obtain an interval container from an intent");
            return;
        }
        IInterval interval = intervalContainer.getParcelable(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
        if (null == interval) {
            App.getLogger().e(this.getClass().getName(), "Unable to obtain interval instance from an intent");
            return;
        }
        IInterval nextInterval = interval.getNext();
        try {
            nextInterval.start(context);
        } catch (IntervalException e) {
            App.getLogger().e(this.getClass().getName(), e.getMessage(), e);
        }
    }
}
