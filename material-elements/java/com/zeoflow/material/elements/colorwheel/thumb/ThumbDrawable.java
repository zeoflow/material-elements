package com.zeoflow.material.elements.colorwheel.thumb;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.FloatRange;

public class ThumbDrawable
{

    public int indicatorColor = 0;
    public int strokeColor = 0;
    public int thumbColor = 0;
    public int radius = 0;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float x = 0f;
    private float y = 0f;
    private float colorCircleScale = 0f;
    public ThumbDrawable()
    {
        paint.setStrokeWidth(1f);
    }
    public float getColorCircleScale()
    {
        return colorCircleScale;
    }
    public void setColorCircleScale(@FloatRange(from = 0, to = 1) float scale)
    {
        colorCircleScale = scale;
    }
    public void setCoordinates(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void draw(Canvas canvas)
    {
        drawThumb(canvas);
        drawStroke(canvas);
        drawColorIndicator(canvas);
    }

    private void drawThumb(Canvas canvas)
    {
        paint.setColor(thumbColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);
    }

    private void drawStroke(Canvas canvas)
    {
        float strokeCircleRadius = radius - paint.getStrokeWidth() / 2f;

        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, strokeCircleRadius, paint);
    }

    private void drawColorIndicator(Canvas canvas)
    {
        float colorIndicatorCircleRadius = radius * colorCircleScale;

        paint.setColor(indicatorColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, colorIndicatorCircleRadius, paint);
    }

    public void restoreState(ThumbDrawableState state)
    {
        radius = state.getRadius();
        thumbColor = state.getThumbColor();
        strokeColor = state.getStrokeColor();
        colorCircleScale = state.getColorCircleScale();
    }

    public ThumbDrawableState saveState()
    {
        return new ThumbDrawableState(
                radius,
                thumbColor,
                strokeColor,
                colorCircleScale
        );
    }

}
