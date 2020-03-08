package com.fstronin.weardoro.container;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

import com.fstronin.weardoro.R;
import com.fstronin.weardoro.logging.Logger;
import com.fstronin.weardoro.logging.LoggerInterface;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Base implements IContainer
{
    private Map<Context, NotificationManagerCompat> mNotificationManagers = new HashMap<>();
    private NotificationChannel mNotificationChannel;
    private LoggerInterface mLogger;
    private long mMillisFocusInterval;
    private long mMillisRestInterval;
    private long mMillisLongRestInterval;
    private int mLongRestIntervalPosition;
    private int mServiceForegroundId;
    private SimpleDateFormat mTimerClockFormat;

    @Override
    public NotificationManagerCompat getNotificationManager(Context ctx)
    {
        if (!mNotificationManagers.containsKey(ctx)) {
            mNotificationManagers.put(ctx, NotificationManagerCompat.from(ctx));
        }
        return mNotificationManagers.get(ctx);
    }

    @Override
    public NotificationChannel getNotificationChannel(Context ctx)
    {
        if (null == mNotificationChannel) {
            mNotificationChannel = new NotificationChannel(
                    ctx.getString(R.string.notification_channel_id),
                    ctx.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getNotificationManager(ctx).createNotificationChannel(mNotificationChannel);
        }
        return mNotificationChannel;
    }

    @Override
    public LoggerInterface getLogger() {
        if (null == mLogger) {
            mLogger = new Logger();
        }
        return mLogger;
    }

    private long getMillisFromStringMinutes(String minutes)
    {
        return Long.parseLong(minutes) * 60L * 1000L;
    }

    @Override
    public long getMillisFocusInterval(Context ctx)
    {
        if (0 == mMillisFocusInterval) {
            mMillisFocusInterval = getMillisFromStringMinutes(ctx.getString(R.string.timer_interval_focus_minutes));
        }
        return mMillisFocusInterval;
    }

    @Override
    public long getMillisRestInterval(Context ctx)
    {
        if (0 == mMillisRestInterval) {
            mMillisRestInterval = getMillisFromStringMinutes(ctx.getString(R.string.timer_interval_rest_minutes));
        }
        return mMillisRestInterval;
    }

    @Override
    public long getMillisLongRestInterval(Context ctx)
    {
        if (0 == mMillisLongRestInterval) {
            mMillisLongRestInterval = getMillisFromStringMinutes(ctx.getString(R.string.timer_interval_long_rest_minutes));
        }
        return mMillisLongRestInterval;
    }

    @Override
    public int getLongRestIntervalPosition(Context ctx) {
        if (0 == mLongRestIntervalPosition) {
            mLongRestIntervalPosition = Integer.parseInt(ctx.getString(R.string.timer_interval_long_rest_position));
        }
        return mLongRestIntervalPosition;
    }

    @Override
    public long getMillisCountDownInterval(Context ctx)
    {
        return 1000L;
    }

    @Override
    public int getServiceForegroundId(Context ctx)
    {
        if (0 == mServiceForegroundId) {
            mServiceForegroundId = Integer.parseInt(ctx.getString(R.string.service_foreground_id));
        }
        return mServiceForegroundId;
    }

    private Locale getLocale()
    {
        return Locale.US;
    }

    @Override
    public DateFormat getTimerClockFormat(Context ctx)
    {
        if (null == mTimerClockFormat) {
            mTimerClockFormat = new SimpleDateFormat(ctx.getString(R.string.format_timer_clock), getLocale());
        }
        return mTimerClockFormat;
    }
}
