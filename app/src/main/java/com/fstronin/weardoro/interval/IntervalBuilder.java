package com.fstronin.weardoro.interval;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.fstronin.weardoro.App;

public class IntervalBuilder
{
    @Nullable
    public IInterval fromSharedPreferences(SharedPreferences sp, String classKey, String dataKey) {
        String className = this.getClass().getName();
        Class<Interval> intervalClass = null;
        try {
            String intervalClassName = sp.getString(classKey, "");
            if (intervalClassName.length() > 0) {
                App.getLogger().d(className, "Trying to load an interval instance from shared preferences, class name = " + intervalClassName);
                intervalClass = (Class<Interval>) Class.forName(intervalClassName);
            }
        } catch (ClassNotFoundException e) {
            App.getLogger().e(className, e.getMessage(), e);
        }
        String intervalData = sp.getString(dataKey, "");
        IInterval result = null;
        if (null != intervalClass && intervalData.length() > 0) {
            App.getLogger().d(className, "Parsing the " + intervalClass.getName() + " data from JSON and going to create an instance");
            result = App.getGson().fromJson(intervalData, intervalClass);
        }
        return result;
    }
}
