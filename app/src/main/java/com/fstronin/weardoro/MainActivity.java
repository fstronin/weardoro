package com.fstronin.weardoro;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.fstronin.weardoro.logging.Logger;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.fstronin.weardoro.service.timer.TimerMode;
import com.fstronin.weardoro.service.timer.TimerService;
import com.fstronin.weardoro.service.timer.TimerState;

public class MainActivity extends WearableActivity {

    private static final int MAIN_TIMER_SERVICE_REQUEST_CODE = 1;
    private static final int FOREGROUND_ID = 1;
    private static final long DEFAULT_FOCUS_MILLIS = 1000L * 60L * 25L;
    private static final long DEFAULT_REST_MILLIS = 1000L * 60L * 5L;
    private static final long DEFAULT_LONG_REST_MILLIS = 1000L * 60L * 15L;
     /*
    private static final long DEFAULT_FOCUS_MILLIS = 1000L * 25L;
    private static final long DEFAULT_REST_MILLIS = 1000L * 5L;
    private static final long DEFAULT_LONG_REST_MILLIS = 1000L * 15L;
    */
    private static final int DEFAULT_LONG_REST_ITERATION_DIVIDER = 4;
    private String mClassName;
    private TextView mTopTextView;
    private TextView mBottomTextView;
    private Button mActionBtn;
    private TimerArc mTimerArc;
    private DateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
    private LoggerInterface mLogger = new Logger();
    private final Intent mEmptyIntent = new Intent();
    private NotificationManagerCompat mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private long mMillisUntilFinished = 0L;
    private long mMillisInFuture = 0L;
    private long mCountDownInterval = 0L;
    private long mMillisFocusInterval = DEFAULT_FOCUS_MILLIS;
    private long mMillisRestInterval = DEFAULT_REST_MILLIS;
    private long mMillisLongRestInterval = DEFAULT_LONG_REST_MILLIS;
    private int mLongRestIntervalPosition = DEFAULT_LONG_REST_ITERATION_DIVIDER;
    private TimerState mTimerState = TimerState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClassName = MainActivity.class.getName();
        mCountDownInterval = Long.parseLong(getString(R.string.timer_interval_millis));

        mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationChannel = buildNotificationChannel();
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        mTopTextView = (TextView) findViewById(R.id.text_view_top);
        mBottomTextView = (TextView) findViewById(R.id.text_view_bottom);

        mActionBtn = (Button) findViewById(R.id.actionBtn);
        mActionBtn.setOnClickListener(this::onActionBtnClick);
        mActionBtn.setOnLongClickListener(this::onActionBtnLongClick);

