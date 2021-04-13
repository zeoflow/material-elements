package com.zeoflow.material.elements.color;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawable;
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState;
import com.zeoflow.material.elements.gradientseekbar.GradientSeekBar;
import com.zeoflow.material.elements.gradientseekbar.GradientSeekBarState;

import static com.zeoflow.material.elements.colorwheel.utils.ColorUtils.MAX_ALPHA;
import static com.zeoflow.material.elements.colorwheel.utils.ColorUtils.interpolateColorLinear;
import static com.zeoflow.material.elements.colorwheel.utils.ColorUtils.setColorAlpha;
import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.ensureNumberWithinRange;
import static com.zeoflow.material.elements.colorwheel.utils.TouchUtils.isTap;
import static java.lang.reflect.Array.getInt;

public class ColorSeekBar extends View
{

    Orientation orientation = Orientation.VERTICAL;
    boolean interceptTouchEvent = true;
    private ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
    private int[] gradientColors = new int[]{
            Color.parseColor("#000000"),
            Color.parseColor("#ff0000"),
            Color.parseColor("#ff0040"),
            Color.parseColor("#ff0080"),
            Color.parseColor("#ff00bf"),
            Color.parseColor("#ff00ff"),
            Color.parseColor("#bf00ff"),
            Color.parseColor("#8000ff"),
            Color.parseColor("#4000ff"),
            Color.parseColor("#0000ff"),
            Color.parseColor("#0040ff"),
            Color.parseColor("#0080ff"),
            Color.parseColor("#00bfff"),
            Color.parseColor("#00ffff"),
            Color.parseColor("#00ffbf"),
            Color.parseColor("#00ff80"),
            Color.parseColor("#00ff40"),
            Color.parseColor("#00ff00"),
            Color.parseColor("#40ff00"),
            Color.parseColor("#80ff00"),
            Color.parseColor("#bfff00"),
            Color.parseColor("#ffff00"),
            Color.parseColor("#ffbf00"),
            Color.parseColor("#ff8000"),
            Color.parseColor("#ff4000"),
            Color.parseColor("#ff0000"),
            Color.parseColor("#ffffff")
    };
    private ThumbDrawable thumbDrawable = new ThumbDrawable();
    private GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
    private OrientationStrategy orientationStrategy;
    private float downX = 0f;
    private float downY = 0f;
    private float offset = 0f;
    private int barSize = 0;
    private int strokeSize = 0;
    private float cornersRadius = 0f;
    private int argb = 0;
    public int getCurrentColorAlpha = Color.alpha(argb);
    private IColorChangeListener listener;
    public ColorSeekBar(Context context)
    {
        this(context, null);
    }

