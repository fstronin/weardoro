package com.fstronin.weardoro.container;

import android.app.NotificationChannel;
import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

import com.fstronin.weardoro.logging.LoggerInterface;

import java.text.DateFormat;

public interface IContainer
{
    NotificationManagerCompat getNotificationManager(Context ctx);
    NotificationChannel getNotificationChannel(Context ctx);
    LoggerInterface getLogger();
    long getMillisFocusInterval(Context ctx);
    long getMillisRestInterval(Context ctx);
    long getMillisLongRestInterval(Context ctx);
    int getLongRestIntervalPosition(Context ctx);
    long getMillisCountDownInterval(Context ctx);
    int getServiceForegroundId(Context ctx);
    DateFormat getTimerClockFormat(Context ctx);
}
