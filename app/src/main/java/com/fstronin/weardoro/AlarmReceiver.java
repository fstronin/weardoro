package com.fstronin.weardoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fstronin.weardoro.interval.IInterval;
import com.fstronin.weardoro.interval.IntervalException;

import java.lang.reflect.Type;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        String intervalClassName = intent.getStringExtra(IInterval.ALARM_INTENT_INTERVAL_CLASS_KEY);
        if (null == intervalClassName) {
            App.getLogger().e(this.getClass().getName(), "Interval class name is empty");
            return;
        }
        Class intervalClass = null;
        try {
            intervalClass = Class.forName(intervalClassName);
        } catch (ClassNotFoundException e) {
            App.getLogger().d(this.getClass().getName(), e.getMessage(), e);
        }
        if (null == intervalClass) {
            return;
        }
        String intervalData = intent.getStringExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
        IInterval interval = App
                .getGson()
                .fromJson(intervalData, (Type) intervalClass);
        if (null == interval) {
            App.getLogger().d(this.getClass().getName(), "Unable to create an IInterval from JSON, empty result");
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
