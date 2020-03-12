package com.fstronin.weardoro;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class CounterStorage
{
    private final String FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY = "focus-interval-count-timestamp-key";
    private final String FOCUS_INTERVAL_COUNT_VALUE_KEY = "focus-interval-count-value-key";

    public int getFocusIntervalCount(Context context)
    {
        SharedPreferences sp = App.getSharedPreferences(context);
        ZoneId currentZone = ZoneId.systemDefault();
        long currentTimeMillis = System.currentTimeMillis();
            Instant lastUpdateInstant = sp.contains(FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY)
                    ? Instant.ofEpochMilli(sp.getLong(FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY, currentTimeMillis))
                    : Instant.now();
            LocalDate lastUpdateDate = lastUpdateInstant.atZone(currentZone).toLocalDate();
            LocalDate currentDate = Instant.now().atZone(currentZone).toLocalDate();
            return lastUpdateDate.isEqual(currentDate)
                    ?  sp.getInt(FOCUS_INTERVAL_COUNT_VALUE_KEY, 0)
                    : 0;
    }

    public void incrementFocusIntervalCount(Context context)
    {
        SharedPreferences.Editor spEditor = App.getSharedPreferences(context).edit();
        spEditor.putLong(FOCUS_INTERVAL_COUNT_TIMESTAMP_KEY, System.currentTimeMillis());
        spEditor.putInt(FOCUS_INTERVAL_COUNT_VALUE_KEY, getFocusIntervalCount(context) + 1);
        spEditor.apply();
    }
}