    public ColorSeekBar(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    public ColorSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }
    public ColorSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttributes(context, attrs, R.style.GradientSeekBarDefaultStyle);
    }
    public int getStartColor()
    {
        return gradientColors[0];
    }
    public void setStartColor(int color)
    {
        setColors(color, gradientColors[1]);
    }
    public int getEndColor()
    {
        return gradientColors[1];
    }
    public void setEndColor(int color)
    {
        setColors(gradientColors[0], color);
    }
    public float getOffset()
    {
        return offset;
    }
    public void setOffset(float value)
    {
        offset = ensureOffsetWithinRange(offset);
        calculateArgb();
    }
    public int getBarSize()
    {
        return barSize;
    }
    public void setBarSize(int size)
    {
        barSize = size;
        requestLayout();
    }
    public int getStrokeSize()
    {
        return strokeSize;
    }
    public void setStrokeSize(int size)
    {
        strokeSize = size;
        requestLayout();
    }
    public float getCornersRadius()
    {
        return cornersRadius;
    }
    public void setCornersRadius(float radius)
    {
        cornersRadius = radius;
        invalidate();
    }
    public Orientation getOrientation()
    {
        return orientation;
    }
    public void setOrientation(Orientation orientation)
    {
        this.orientation = orientation;
        orientationStrategy = createOrientationStrategy();
        requestLayout();
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
    public void setThumbColorCircleScale(float color)
    {
        thumbDrawable.setColorCircleScale(color);
        invalidate();
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
    public void setColorChangeListener(IColorChangeListener listener)
    {
        this.listener = listener;
    }
    private void parseAttributes(Context context, AttributeSet attrs, int defStyle)
    {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientSeekBar, 0, defStyle);
        setColors(
                a.getColor(R.styleable.GradientSeekBar_asb_startColor, Color.TRANSPARENT),
                a.getColor(R.styleable.GradientSeekBar_asb_endColor, Color.BLACK)
        );
        setThumbColor(a.getColor(R.styleable.GradientSeekBar_asb_thumbColor, 0));
        setThumbStrokeColor(a.getColor(R.styleable.GradientSeekBar_asb_thumbStrokeColor, 0));
        setThumbColorCircleScale(a.getFloat(R.styleable.GradientSeekBar_asb_thumbColorCircleScale, 0f));
        setThumbRadius(a.getDimensionPixelSize(R.styleable.GradientSeekBar_asb_thumbRadius, 0));
        setBarSize(a.getDimensionPixelSize(R.styleable.GradientSeekBar_asb_barSize, 0));
        setStrokeSize(a.getDimensionPixelSize(R.styleable.GradientSeekBar_asb_strokeSize, 0));
        setCornersRadius(a.getDimension(R.styleable.GradientSeekBar_asb_barCornersRadius, 0f));
        setOffset(ensureOffsetWithinRange(a.getFloat(R.styleable.GradientSeekBar_asb_offset, 0f)));
        int o = a.getInt(R.styleable.GradientSeekBar_asb_orientation, 0);
        if (o == 0)
        {
            setOrientation(Orientation.HORIZONTAL);
        } else {
            setOrientation(Orientation.VERTICAL);
        }
        a.recycle();

    }

    private int pickColor(float position, int canvasWidth)
    {
        float value = (position - getPaddingStart()) / (canvasWidth - (getPaddingStart() + getPaddingEnd()));
        if (value <= 0)
        {
            return gradientColors[0];
        } else if (value >= 1)
        {
            return gradientColors[1];
        } else
        {
            float colorPosition = value * (gradientColors.length - 1);
            int i = (int) colorPosition;
            colorPosition -= i;
            int c0 = gradientColors[i];
            int c1 = gradientColors[i + 1];

            int red = mix(Color.red(c0), Color.red(c1), colorPosition);
            int green = mix(Color.green(c0), Color.green(c1), colorPosition);
            int blue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
            return Color.rgb(red, green, blue);
        }
    }

    private int mix(int start, int end, float position)
    {
        return start + Math.round(position * (end - start));
    }
    private OrientationStrategy createOrientationStrategy()
    {
        if (orientation == Orientation.VERTICAL)
        {
            return new VerticalStrategy();
        }
        return new HorizontalStrategy();
    }
    public void setColors(int startColor, int endColor)
    {
        updateGradientColors(startColor, endColor);
        calculateArgb();
    }
    private void updateGradientColors(int startColor, int endColor)
    {
        gradientColors[0] = startColor;
        gradientColors[1] = endColor;
        gradientDrawable.setColors(gradientColors);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Rect dimens = orientationStrategy.measure(this, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(dimens.width(), dimens.height());
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        drawGradientRect(canvas);
        drawThumb(canvas);
    }
    private void drawGradientRect(Canvas canvas)
    {
        gradientDrawable.setOrientation(orientationStrategy.getGradientOrientation());
        gradientDrawable.setBounds(orientationStrategy.calculateGradientBounds(this));
        gradientDrawable.setCornerRadius(cornersRadius);
        gradientDrawable.setStroke(strokeSize / 4, getThumbColor());
        gradientDrawable.draw(canvas);
    }
    private void drawThumb(Canvas canvas)
    {
        PointF coordinates = orientationStrategy.calculateThumbCoordinates(this, gradientDrawable.getBounds());

        thumbDrawable.indicatorColor = argb;
        thumbDrawable.setCoordinates(coordinates.x, coordinates.y);
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
                calculateOffsetOnMotionEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                calculateOffsetOnMotionEvent(event);
                if (isTap(event, downX, downY, viewConfig)) performClick();
                break;
        }
        return true;
    }
    private void onActionDown(MotionEvent event)
    {
        getParent().requestDisallowInterceptTouchEvent(interceptTouchEvent);
        calculateOffsetOnMotionEvent(event);
        downX = event.getX();
        downY = event.getY();
    }
    private float thumbX = 24f;
    private void calculateOffsetOnMotionEvent(MotionEvent event)
    {
        thumbX = event.getX();
        invalidate();
        setOffset(orientationStrategy.calculateOffsetOnMotionEvent(this, event, gradientDrawable.getBounds()));
    }
    private void calculateArgb()
    {
        argb = pickColor(thumbX, getWidth());
        argb = interpolateColorLinear(gradientColors[0], gradientColors[1], getOffset());
        fireListener();
        invalidate();
    }
    private void fireListener()
    {
        if (listener != null)
        {
            listener.onChanged(argb);
        }
    }
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        ThumbDrawableState thumbState = thumbDrawable.saveState();
        return null;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof GradientSeekBarState)
        {
            super.onRestoreInstanceState(((GradientSeekBarState) state).getSuperState());
            readGradientSeekBarState((GradientSeekBarState) state);
        } else
        {
            super.onRestoreInstanceState(state);
        }
    }
    private void readGradientSeekBarState(GradientSeekBarState state)
    {
        updateGradientColors(state.getStartColor(), state.getEndColor());
        offset = state.getOffset();
        barSize = state.getBarSize();
        cornersRadius = state.getCornerRadius();
        orientation = Orientation.values()[state.getOrientation()];
        interceptTouchEvent = state.getInterceptTouchEvent();
        thumbDrawable.restoreState(state.getThumbState());
    }
    private float ensureOffsetWithinRange(float offset)
    {
        return ensureNumberWithinRange(offset, 0f, 1f);
    }
    public void setTransparentToColor(int color)
    {
        setTransparentToColor(color, true);
    }
    public void setTransparentToColor(int color, boolean respectAlpha)
    {
        if (respectAlpha) setOffset((float) Color.alpha(color) / MAX_ALPHA);
        setColors(setColorAlpha(color, 0), setColorAlpha(color, MAX_ALPHA));
    }
    public void setBlackToColor(int color)
    {
        this.setColors(Color.BLACK, color);
    }
    enum Orientation
    {VERTICAL, HORIZONTAL}

    public interface IColorChangeListener
    {

        void onChanged(int argb);

    }

}