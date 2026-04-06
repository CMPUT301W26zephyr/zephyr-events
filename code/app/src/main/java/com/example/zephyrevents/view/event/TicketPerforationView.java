package com.example.zephyrevents.view.event;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;

/** Dashed vertical line for ticket tear styling. */
public class TicketPerforationView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int grooveColor;

    public TicketPerforationView(Context context) {
        super(context);
        init(context);
    }

    public TicketPerforationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        grooveColor = ContextCompat.getColor(context, R.color.ticket_groove_edge);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        setWillNotDraw(false);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        float cx = w / 2f;
        float pad = dp(5);
        if (h < pad * 2) {
            return;
        }

        float stroke = dp(1.15f);
        paint.setStrokeWidth(stroke);
        paint.setColor(grooveColor);
        float dot = dp(2f);
        float gap = dp(3.5f);
        paint.setPathEffect(new DashPathEffect(new float[]{dot, gap}, 0));

        canvas.drawLine(cx, pad, cx, h - pad, paint);
    }
}
