package com.fstronin.weardoro;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.fstronin.weardoro.container.Base;
import com.fstronin.weardoro.container.IContainer;
import com.fstronin.weardoro.interval.IntervalBuilder;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Locale;

public class App
{
    private static IContainer mContainer = new Base();

    public static void setContainer(IContainer container)
    {
        mContainer = container;
    }

    public static NotificationManager getNotificationManager(Context ctx)
    {
        return mContainer.getNotificationManager(ctx);
    }

    public static NotificationChannel getNotificationChannel(Context ctx)
    {
        return mContainer.getNotificationChannel(ctx);
    }

    public static LoggerInterface getLogger()
    {
        return mContainer.getLogger();
    }

    public static long getMillisFocusInterval(Context ctx)
    {
        return mContainer.getMillisFocusInterval(ctx);
    }

    public static long getMillisRestInterval(Context ctx)
    {
        return mContainer.getMillisRestInterval(ctx);
    }

    public static long getMillisLongRestInterval(Context ctx)
    {
        return mContainer.getMillisLongRestInterval(ctx);
    }

    public static long getMillisCountDownInterval(Context ctx)
    {
        return mContainer.getMillisCountDownInterval(ctx);
    }

    public static int getLongRestIntervalPosition(Context ctx)
    {
        return mContainer.getLongRestIntervalPosition(ctx);
    }

    public static int getServiceForegroundId(Context ctx)
    {
        return mContainer.getServiceForegroundId(ctx);
    }

    public static DateFormat getTimerClockFormat(Context ctx)
    {
        return mContainer.getTimerClockFormat(ctx);
    }

    public static AlarmManager getAlarmManager(Context ctx)
    {
        return mContainer.getAlarmManager(ctx);
    }

    public static int getAlarmType()
    {
        return mContainer.getAlarmType();
    }

    public static Locale getLocale()
    {
        return mContainer.getLocale();
    }

    public static Gson getGson()
    {
        return mContainer.getGson();
    }

    public static SharedPreferences getSharedPreferences(Context ctx)
    {
        return mContainer.getSharedPreferences(ctx);
    }

    public static IntervalBuilder getIntervalBuilder()
    {
        return mContainer.getIntervalBuilder();
    }
}
