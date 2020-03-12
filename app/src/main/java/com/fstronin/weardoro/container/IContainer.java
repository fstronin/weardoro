package com.fstronin.weardoro.container;

import java.text.DateFormat;
import java.util.Locale;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.fstronin.weardoro.CounterStorage;
import com.fstronin.weardoro.interval.IntervalBuilder;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.google.gson.Gson;

public interface IContainer
{
    NotificationManager getNotificationManager(Context ctx);
    NotificationChannel getNotificationChannel(Context ctx);
    LoggerInterface getLogger();
    long getMillisFocusInterval(Context ctx);
    long getMillisRestInterval(Context ctx);
    long getMillisLongRestInterval(Context ctx);
    int getLongRestIntervalPosition(Context ctx);
    long getMillisCountDownInterval(Context ctx);
    int getServiceForegroundId(Context ctx);
    DateFormat getTimerClockFormat(Context ctx);
    AlarmManager getAlarmManager(Context ctx);
    int getAlarmType();
    Locale getLocale();
    Gson getGson();
    SharedPreferences getSharedPreferences(Context ctx);
    IntervalBuilder getIntervalBuilder();
    CounterStorage getCounterStorage();
}
