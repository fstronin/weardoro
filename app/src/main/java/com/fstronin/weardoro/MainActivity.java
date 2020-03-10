package com.fstronin.weardoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fstronin.weardoro.interval.AlarmPendingIntentBuilder;
import com.fstronin.weardoro.interval.FocusInterval;
import com.fstronin.weardoro.interval.IInterval;
import com.fstronin.weardoro.interval.IntervalException;

import java.util.Date;

public class MainActivity extends WearableActivity
{
    private TextView mTopTextView;
    private TextView mBottomTextView;
    private Button mActionBtn;
    private TimerArc mTimerArc;
    private IInterval mInterval;

    private BroadcastReceiver mBroadcastReceiver;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTopTextView = findViewById(R.id.text_view_top);
        mBottomTextView = findViewById(R.id.text_view_bottom);

        mActionBtn = findViewById(R.id.actionBtn);
        mActionBtn.setOnClickListener(this::onActionBtnClick);
        mActionBtn.setOnLongClickListener(this::onActionBtnLongClick);

        mTimerArc = findViewById((R.id.timerArc));

        mBroadcastReceiver = buildBroadcastReceiver();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // If user paused the activity then it doesn't need to handle any updates from AlarmReceiver
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Try to read latest started interval
        mInterval = App.getIntervalBuilder().fromSharedPreferences(
                App.getSharedPreferences(this),
                IInterval.PREF_KEY_INTERVAL_CLASS,
                IInterval.PREF_KEY_INTERVAL_DATA
        );
        // If nothing found then just create a default one
        if (null == mInterval) {
            mInterval = new FocusInterval(new AlarmPendingIntentBuilder());
        }

        onIntervalLoad(mInterval);

        // Register a receiver to be able to receive messages from an AlarmReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(IInterval.ALARM_INTENT_ACTION_INTERVAL_STARTED);
        filter.addAction(IInterval.ALARM_INTENT_ACTION_INTERVAL_PAUSED);
        filter.addAction(IInterval.ALARM_INTENT_ACTION_INTERVAL_STOPPED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void onActionBtnClick(View v)
    {
        try {
            switch (mInterval.getState()) {
                case IDLE:
                    mInterval.start(this);
                    break;
                case RUNNING:
                    mInterval.pause(this);
                    break;
                case PAUSED:
                    mInterval.resume(this);
                    break;
                default:
                    App.getLogger().e(this.getLocalClassName(), "Unsupported interval operation requested onActionBtnClick");
            }
        } catch (IntervalException e) {
            App.getLogger().e(this.getLocalClassName(), e.getMessage());
        }
    }

    private boolean onActionBtnLongClick(View v)
    {
        try {
            if (null != mCountDownTimer) {
                mCountDownTimer.cancel();
            }
            mInterval.stop(this);
        } catch (IntervalException e) {
            App.getLogger().e(this.getClass().getName(), e.getMessage(), e);
        }
        return true;
    }

    private void onIntervalLoad(IInterval interval)
    {
        mTopTextView.setText(App.getTimerClockFormat(this).format(new Date(interval.getMillisInFuture())));
        mBottomTextView.setText(interval.getDisplayName(this));
        switch (interval.getState()) {
            case PAUSED:
                mActionBtn.setText(R.string.text_resume);
                break;
            case RUNNING:
                onIntervalStarted(interval);
        }
    }

    private void onIntervalStarted(IInterval interval)
    {
        if (null != mCountDownTimer) {
            mCountDownTimer.cancel();
        }
        long timerMillisInFuture = interval.getMillisInFuture();
        if (timerMillisInFuture > 0) {
            mCountDownTimer = new CountDownTimer(timerMillisInFuture, App.getMillisCountDownInterval(this)) {
                @Override
                public void onTick(long millisUntilFinished) {
                   //  App.getLogger().d(this.getClass().getName(), "Timer tick, millisUntilFinished=" + millisUntilFinished);
                   // mTimerArc.update(MainActivity.this, timerMillisInFuture, millisUntilFinished);
                    mTopTextView.setText(App.getTimerClockFormat(MainActivity.this).format(new Date(millisUntilFinished)));
                }

                @Override
                public void onFinish() {
                    App.getLogger().d(this.getClass().getName(), "Timer finished");
                }
            }.start();
        }
        mActionBtn.setText(R.string.text_pause);
        mBottomTextView.setText(interval.getDisplayName(this));
    }

    private void onIntervalPaused(IInterval interval)
    {
        if (null != mCountDownTimer) {
            mCountDownTimer.cancel();
        }
        mActionBtn.setText(R.string.text_resume);
    }

    private void onIntervalStopped(IInterval interval)
    {
        if (null != mCountDownTimer) {
            mCountDownTimer.cancel();
        }
        mActionBtn.setText(R.string.text_stop);
        mTopTextView.setText(R.string.text_greetings);
        mBottomTextView.setText("");
        mTimerArc.update(this, 0, 0);
    }

    private BroadcastReceiver buildBroadcastReceiver()
    {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                IInterval interval = intent.getParcelableExtra(IInterval.ALARM_INTENT_INTERVAL_INSTANCE_KEY);
                if (null == interval) {
                    App.getLogger().e(this.getClass().getName(), "Unable to obtain interval instance from an intent");
                    return;
                }
                switch (intent.getAction()) {
                    case IInterval.ALARM_INTENT_ACTION_INTERVAL_STARTED:
                        MainActivity.this.onIntervalStarted(interval);
                        break;
                    case IInterval.ALARM_INTENT_ACTION_INTERVAL_PAUSED:
                        MainActivity.this.onIntervalPaused(interval);
                        break;
                    case IInterval.ALARM_INTENT_ACTION_INTERVAL_STOPPED:
                        MainActivity.this.onIntervalStopped(interval);
                        break;
                }
            }
        };
    }
}
