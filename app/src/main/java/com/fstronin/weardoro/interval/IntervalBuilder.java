package com.fstronin.weardoro.interval;

import android.content.SharedPreferences;

import com.fstronin.weardoro.App;

public class IntervalBuilder
{
    public IInterval fromSharedPreferences(SharedPreferences sp, String classKey, String dataKey, AlarmPendingIntentBuilder alarmIntentBuilder) {
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
        IInterval result;
        if (null == intervalClass || intervalData.length() == 0) {
            App.getLogger().d(className, "Empty class found in preferences or empty data, will build a default FocusInterval instance");
            result = new FocusInterval(alarmIntentBuilder);
        } else {
            App.getLogger().d(className, "Parsing the " + intervalClass.getName() + " data from JSON and going to create an instance");
            result = App.getGson().fromJson(intervalData, intervalClass);
        }
        return result;
    }
}