        mTimerArc = (TimerArc) findViewById((R.id.timerArc));
    }

    private NotificationChannel buildNotificationChannel()
    {
        return new NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
    }

    @Override
    public void onStart()
    {
        super.onStart();
        sendTimerPing();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    private void onActionBtnClick(View v)
    {
        switch (mTimerState) {
            case IDLE:
                onTimerStartRequested();
                break;
            case RUNNING:
                onTimerPauseRequested();
                break;
            case PAUSED:
                onTimerResumeRequested();
                break;
            default:
                mLogger.e(mClassName, "Unsupported timer state detected onActionBtnClick");
        }
    }

    private void sendTimerPing()
    {
        Intent intent = (new Intent(this, TimerService.class))
                .setAction("ping")
                .putExtra(
                        "activityPendingIntent",
                        createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
                )
                .putExtra("logger", mLogger);
        startService(intent);
    }

    private void onTimerPongCOMMON(
            TimerMode mode,
            long millisUntilFinished,
            long millisInFuture,
            int iterationNum
    ) {
        mMillisInFuture = millisInFuture;
        updateTimerArc(mMillisUntilFinished);
    }

    private void onTimerPongIDLE(
            TimerMode mode,
            long millisUntilFinished,
            long millisInFuture,
            int iterationNum
    ) {
        mActionBtn.setText(R.string.text_start);
        mMillisInFuture = 0;
        setMillisUntilFinished(0);

        mTopTextView.setText(R.string.text_greetings);

        mBottomTextView.setText("");
        mBottomTextView.setVisibility(View.INVISIBLE);
    }

    private void onTimerPongRUNNING(
            TimerMode mode,
            long millisUntilFinished,
            long millisInFuture,
            int iterationNum
    ) {
        mActionBtn.setText(R.string.text_pause);
        setMillisUntilFinished(millisUntilFinished);
        int timerModeTextId = mode == TimerMode.FOCUS
                ? R.string.text_timer_mode_focus
                : R.string.text_timer_mode_rest;
        mBottomTextView.setVisibility(View.VISIBLE);
        mBottomTextView.setText(timerModeTextId);
    }

    private void onTimerPongPAUSED(
            TimerMode mode,
            long millisUntilFinished,
            long mMillisInFuture,
            int iterationNum
    )
    {
        mActionBtn.setText(R.string.text_resume);
        setMillisUntilFinished(millisUntilFinished);
    }

    private void onTimerPong(
            TimerState state,
            TimerMode mode,
            long millisUntilFinished,
            long millisInFuture,
            int iterationNum
    )
    {
        mLogger.d(
                mClassName,
                String.format(
                        Locale.US,
                        "Timer pong, state=%d, mode=%d, millisUntilFinished=%d, millisInFuture=%d, ",
                        state.ordinal(),
                        mode.ordinal(),
                        millisUntilFinished,
                        millisInFuture
                )
        );
        if (state == TimerState.CORRUPTED) {
            mLogger.e(mClassName, "TimerState CORRUPTED");
            return;
        }
        mTimerState = state;
        if (mode == TimerMode.CORRUPTED) {
            mLogger.e(mClassName, "TimerMode CORRUPTED");
            return;
        }
        onTimerPongCOMMON(mode, millisUntilFinished, millisInFuture, iterationNum);
        switch (state) {
            case IDLE:
                onTimerPongIDLE(mode, millisUntilFinished, millisInFuture, iterationNum);
                break;
            case RUNNING:
                onTimerPongRUNNING(mode, millisUntilFinished, millisInFuture, iterationNum);
                break;
            case PAUSED:
                onTimerPongPAUSED(mode, millisUntilFinished, millisInFuture, iterationNum);
                break;
            default:
                mLogger.e(mClassName, "Unsupported timer state detected onTimerPong");
        }
    }

    private void onTimerTick(long millisUntilFinished)
    {
        setMillisUntilFinished(millisUntilFinished);
    }

    private Intent buildTimerStartIntent()
    {
        Intent intent = (new Intent(this, TimerService.class))
                .setAction("start")
                .putExtra("countDownInterval", mCountDownInterval)
                .putExtra("millisFocusInterval", mMillisFocusInterval)
                .putExtra("millisRestInterval", mMillisRestInterval)
                .putExtra("millisLongRestInterval", mMillisLongRestInterval)
                .putExtra("longRestIntervalPosition", mLongRestIntervalPosition)
                .putExtra("foregroundId", FOREGROUND_ID)
                .putExtra("notificationChannel", mNotificationChannel)
                .putExtra("notificationContentTitle", R.string.text_timer_service_notification_title)
                .putExtra("notificationContentText", R.string.text_timer_service_notification_content)
                .putExtra("notificationSmallIconRId", R.drawable.ic_cc_checkmark)
                .putExtra(
                    "notificationContentIntent",
                    PendingIntent.getActivity(
                            this,
                            0,
                            (new Intent(this, MainActivity.class))
                                .setAction(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_LAUNCHER),
                            0
                    )
                )
                .putExtra(
                    "activityPendingIntent",
                    createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
                )
                .putExtra("logger", mLogger);

        return intent;
    }

    private Intent buildTimerStopIntent()
    {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("stop");
        intent.putExtra(
                "activityPendingIntent",
                createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
        );
        intent.putExtra("logger", mLogger);

        return intent;
    }

    private Intent buildTimerSimpleIntent(String action)
    {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction(action);
        return intent;
    }

    private void onTimerStartRequested()
    {
        startService(buildTimerStartIntent());
    }

    private void onTimerPauseRequested()
    {
        startService(buildTimerSimpleIntent("pause"));
    }

    private void onTimerResumeRequested()
    {
        startService(buildTimerSimpleIntent("resume"));
    }

    private void onTimerStopRequested()
    {
        startService(buildTimerStopIntent());
    }

    private void onTimerFinished() {}

    private boolean onActionBtnLongClick(View v)
    {
        onTimerStopRequested();
        return true;
    }

    private void setMillisUntilFinished(long millisUntilFinished)
    {
        mMillisUntilFinished = millisUntilFinished;
        String timeFormatted = mDateFormat.format(new Date(mMillisUntilFinished));
        mTopTextView.setText(timeFormatted);
        updateTimerArc(mMillisUntilFinished);
    }

    protected void updateTimerArc(long millisUntilFinished)
    {
        float clockCirclePercent = millisUntilFinished > mCountDownInterval
                ? (float)millisUntilFinished / (float)mMillisInFuture * 100f
                : 0;
        float sweepAngle = 360f * clockCirclePercent / 100f;
        mTimerArc.setSweepAngle(sweepAngle);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MAIN_TIMER_SERVICE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.hasExtra("timer-op")) {
                int timerEvent = data.getIntExtra("timer-op", 0);
                switch (timerEvent) {
                    case TimerService.OP_TIMER_FINISHED:
                        onTimerFinished();
                        break;
                    case TimerService.OP_TIMER_TICK:
                        onTimerTick(data.getLongExtra("millisUntilFinished", 0L));
                        break;
                    case TimerService.OP_TIMER_PONG:
                        onTimerPong(
                                TimerState.values()[data.getIntExtra("state", TimerState.CORRUPTED.ordinal())],
                                TimerMode.values()[data.getIntExtra("mode", TimerMode.CORRUPTED.ordinal())],
                                data.getLongExtra("millisUntilFinished", 0L),
                                data.getLongExtra("millisInFuture", 0L),
                                data.getIntExtra("iterationNum", 0)
                        );
                        break;
                    default:
                        mLogger.e(mClassName, "Unknown timer event: " + timerEvent);
                }
            } else {
                mLogger.d(mClassName, "A timer service request received, but timer-event is empty");
            }
        }
    }
}
