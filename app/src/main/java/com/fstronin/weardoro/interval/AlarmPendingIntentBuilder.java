package com.fstronin.weardoro.interval;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmPendingIntentBuilder
{
    private int mRequestCode;
    private Intent mIntent;
    private int mFlags;

    public AlarmPendingIntentBuilder setRequestCode(int requestCode)
    {
        mRequestCode = requestCode;
        return this;
    }

    public AlarmPendingIntentBuilder setIntent(Intent intent)
    {
        mIntent = intent;
        return this;
    }

    public AlarmPendingIntentBuilder setFlags(int flags)
    {
        mFlags = flags;
        return this;
    }

    public PendingIntent build(Context ctx)
    {
        return PendingIntent.getBroadcast(ctx, mRequestCode, mIntent, mFlags);
    }
}
