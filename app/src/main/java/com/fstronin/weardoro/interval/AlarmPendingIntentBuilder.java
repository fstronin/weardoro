package com.fstronin.weardoro.interval;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fstronin.weardoro.AlarmReceiver;
import com.fstronin.weardoro.App;

public class AlarmPendingIntentBuilder implements Cloneable
{
    private int mRequestCode;
    private int mFlags;

    public AlarmPendingIntentBuilder(int flags)
    {
        init(0, flags);
    }

    public AlarmPendingIntentBuilder()
    {
        init(0, 0);
    }

    public AlarmPendingIntentBuilder(AlarmPendingIntentBuilder source)
    {
        init(source.getRequestCode(), source.getFlags());
    }

    private void init(int requestCode, int flags)
    {
        mRequestCode = requestCode;
        mFlags = flags;
    }

    public int getRequestCode()
    {
        return mRequestCode;
    }

    public int getFlags()
    {
        return mFlags;
    }

    private Intent buildAlarmIntent(Context ctx, IInterval interval)
    {
        String className = interval.getClass().getName();
        return  (new Intent(ctx, AlarmReceiver.class))
                .setAction(IInterval.ALARM_INTENT_ACTION_INTERVAL_FINISHED)
                .putExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY, App.getGson().toJson(interval))
                .putExtra(IInterval.ALARM_INTENT_INTERVAL_CLASS_KEY, className);
    }

    public PendingIntent build(Context ctx, IInterval interval)
    {
        mRequestCode ++;
        if (mRequestCode == Integer.MAX_VALUE) {
            mRequestCode = 1;
        }
        return PendingIntent.getBroadcast(ctx, mRequestCode, buildAlarmIntent(ctx, interval), mFlags);
    }
}
