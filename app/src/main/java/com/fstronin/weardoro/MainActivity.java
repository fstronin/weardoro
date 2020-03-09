package com.fstronin.weardoro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.fstronin.weardoro.interval.AlarmPendingIntentBuilder;
import com.fstronin.weardoro.interval.FocusInterval;
import com.fstronin.weardoro.interval.IInterval;
import com.fstronin.weardoro.interval.IntervalBuilder;
import com.fstronin.weardoro.interval.IntervalException;

public class MainActivity extends WearableActivity
{

    private final String PREF_KEY_INTERVAL_CLASS = "INTERVAL_CLASS";
    private final String PREF_KEY_INTERVAL_DATA = "INTERVAL_DATA";
    private final int ALARM_REQUEST_CODE = 1;

    private IntervalBuilder mIntervalBuilder;
    private AlarmPendingIntentBuilder mAlarmIntentBuilder;

    private TextView mTopTextView;
    private TextView mBottomTextView;
    private Button mActionBtn;
    private TimerArc mTimerArc;
    private IInterval mInterval;

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

        mIntervalBuilder = new IntervalBuilder();

        mAlarmIntentBuilder = (new AlarmPendingIntentBuilder())
                .setRequestCode(ALARM_REQUEST_CODE)
                .setFlags(0)
                .setIntent((new Intent(this, AlarmReceiver.class)).setAction("com.fstronin.weardoro.INTERVAL_FINISHED"));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mInterval = new FocusInterval(mAlarmIntentBuilder);
        /*
        mInterval = mIntervalBuilder.fromSharedPreferences(
                App.getSharedPreferences(this),
                PREF_KEY_INTERVAL_CLASS,
                PREF_KEY_INTERVAL_DATA,
                mAlarmIntentBuilder
        );
        */
    }

    @Override
    public void onStop()
    {
        String intervalData = App.getGson().toJson(mInterval);
        App.getLogger().d(
                this.getLocalClassName(),
                "Going to save interval instance into shared preferences, class = "
                + mInterval.getClass().getName() + ", data = " + intervalData
        );
        SharedPreferences.Editor spEditor = App.getSharedPreferences(this).edit();
        spEditor.putString(PREF_KEY_INTERVAL_CLASS, mInterval.getClass().getName());
        spEditor.putString(PREF_KEY_INTERVAL_DATA, intervalData);
        spEditor.apply();
        super.onStop();
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
        // onIntervalStopRequested(mInterval);
        return true;
    }

    protected void updateTimerArc(long millisUntilFinished)
    {
        long mMillisInFuture =0;
        float clockCirclePercent = millisUntilFinished > App.getMillisCountDownInterval(this)
                ? (float)millisUntilFinished / (float)mMillisInFuture * 100f
                : 0;
        float sweepAngle = 360f * clockCirclePercent / 100f;
        mTimerArc.setSweepAngle(sweepAngle);
    }

}
