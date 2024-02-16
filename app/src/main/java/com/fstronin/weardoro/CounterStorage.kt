package com.fstronin.weardoro

import android.content.Context
import com.fstronin.weardoro.App.getSharedPreferences
import java.time.Instant
import java.time.ZoneId

class CounterStorage {
    private val FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY = "focus-interval-count-timestamp-key"
    private val FOCUS_INTERVAL_COUNT_VALUE_KEY = "focus-interval-count-value-key"
    fun getFocusIntervalCount(context: Context?): Int {
        val sp = getSharedPreferences(context)
        val currentZone = ZoneId.systemDefault()
        val currentTimeMillis = System.currentTimeMillis()
        val lastUpdateInstant =
            if (sp.contains(FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY)) Instant.ofEpochMilli(
                sp.getLong(
                    FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY,
                    currentTimeMillis
                )
            ) else Instant.now()
        val lastUpdateDate = lastUpdateInstant.atZone(currentZone).toLocalDate()
        val currentDate = Instant.now().atZone(currentZone).toLocalDate()
        return if (lastUpdateDate.isEqual(currentDate)) sp.getInt(
            FOCUS_INTERVAL_COUNT_VALUE_KEY,
            0
        ) else 0
    }

    fun incrementFocusIntervalCount(context: Context?) {
        val spEditor = getSharedPreferences(context).edit()
        spEditor.putLong(FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY, System.currentTimeMillis())
        spEditor.putInt(FOCUS_INTERVAL_COUNT_VALUE_KEY, getFocusIntervalCount(context) + 1)
        spEditor.apply()
    }
}
