package com.fstronin.weardoro.service.timer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fstronin.weardoro.logging.LoggerInterface;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TimerService extends Service {

    protected final String CLASS_NAME;
    private final int DEFAULT_FOREGROUND_ID = 765432;

    public final int OP_TIMER_TICK = 1;
    public final int OP_TIMER_FINISHED = 2;

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

    /**
     * The service builds a notification to be in a foreground mode,
     * starting from the Android 8.1 the system requires all notifications be assigned
     * to a channel.
     */
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private String mNotificationContentTitle;
    private String mNotificationContentText;
    private PendingIntent mNotificationContentIntent;
    private int mNotificationSmallIconRId;

    private PausableCountDownTimer mTimer;

    protected final ImmutableMap requiredIntentExtrasToActions = ImmutableMap.of(
            "start", Arrays.asList(
                    "millisInFuture",
                    "countDownInterval",
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

    protected Notification getForegroundNotification(NotificationChannel notificationChannel) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, notificationChannel.getId());
        b.setOngoing(true)
                .setSmallIcon(mNotificationSmallIconRId)
                .setContentTitle(mNotificationContentTitle)
                .setContentText(mNotificationContentText)
                .setContentIntent(mNotificationContentIntent);
        return b.build();
    }

    protected void onTimerStartRequested(long millisInFuture, long countDownInterval) {
        assert mLogger != null;
        mLogger.d(
                CLASS_NAME,
                String.format(
                        Locale.US,
                        "Timer start requested, millisInFuture=%d, countDownInterval=%d",
                        millisInFuture,
                        countDownInterval
                )
        );
        if (mTimer != null) {
            mTimer.cancel();
            mLogger.d(CLASS_NAME, "Found that timer reference is not empty, anyway cancelled and cleared");
        }
        mTimer = (PausableCountDownTimer) getTimer(millisInFuture, countDownInterval).start();
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
            mLogger.d(CLASS_NAME, "Timer cancelled and a reference cleared");
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
            mLogger.d(CLASS_NAME, "Timer paused");
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
            mLogger.d(CLASS_NAME, "Timer resumed");
        } else {
            mLogger.e(CLASS_NAME, "Unable to resume the timer, reference is empty");
        }
    }

    protected void onTimerTick(long millisUntilFinished)
    {
        assert mLogger != null;
        try {
            mLogger.d(CLASS_NAME, "Timer tick, millisUntilFinished =" + millisUntilFinished);
            Intent intent = new Intent();
            intent.putExtra("timer-event", OP_TIMER_TICK);
            intent.putExtra("millisUntilFinished", millisUntilFinished);
            mActivityPendingIntent.send(TimerService.this, Activity.RESULT_OK, intent);
        } catch (Throwable e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        }
    }

    protected void onTimerFinished()
    {
        assert mLogger != null;
        try {
            mLogger.d(CLASS_NAME, "Timer finish");
            Intent intent = new Intent();
            intent.putExtra("timer-event", OP_TIMER_FINISHED);
            intent.putExtra("millisUntilFinished", 0L);
            mActivityPendingIntent.send(TimerService.this, Activity.RESULT_OK, intent);
        } catch (Throwable e) {
            mLogger.e(CLASS_NAME, e.getMessage(), e);
        } finally {
            onTimerStopRequested();
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
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingIntent");
                mForegroundId = intent.getIntExtra("foregroundId", DEFAULT_FOREGROUND_ID);
                mNotificationChannel = intent.getParcelableExtra("notificationChannel");
                mNotificationContentTitle = intent.getStringExtra("notificationContentTitle");
                mNotificationContentText = intent.getStringExtra("notificationContentText");
                mNotificationSmallIconRId = intent.getIntExtra("notificationSmallIconRId", 0);
                mNotificationContentIntent = intent.getParcelableExtra("notificationContentIntent");
                mLogger = intent.getParcelableExtra("logger");
                break;
            case "stop":
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingResult");
                break;
            case "ping":
                mActivityPendingIntent = intent.getParcelableExtra("activityPendingIntent");
                mLogger = intent.getParcelableExtra("logger");
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) throws RuntimeException {
        if (intent == null) {
            throw new RuntimeException("The service is unable to starts without an intent");
        }

        validateIntent(intent);
        takeMemberValuesFromIntent(intent);

        // The notification manager is not a Parcelable instance so must be taken from
        // the system service locator instead of injection through an intent.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String action = intent.getAction();
        assert action != null;
        switch (action) {
            case "start":
                onTimerStartRequested(
                        intent.getLongExtra("millisInFuture", 0),
                        intent.getLongExtra("countDownInterval", 0)
                );
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
        }

        return START_STICKY;
    }

    protected PausableCountDownTimer getTimer(long millisInFuture, long countDownInterval)
    {
        assert mLogger != null;
        return new PausableCountDownTimer(millisInFuture, countDownInterval, mLogger) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimerService.this.onTimerTick(millisUntilFinished);
            }
            @Override
            public void onFinish() {
                TimerService.this.onTimerFinished();
            }
        };
    }
}
