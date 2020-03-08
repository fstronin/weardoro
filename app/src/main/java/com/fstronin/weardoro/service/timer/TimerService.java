package com.fstronin.weardoro.service.timer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fstronin.weardoro.R;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class TimerService extends Service {

    private final String CLASS_NAME;
    private static final int DEFAULT_FOREGROUND_ID = 765432;

    public static final int OP_TIMER_TICK = 1;
    public static final int OP_TIMER_FINISHED = 2;
    public static final int OP_TIMER_PONG = 4;

    private static final long[] VIBRATOR_PATTERN =  {0, 300, 50, 300};

    /**
     * The logger instance passing by an activity that starts the service,
     * it must be taken from an intent in the method "onStartCommand"
     */
    @Nullable  protected LoggerInterface mLogger;

    private PendingIntent mActivityPendingIntent;

    /**
     * The id will be used to make the service foreground
     */
    private int mForegroundId;

    private Vibrator mVibrator;
    private NotificationManagerCompat mNotificationManager;

    /**
     * The service builds a notification to be in a foreground mode,
     * starting from the Android 8.1 the system requires all notifications be assigned
     * to a channel.
     */
    private NotificationChannel mNotificationChannel;
    private int mNotificationContentTitle;
    private int mNotificationContentText;
    private PendingIntent mNotificationContentIntent;
    private int mNotificationSmallIconRId;

    private TimerState mState = TimerState.IDLE;
    private TimerMode mMode = TimerMode.FOCUS;
    private long mMillisFocusInterval;
    private long mMillisRestInterval;
    private long mMillisLongRestInterval;
    private int mLongRestIntervalPosition;
    private int mFocusIntervalIterationsCount;

    private PausableCountDownTimer mTimer;
    private long mMillisUntilFinished = 0L;
    private long mMillisInFuture = 0L;
    private long mCountDownInterval = 0L;

    private final ImmutableMap requiredIntentExtrasToActions = ImmutableMap.of(
            "start", Arrays.asList(
                    "countDownInterval",
                    "millisFocusInterval",
                    "millisRestInterval",
                    "millisLongRestInterval",
                    "longRestIntervalPosition",
                    "activityPendingIntent",
                    "foregroundId",
                    "notificationChannel",
                    "notificationContentTitle",
                    "notificationContentText",
                    "notificationSmallIconRId",
                    "notificationContentIntent",
                    "logger"
            ),
            "ping", Arrays.asList(
                    "activityPendingIntent",
                    "logger"
            ),
            "stop", Collections.singletonList(
                    "activityPendingIntent"
            ),
            "pause", Collections.EMPTY_LIST,
            "resume", Collections.EMPTY_LIST
    );

    public TimerService() {
        super();
        CLASS_NAME = TimerService.class.toString();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected Notification getForegroundNotification(NotificationChannel notificationChannel)
    {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, notificationChannel.getId());
        b.setOngoing(true)
                .setSmallIcon(mNotificationSmallIconRId)
                .setContentTitle(getString(mNotificationContentTitle))
                .setContentText(getString(mNotificationContentText))
                .setContentIntent(mNotificationContentIntent);
        return b.build();
    }

    protected Notification getTimerIsDoneNotification(
            NotificationChannel notificationChannel,
            String title,
            String message
    ) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, notificationChannel.getId());
        b.setSmallIcon(mNotificationSmallIconRId)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(mNotificationContentIntent);
        return b.build();
    }

    protected void onTimerStartRequested() {
        switch (mMode) {
            case FOCUS:
                mMillisInFuture = mMillisFocusInterval;
                break;
            case REST:
                mMillisInFuture = mMillisRestInterval;
                break;
            case LONG_REST:
                mMillisInFuture = mMillisLongRestInterval;
                break;
        }
        assert mLogger != null;
        mLogger.d(
                CLASS_NAME,
                String.format(
                        Locale.US,
                        "Timer start requested, millisInFuture=%d, countDownInterval=%d",
                        mMillisInFuture,
                        mCountDownInterval
                )
        );
        if (mTimer != null) {
            mTimer.cancel();
            mLogger.d(CLASS_NAME, "Found that timer reference is not empty, anyway cancelled and cleared");
        }
        mTimer = (PausableCountDownTimer) getTimer(mMillisInFuture, mCountDownInterval).start();
        mState = TimerState.RUNNING;
        try {
            onTimerPingRequested();
        } catch (Throwable e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        }
        // Go to foreground mode until onTimerStopRequest will not be received
        startForeground(mForegroundId, getForegroundNotification(mNotificationChannel));
    }

    protected void onTimerStopRequested()
    {
        assert mLogger != null;
        mLogger.d( CLASS_NAME, "Timer stop requested");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mState = TimerState.IDLE;
            mLogger.d(CLASS_NAME, "Timer cancelled and a reference cleared");
            try {
                onTimerPingRequested();
            } catch (Throwable e) {
                mLogger.e(CLASS_NAME, e.getMessage(), e);
            }
        }
        // Go to background mode and stop the service
        stopForeground(true);
        stopSelf();
    }

    protected void onTimerPauseRequested()
    {
        assert mLogger != null;
        mLogger.d(CLASS_NAME, "Timer pause requested");
        if (mTimer != null) {
            mTimer.pause();
            mState = TimerState.PAUSED;
            mLogger.d(CLASS_NAME, "Timer paused");
            try {
                onTimerPingRequested();
            } catch (Throwable e) {
                mLogger.e(CLASS_NAME, e.getMessage(), e);
            }
        } else {
            mLogger.e(CLASS_NAME, "Unable to pause the timer, reference is empty");
        }
    }

    protected void onTimerResumeRequested()
    {
        assert mLogger != null;
        mLogger.d(CLASS_NAME, "Timer resume requested");
        if (mTimer != null) {
            mTimer.resume();
            mState = TimerState.RUNNING;
            mLogger.d(CLASS_NAME, "Timer resumed");
            try {
                onTimerPingRequested();
            } catch (Throwable e) {
                mLogger.e(CLASS_NAME, e.getMessage(), e);
            }
        } else {
            mLogger.e(CLASS_NAME, "Unable to resume the timer, reference is empty");
        }
    }

    private void onTimerPingRequested()
    {
        assert mLogger != null;
        mLogger.d(CLASS_NAME, "Timer ping, state = " + mState.ordinal());
        Intent intent = (new Intent())
                .putExtra("timer-op", OP_TIMER_PONG)
                .putExtra("state", mState.ordinal())
                .putExtra("mode", mMode.ordinal())
                .putExtra("millisUntilFinished", mMillisUntilFinished)
                .putExtra("millisInFuture", mMillisInFuture);
        try {
            mActivityPendingIntent.send(TimerService.this, Activity.RESULT_OK, intent);
        } catch (PendingIntent.CanceledException e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        }

        if (mVibrator == null) {
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }

        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(this);
        }
    }

    protected void onTimerTick(long millisUntilFinished)
    {
        assert mLogger != null;
        mLogger.d(CLASS_NAME, "Timer tick, millisUntilFinished =" + millisUntilFinished);
        Intent intent = (new Intent())
                .putExtra("timer-op", OP_TIMER_TICK)
                .putExtra("millisUntilFinished", millisUntilFinished);
        mMillisUntilFinished = millisUntilFinished;
        try {
            mActivityPendingIntent.send(TimerService.this, Activity.RESULT_OK, intent);
        } catch (PendingIntent.CanceledException e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        }
    }

    protected void onTimerFinished()
    {
        assert mLogger != null;
        try {
            mLogger.d(CLASS_NAME, "Timer finish");
            Intent intent = (new Intent())
                    .putExtra("timer-op", OP_TIMER_FINISHED)
                    .putExtra("millisUntilFinished", 0L);
            mActivityPendingIntent.send(TimerService.this, Activity.RESULT_OK, intent);
        } catch (PendingIntent.CanceledException e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        } finally {
            switch (mMode) {
                case REST:
                case LONG_REST:
                    // From both REST modes it only may go to a FOCUS mode
                    mMode = TimerMode.FOCUS;
                    mMillisInFuture = mMillisFocusInterval;
                    break;
                case FOCUS:
                    // Each "mLongRestIntervalPosition" times it MUST go to a LONG REST mode
                    mFocusIntervalIterationsCount ++;
                    mMode = mFocusIntervalIterationsCount % mLongRestIntervalPosition == 0
                            ? TimerMode.LONG_REST
                            : TimerMode.REST;
                    mMillisInFuture = mMode == TimerMode.LONG_REST
                            ? mMillisLongRestInterval
                            : mMillisRestInterval;
                    break;
                default:
                    mLogger.e(CLASS_NAME, "Unknown timer mode: " + mMode.ordinal());
            }
            if (mVibrator.hasVibrator()) {
                mLogger.d(CLASS_NAME, "Vibrating!");
                mVibrator.vibrate(VibrationEffect.createWaveform(VIBRATOR_PATTERN, -1));
            } else {
                mLogger.d(CLASS_NAME, "Vibrator disconnected");
            }
            mNotificationManager.notify(
                    2,
                    getTimerIsDoneNotification(
                            mNotificationChannel,
                            "Hey, its me",
                            mMode == TimerMode.FOCUS ? "Its time to work!" : "Take a rest, babe!"
                    )
            );
            onTimerStartRequested();
        }
    }

    protected void validateIntent(Intent intent) throws RuntimeException
    {
        String action = intent.getAction();
        if (action == null) {
            throw new RuntimeException("An empty action passed");
        }
        if (!requiredIntentExtrasToActions.containsKey(action)) {
            throw new RuntimeException("An unknown action passed");
        }
        @SuppressWarnings("unchecked")
        List<String> requiredExtras = (List<String>)requiredIntentExtrasToActions.get(action);
        assert requiredExtras != null;
        for (String name:requiredExtras) {
            if (!intent.hasExtra(name)) {
                throw new RuntimeException("The required intent extra is not passed: " + name);
            }
        }
    }

    protected void takeMemberValuesFromIntent(Intent intent)
    {
        assert intent != null;
        String action = intent.getAction();
        assert action != null;
        switch (action) {
            case "start":
                mCountDownInterval = intent.getLongExtra("countDownInterval", 0L);
                mMillisFocusInterval = intent.getLongExtra("millisFocusInterval", 0L);
                mMillisRestInterval = intent.getLongExtra("millisRestInterval", 0L);
                mMillisLongRestInterval = intent.getLongExtra("millisLongRestInterval", 0L);
                mLongRestIntervalPosition = intent.getIntExtra("longRestIntervalPosition", 0);
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingIntent");
                mForegroundId = intent.getIntExtra("foregroundId", DEFAULT_FOREGROUND_ID);
                mNotificationChannel = intent.getParcelableExtra("notificationChannel");
                mNotificationContentTitle = intent.getIntExtra("notificationContentTitle", 0);
                mNotificationContentText = intent.getIntExtra("notificationContentText", 0);
                mNotificationSmallIconRId = intent.getIntExtra("notificationSmallIconRId", 0);
                mNotificationContentIntent = intent.getParcelableExtra("notificationContentIntent");
                mLogger = intent.getParcelableExtra("logger");
                break;
            case "stop":
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingIntent");
                break;
            case "ping":
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingIntent");
                mLogger = intent.getParcelableExtra("logger");
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) throws RuntimeException {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        validateIntent(intent);
        takeMemberValuesFromIntent(intent);

        String action = intent.getAction();
        assert action != null;
        try {
            switch (action) {
                case "start":
                    onTimerStartRequested();
                    break;
                case "stop":
                    onTimerStopRequested();
                    break;
                case "pause":
                    onTimerPauseRequested();
                    break;
                case "resume":
                    onTimerResumeRequested();
                    break;
                case "ping":
                    onTimerPingRequested();
                    break;
            }
        } catch (Throwable e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        }

        return START_STICKY;
    }

    protected PausableCountDownTimer getTimer(long millisInFuture, long countDownInterval)
    {
        assert mLogger != null;
        return new PausableCountDownTimer(millisInFuture, countDownInterval, mLogger) {
            @Override
            public void onTick(long millisUntilFinished) {
                try {
                    TimerService.this.onTimerTick(millisUntilFinished);
                } catch (Throwable e) {
                    mLogger.e(CLASS_NAME, e.getMessage(), e);
                }
            }
            @Override
            public void onFinish() {
                TimerService.this.onTimerFinished();
            }
        };
    }
}
