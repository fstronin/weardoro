package com.fstronin.weardoro.interval;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.fstronin.weardoro.AlarmReceiver;

public class AlarmPendingIntentBuilder implements Parcelable
{
    private final int REQUEST_CODE = 1;
    private final int FLAGS = PendingIntent.FLAG_UPDATE_CURRENT;

    public AlarmPendingIntentBuilder()  {}
    public AlarmPendingIntentBuilder(Parcel in)  {}

    public static final Creator<AlarmPendingIntentBuilder> CREATOR = new Creator<AlarmPendingIntentBuilder>() {
        @Override
        public AlarmPendingIntentBuilder createFromParcel(Parcel in) {
            return new AlarmPendingIntentBuilder(in);
        }

        @Override
        public AlarmPendingIntentBuilder[] newArray(int size) {
            return new AlarmPendingIntentBuilder[size];
        }
    };

    private Intent buildAlarmIntent(Context ctx, IInterval interval)
    {
        Bundle intervalContainer = new Bundle();
        // Alarm Manager intents do not support custom parcelable objects
        intervalContainer.putParcelable(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY, interval);
        return (new Intent(ctx, AlarmReceiver.class))
                .setAction(IInterval.ALARM_INTENT_ACTION_INTERVAL_FINISHED)
                .putExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY, intervalContainer);
    }

    public PendingIntent build(Context ctx, IInterval interval)
    {
        return PendingIntent.getBroadcast(ctx, REQUEST_CODE, buildAlarmIntent(ctx, interval), FLAGS);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }
}
