package com.fstronin.weardoro;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fstronin.weardoro.logging.Logger;
import com.fstronin.weardoro.logging.LoggerInterface;
import com.fstronin.weardoro.service.timer.TimerService;


public class MainActivity extends WearableActivity {

    private final int TIMER_SERVICE_REQUEST_CODE = 1;
    private final int FOREGROUND_ID = 1;
    private final String NOTIFICATION_CHANNEL_ID = "com.fstronin.weardoro";
    private final String NOTIFICATION_CHANNEL_NAME = "Weardoro";

    private TextView mTextView;
    private Button mStartBtn;
    private Button mStopBtn;
    private TimerArc mTimerArc;
    private DateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
    private LoggerInterface mLogger = new Logger();
    private final Intent mEmptyIntent = new Intent();
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationChannel = buildNotificationChannel();
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        mTextView = (TextView) findViewById(R.id.text);

        mStartBtn = (Button) findViewById(R.id.startBtn);
        mStartBtn.setOnClickListener(this::onStartBtnClick);

        mStopBtn = (Button) findViewById(R.id.stopBtn);
        mStopBtn.setOnClickListener(this::onStopBtnClick);

        mTimerArc = (TimerArc) findViewById((R.id.timerArc));
    }

    private NotificationChannel buildNotificationChannel()
    {
        return new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("ping");
        intent.putExtra(
                "activityPendingIntent",
                createPendingResult(TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
        );
        intent.putExtra("logger", mLogger);
        startService(intent);
    }

    protected void onStartBtnClick(View v)
    {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("start");
        intent.putExtra("millisInFuture", 1000L * 10);
        intent.putExtra("countDownInterval", 1000L);
        intent.putExtra("foregroundId", FOREGROUND_ID);
        intent.putExtra("notificationChannel", mNotificationChannel);
        intent.putExtra("notificationContentTitle", "Notification Title");
        intent.putExtra("notificationContentText", "Notification text");
        intent.putExtra("notificationSmallIconRId", R.drawable.ic_cc_checkmark);
        intent.putExtra(
                "notificationContentIntent",
                PendingIntent.getActivity(this, 0, mEmptyIntent, 0)
        );
        intent.putExtra(
                "activityPendingIntent",
                createPendingResult(TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
        );
        intent.putExtra("logger", mLogger);
        startService(intent);
    }

    protected void onStopBtnClick(View v)
    {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("stop");
        intent.putExtra(
                "activityPendingIntent",
                createPendingResult(TIMER_SERVICE_REQUEST_CODE, mEmptyIntent, 0)
        );
        intent.putExtra("logger", mLogger);
        startService(intent);
    }

    protected void updateTimerArc(long millisInFuture, long millisUntilFinished)
    {
        float clockCirclePercent = millisUntilFinished > 0
                ? (float)millisUntilFinished / (float)millisInFuture * 100f
                : 0;
        float sweepAngle = 360f * clockCirclePercent / 100f;
        mTimerArc.setSweepAngle(sweepAngle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TIMER_SERVICE_REQUEST_CODE && resultCode == RESULT_OK) {
            String timeFormatted = mDateFormat.format(new Date(data.getLongExtra("millisUntilFinished", 0)));
            mTextView.setText(timeFormatted);
            updateTimerArc(1000L * 10, data.getLongExtra("millisUntilFinished", 0));
        }
    }
}
