package com.fstronin.weardoro.container;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.fstronin.weardoro.R;
import com.fstronin.weardoro.interval.Interval;
import com.fstronin.weardoro.interval.IntervalBuilder;
import com.fstronin.weardoro.logging.Logger;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Base implements IContainer
{
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private LoggerInterface mLogger;
    private long mMillisFocusInterval;
    private long mMillisRestInterval;
    private long mMillisLongRestInterval;
    private int mLongRestIntervalPosition;
    private int mServiceForegroundId;
    private SimpleDateFormat mTimerClockFormat;
    private AlarmManager mAlarmManager;
    private Gson mGson;
    private SharedPreferences mSharedPreferences;

    @Override
    public NotificationManager getNotificationManager(Context ctx)
    {
        if (null == mNotificationManager) {
            mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
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
        return 200L;
    }

    @Override
    public int getServiceForegroundId(Context ctx)
    {
        if (0 == mServiceForegroundId) {
            mServiceForegroundId = Integer.parseInt(ctx.getString(R.string.service_foreground_id));
        }
        return mServiceForegroundId;
    }

    public Locale getLocale()
    {
        return Locale.US;
    }

    @Override
    public DateFormat getTimerClockFormat(Context ctx)
    {
        if (null == mTimerClockFormat) {
            mTimerClockFormat = new SimpleDateFormat(ctx.getString(R.string.format_timer_clock), getLocale());
        }
        return (DateFormat) mTimerClockFormat.clone();
    }

    @Override
    public AlarmManager getAlarmManager(Context ctx)
    {
        if (null == mAlarmManager) {
            mAlarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        }
        return mAlarmManager;
    }

    @Override
    public int getAlarmType()
    {
        return AlarmManager.RTC_WAKEUP;
    }

    @Override
    public Gson getGson()
    {
        if (null == mGson)
        {
            mGson = new Gson();
        }
        return mGson;
    }

    public SharedPreferences getSharedPreferences(Context ctx)
    {
        if (null == mSharedPreferences) {
            mSharedPreferences = ctx.getSharedPreferences("com.fstronin.weardoro.preferences", Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public IntervalBuilder getIntervalBuilder()
    {
        return new IntervalBuilder();
    }
}
