package com.fstronin.weardoro;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.fstronin.weardoro.interval.IInterval;


public class TimerArc extends View
{
    private Paint paint;
    private RectF oval;
    private float sweepAngle = 0;
    private String mColor;

    public TimerArc(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray styleableAttrs = context.obtainStyledAttributes(attrs, R.styleable.TimerArc);
        mColor = styleableAttrs.getString(R.styleable.TimerArc_arcColor);
        styleableAttrs.recycle();

        paint = buildPaint(context);
        oval = buildOval();
    }

    protected RectF buildOval()
    {
        return new RectF();
    }

    protected Paint buildPaint(Context context)
    {
        Paint p = new Paint();
        p.setColor(Color.parseColor(mColor));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(25);
        p.setAntiAlias(true);

        return p;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        oval.left = getLeft();
        oval.top = getTop();
        oval.right = getRight();
        oval.bottom = getBottom();

        canvas.drawArc(oval, -90, sweepAngle, false, paint);
    }

    private void setSweepAngle(float value)
    {
        sweepAngle = value;
        invalidate();
    }

    private float getClockCirclePercent(long millisInFuture, long millisUntilFinished, long millisCountDownInterval)
    {
        return millisUntilFinished > millisCountDownInterval
                ? (float) millisUntilFinished / (float) millisInFuture * 100f
                : 0;
    }

    private void update(Context ctx, long millisInFuture, long millisUntilFinished, float fullCircleAngle)
    {
        float clockCirclePercent = getClockCirclePercent(millisInFuture, millisUntilFinished, App.getMillisCountDownInterval(ctx));
        float sweepAngle = fullCircleAngle * clockCirclePercent / 100f;
        setSweepAngle(sweepAngle);
    }

    public void update(Context ctx, long millisInFuture, long millisUntilFinished, IInterval interval)
    {
        long intervalDuration = interval.getDuration();
        long intervalElapsedTime = interval.getElapsed()    ;
        float fullCircleAngle = 360f;
        if (intervalElapsedTime > 0) {
            float intervalElapsedTimePercent = (float) intervalElapsedTime / (float) intervalDuration * 100f;
            fullCircleAngle = 360f - (360f * intervalElapsedTimePercent / 100f);
        }
        update(ctx, millisInFuture, millisUntilFinished, fullCircleAngle);
    }

    public void update(Context ctx, long millisInFuture, long millisUntilFinished)
    {
        update(ctx, millisInFuture, millisUntilFinished, 360f);
    }
}
