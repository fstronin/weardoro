package com.fstronin.weardoro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class TimerArc extends View
{
    private Paint paint;
    private RectF oval;
    private float sweepAngle = 0;

    public TimerArc(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        paint = buildPaint();
        oval = buildOval();
    }

    protected RectF buildOval()
    {
        return new RectF();
    }

    protected Paint buildPaint()
    {
        Paint p = new Paint();
        p.setColor(Color.RED);
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

    public void setSweepAngle(float value)
    {
        sweepAngle = value;
        invalidate();
    }
}
