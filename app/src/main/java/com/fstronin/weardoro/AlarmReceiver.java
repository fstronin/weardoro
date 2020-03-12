package com.fstronin.weardoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.fstronin.weardoro.interval.IInterval;
import com.fstronin.weardoro.interval.IntervalException;
import com.fstronin.weardoro.interval.Type;

public class AlarmReceiver extends BroadcastReceiver
{
    private final long[] VIBRATION_PATTERN = { 0, 200, 50, 200 };
    private final int[] VIBRATION_AMPLITUDES = {0, 255, 10, 255};

    @Override
    public void onReceive(Context context, Intent intent) {
        beep(context);
        String action = intent.getAction();
        if (null == action) {
            App.getLogger().e(this.getClass().getName(), "Empty action");
            return;
        }
        Bundle intervalContainer = intent.getBundleExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
        if (null == intervalContainer) {
            App.getLogger().e(this.getClass().getName(), "Unable to obtain an interval container from an intent");
            return;
        }
        IInterval interval = intervalContainer.getParcelable(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
        if (null == interval) {
            App.getLogger().e(this.getClass().getName(), "Unable to obtain interval instance from an intent");
            return;
        }
        if (interval.getType() == Type.FOCUS) {
            App.getCounterStorage().incrementFocusIntervalCount(context);
        }
        IInterval nextInterval = interval.getNext();
        try {
            nextInterval.start(context);
        } catch (IntervalException e) {
            App.getLogger().e(this.getClass().getName(), e.getMessage(), e);
        }
    }

    private void beep(Context ctx)
    {
        Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (null != vibrator && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_AMPLITUDES, -1));
        }
        Intent intent = (new Intent(ctx, MainActivity.class))
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }
}
