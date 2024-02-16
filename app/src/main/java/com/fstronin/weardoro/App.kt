package com.fstronin.weardoro

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.fstronin.weardoro.container.Base
import com.fstronin.weardoro.container.IContainer
import com.fstronin.weardoro.interval.IntervalBuilder
import com.fstronin.weardoro.logging.LoggerInterface
import com.google.gson.Gson
import java.text.DateFormat
import java.util.Locale

object App {
    private var mContainer: IContainer = Base()
    fun setContainer(container: IContainer) {
        mContainer = container
    }

    fun getNotificationManager(ctx: Context?): NotificationManager {
        return mContainer.getNotificationManager(ctx)
    }

    fun getNotificationChannel(ctx: Context?): NotificationChannel {
        return mContainer.getNotificationChannel(ctx)
    }

    @JvmStatic
    val logger: LoggerInterface
        get() = mContainer.logger

    fun getMillisFocusInterval(ctx: Context?): Long {
        return mContainer.getMillisFocusInterval(ctx)
    }

    fun getMillisRestInterval(ctx: Context?): Long {
        return mContainer.getMillisRestInterval(ctx)
    }

    fun getMillisLongRestInterval(ctx: Context?): Long {
        return mContainer.getMillisLongRestInterval(ctx)
    }

    @JvmStatic
    fun getMillisCountDownInterval(ctx: Context?): Long {
        return mContainer.getMillisCountDownInterval(ctx)
    }

    fun getLongRestIntervalPosition(ctx: Context?): Int {
        return mContainer.getLongRestIntervalPosition(ctx)
    }

    fun getServiceForegroundId(ctx: Context?): Int {
        return mContainer.getServiceForegroundId(ctx)
    }

    @JvmStatic
    fun getTimerClockFormat(ctx: Context?): DateFormat {
        return mContainer.getTimerClockFormat(ctx)
    }

    @JvmStatic
    fun getAlarmManager(ctx: Context?): AlarmManager {
        return mContainer.getAlarmManager(ctx)
    }

    @JvmStatic
    val alarmType: Int
        get() = mContainer.alarmType
    @JvmStatic
    val locale: Locale
        get() = mContainer.locale
    @JvmStatic
    val gson: Gson
        get() = mContainer.gson

    @JvmStatic
    fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return mContainer.getSharedPreferences(ctx)
    }

    @JvmStatic
    val intervalBuilder: IntervalBuilder
        get() = mContainer.intervalBuilder
    @JvmStatic
    val counterStorage: CounterStorage
        get() = mContainer.counterStorage
}
