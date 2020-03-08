package com.fstronin.weardoro;

import java.util.Date;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fstronin.weardoro.service.timer.TimerMode;
import com.fstronin.weardoro.service.timer.TimerService;
import com.fstronin.weardoro.service.timer.TimerState;

public class MainActivity extends WearableActivity {

    private static final int MAIN_TIMER_SERVICE_REQUEST_CODE = 1;
    private String mClassName;
    private TextView mTopTextView;
    private TextView mBottomTextView;
    private Button mActionBtn;
    private TimerArc mTimerArc;
    private long mMillisUntilFinished = 0L;
    private long mMillisInFuture = 0L;
    private TimerState mTimerState = TimerState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClassName = MainActivity.class.getName();

        mTopTextView = findViewById(R.id.text_view_top);
        mBottomTextView = findViewById(R.id.text_view_bottom);

        mActionBtn = findViewById(R.id.actionBtn);
        mActionBtn.setOnClickListener(this::onActionBtnClick);
        mActionBtn.setOnLongClickListener(this::onActionBtnLongClick);

        mTimerArc = findViewById((R.id.timerArc));
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
                App.getLogger().e(mClassName, "Unsupported timer state detected onActionBtnClick");
        }
    }

    private void sendTimerPing()
    {
        Intent intent = (new Intent(this, TimerService.class))
                .setAction("ping")
                .putExtra(
                        "activityPendingIntent",
                        createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, new Intent(), 0)
                )
                .putExtra("logger", App.getLogger());
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
        App.getLogger().d(
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
            App.getLogger().e(mClassName, "TimerState CORRUPTED");
            return;
        }
        mTimerState = state;
        if (mode == TimerMode.CORRUPTED) {
            App.getLogger().e(mClassName, "TimerMode CORRUPTED");
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
                App.getLogger().e(mClassName, "Unsupported timer state detected onTimerPong");
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
                .putExtra("countDownInterval", App.getMillisCountDownInterval(this))
                .putExtra("millisFocusInterval", App.getMillisFocusInterval(this))
                .putExtra("millisRestInterval", App.getMillisRestInterval(this))
                .putExtra("millisLongRestInterval", App.getMillisLongRestInterval(this))
                .putExtra("longRestIntervalPosition", App.getLongRestIntervalPosition(this))
                .putExtra("foregroundId", App.getServiceForegroundId(this))
                .putExtra("notificationChannel", App.getNotificationChannel(this))
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
                    createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, new Intent(), 0)
                )
                .putExtra("logger", App.getLogger());

        return intent;
    }

    private Intent buildTimerStopIntent()
    {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("stop");
        intent.putExtra(
                "activityPendingIntent",
                createPendingResult(MAIN_TIMER_SERVICE_REQUEST_CODE, new Intent(), 0)
        );
        intent.putExtra("logger", App.getLogger());

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
        String timeFormatted = App.getTimerClockFormat(this).format(new Date(mMillisUntilFinished));
        mTopTextView.setText(timeFormatted);
        updateTimerArc(mMillisUntilFinished);
    }

    protected void updateTimerArc(long millisUntilFinished)
    {
        float clockCirclePercent = millisUntilFinished > App.getMillisCountDownInterval(this)
                ? (float)millisUntilFinished / (float)mMillisInFuture * 100f
                : 0;
        float sweepAngle = 360f * clockCirclePercent / 100f;
        mTimerArc.setSweepAngle(sweepAngle);
    }

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
                        App.getLogger().e(mClassName, "Unknown timer event: " + timerEvent);
                }
            } else {
                App.getLogger().d(mClassName, "A timer service request received, but timer-event is empty");
            }
        }
    }
}
