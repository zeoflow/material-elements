package com.zeoflow.material.elements.colorwheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.color.ColorEnvelope;
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawable;
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState;
import com.zeoflow.material.elements.colorwheel.utils.HsvColor;

import static com.zeoflow.material.elements.colorwheel.utils.ColorUtils.HUE_COLORS;
import static com.zeoflow.material.elements.colorwheel.utils.ColorUtils.SATURATION_COLORS;
import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.toDegrees;
import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.toRadians;
import static com.zeoflow.material.elements.colorwheel.utils.TouchUtils.isTap;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.sin;

public class ColorWheel extends View
{

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    GradientDrawable hueGradient = new GradientDrawable();

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    GradientDrawable saturationGradient = new GradientDrawable();

    ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
    ThumbDrawable thumbDrawable = new ThumbDrawable();
    HsvColor hsvColor = new HsvColor(0f, 0f, 1f);

    int wheelCenterX = 0;
    int wheelCenterY = 0;
    int wheelRadius = 0;
    float downX = 0f;
    float downY = 0f;
    boolean interceptTouchEvent = true;
    private IColorChangeListener listener;
    public ColorWheel(Context context)
    {
        this(context, null);
    }
    public ColorWheel(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    public ColorWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorWheel(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);

        hueGradient.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        hueGradient.setShape(GradientDrawable.OVAL);
        hueGradient.setColors(HUE_COLORS);

        saturationGradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        saturationGradient.setShape(GradientDrawable.OVAL);
        saturationGradient.setColors(SATURATION_COLORS);

        parseAttributes(context, attrs);
    }
    public void parseAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorWheel, 0, R.style.ColorWheelDefaultStyle);
        setThumbRadius(a.getDimensionPixelSize(R.styleable.ColorWheel_cw_thumbRadius, 0));
        setThumbColor(a.getColor(R.styleable.ColorWheel_cw_thumbColor, 0));
        setThumbStrokeColor(a.getColor(R.styleable.ColorWheel_cw_thumbStrokeColor, 0));
        setThumbColorCircleScale(a.getFloat(R.styleable.ColorWheel_cw_thumbColorCircleScale, 0));
        a.recycle();
    }
    public int getRgb()
    {
        return hsvColor.getRGB();
    }
    public void setRgb(int rgb)
    {
        hsvColor.setRGB(rgb);
        hsvColor.setValue(1f);
        fireColorListener();
        invalidate();
    }
    public void setRgb(int r, int g, int b)
    {
        setRgb(Color.rgb(r, g, b));
    }
    public int getThumbRadius()
    {
        return thumbDrawable.radius;
    }
    public void setThumbRadius(int radius)
    {
        thumbDrawable.radius = radius;
        invalidate();
    }
    public int getThumbColor()
    {
        return thumbDrawable.thumbColor;
    }
    public void setThumbColor(int color)
    {
        thumbDrawable.thumbColor = color;
        invalidate();
    }
    public int getThumbStrokeColor()
    {
        return thumbDrawable.strokeColor;
    }
    public void setThumbStrokeColor(int color)
    {
        thumbDrawable.strokeColor = color;
        invalidate();
    }
    public float getThumbColorCircleScale()
    {
        return thumbDrawable.getColorCircleScale();
    }
    public void setThumbColorCircleScale(float scale)
    {
        thumbDrawable.setColorCircleScale(scale);
        invalidate();
    }
    public void setColorChangeListener(IColorChangeListener listener)
    {
        this.listener = listener;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int minDimension = Math.min(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec)
        );

        setMeasuredDimension(
                resolveSize(minDimension, widthMeasureSpec),
                resolveSize(minDimension, heightMeasureSpec)
        );
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        drawColorWheel(canvas);
        drawThumb(canvas);
    }
    public void drawColorWheel(Canvas canvas)
    {
        int hSpace = getWidth() - getPaddingLeft() - getPaddingRight();
        int vSpace = getHeight() - getPaddingTop() - getPaddingBottom();

        wheelCenterX = getPaddingLeft() + hSpace / 2;
        wheelCenterY = getPaddingTop() + vSpace / 2;
        wheelRadius = Math.max((Math.min(hSpace, vSpace) / 2), 0);

        int left = wheelCenterX - wheelRadius;
        int top = wheelCenterY - wheelRadius;
        int right = wheelCenterX + wheelRadius;
        int bottom = wheelCenterY + wheelRadius;

        hueGradient.setBounds(left, top, right, bottom);
        saturationGradient.setBounds(left, top, right, bottom);
        saturationGradient.setGradientRadius(wheelRadius);

        hueGradient.draw(canvas);
        saturationGradient.draw(canvas);
    }
    public void drawThumb(Canvas canvas)
    {
        double r = hsvColor.getSaturation() * wheelRadius;
        double hueRadians = toRadians(hsvColor.getHue());
        float x = (float) (cos(hueRadians) * r + wheelCenterX);
        float y = (float) (sin(hueRadians) * r + wheelCenterY);

        thumbDrawable.indicatorColor = hsvColor.getRGB();
        thumbDrawable.setCoordinates(x, y);
        thumbDrawable.draw(canvas);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateColorOnMotionEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                updateColorOnMotionEvent(event);
                if (isTap(event, downX, downY, viewConfig)) performClick();
                break;
        }
        return true;
    }
    private void onActionDown(MotionEvent event)
    {
        getParent().requestDisallowInterceptTouchEvent(interceptTouchEvent);
        updateColorOnMotionEvent(event);
        downX = event.getX();
        downY = event.getY();
    }
    private void updateColorOnMotionEvent(MotionEvent event)
    {
        calculateColor(event);
        fireColorListener();
        invalidate();
    }
    private void calculateColor(MotionEvent event)
    {
        float legX = event.getX() - wheelCenterX;
        float legY = event.getY() - wheelCenterY;
        double hypot = Math.min(hypot(legX, legY), (float) wheelRadius);
        float hue = (toDegrees((float) atan2(legY, legX)) + 360) % 360;
        float saturation = (float) (hypot / wheelRadius);
        hsvColor.set(hue, saturation, 1f);
    }
    public ColorEnvelope getColorEnvelope()
    {
        return new ColorEnvelope(getRgb());
    }
    public void fireColorListener()
    {
        if (listener != null)
        {
            listener.onChanged(hsvColor.getRGB());
        }
    }
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        ThumbDrawableState thumbState = thumbDrawable.saveState();
        return new ColorWheelState(superState, this, thumbState);
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof ColorWheelState)
        {
            super.onRestoreInstanceState(((ColorWheelState) state).getSuperState());
            readColorWheelState((ColorWheelState) state);
        } else
        {
            super.onRestoreInstanceState(state);
        }
    }
    private void readColorWheelState(ColorWheelState state)
    {
        thumbDrawable.restoreState(state.getThumbState());
        interceptTouchEvent = state.getInterceptTouchEvent();
        setRgb(state.getRgb());
    }

    public interface IColorChangeListener
    {

        void onChanged(int rgb);

    }

}