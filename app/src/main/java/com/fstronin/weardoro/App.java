package com.fstronin.weardoro;

import android.app.NotificationChannel;
import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

import com.fstronin.weardoro.container.Base;
import com.fstronin.weardoro.container.IContainer;
import com.fstronin.weardoro.logging.LoggerInterface;

import java.text.DateFormat;

public class App
{
    private static IContainer mContainer = new Base();

    public static void setContainer(IContainer container)
    {
        mContainer = container;
    }

    public static NotificationManagerCompat getNotificationManager(Context ctx)
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
}
