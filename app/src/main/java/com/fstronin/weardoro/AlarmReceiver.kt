package com.fstronin.weardoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import com.fstronin.weardoro.App.counterStorage
import com.fstronin.weardoro.App.logger
import com.fstronin.weardoro.interval.IInterval
import com.fstronin.weardoro.interval.IntervalException
import com.fstronin.weardoro.interval.Type

class AlarmReceiver : BroadcastReceiver() {
    private val VIBRATION_PATTERN = longArrayOf(0, 200, 50, 200)
    private val VIBRATION_AMPLITUDES = intArrayOf(0, 255, 10, 255)
    override fun onReceive(context: Context, intent: Intent) {
        beep(context)
        val action = intent.action
        if (null == action) {
            logger.e(this.javaClass.name, "Empty action")
            return
        }
        val intervalContainer = intent.getBundleExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY)
        if (null == intervalContainer) {
            logger.e(this.javaClass.name, "Unable to obtain an interval container from an intent")
            return
        }
        val interval =
            intervalContainer.getParcelable<IInterval>(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY)
        if (null == interval) {
            logger.e(this.javaClass.name, "Unable to obtain interval instance from an intent")
            return
        }
        if (interval.type == Type.FOCUS) {
            counterStorage.incrementFocusIntervalCount(context)
        }
        val nextInterval = interval.next
        try {
            nextInterval.start(context)
        } catch (e: IntervalException) {
            logger.e(this.javaClass.name, e.message, e)
        }
    }

    private fun beep(ctx: Context) {
        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (null != vibrator && vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    VIBRATION_PATTERN,
                    VIBRATION_AMPLITUDES,
                    -1
                )
            )
        }
        val intent = Intent(ctx, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }
}
