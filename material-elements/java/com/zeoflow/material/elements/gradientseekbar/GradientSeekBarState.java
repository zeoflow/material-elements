package com.zeoflow.material.elements.gradientseekbar;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState;

import static com.zeoflow.material.elements.colorwheel.extensions.ParcelExtensions.readBooleanCompat;
import static com.zeoflow.material.elements.colorwheel.extensions.ParcelExtensions.writeBooleanCompat;
import static com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState.readThumbState;
import static com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState.writeThumbState;

public class GradientSeekBarState extends View.BaseSavedState
{

    private int startColor;
    private int endColor;
    private float offset;
    private int barSize;
    private float cornerRadius;
    private int orientation;
    private boolean interceptTouchEvent;
    private ThumbDrawableState thumbState;

    public int getStartColor()
    {
        return startColor;
    }
    public int getEndColor()
    {
        return endColor;
    }
    public float getOffset()
    {
        return offset;
    }
    public int getBarSize()
    {
        return barSize;
    }
    public float getCornerRadius()
    {
        return cornerRadius;
    }
    public int getOrientation()
    {
        return orientation;
    }
    public boolean getInterceptTouchEvent()
    {
        return interceptTouchEvent;
    }
    public ThumbDrawableState getThumbState()
    {
        return thumbState;
    }
    public GradientSeekBarState(Parcelable superState, GradientSeekBar view, ThumbDrawableState thumbState)
    {
        super(superState);
        startColor = view.getStartColor();
        endColor = view.getEndColor();
        offset = view.getOffset();
        barSize = view.getBarSize();
        cornerRadius = view.getCornersRadius();
        orientation = view.orientation.ordinal();
        interceptTouchEvent = view.interceptTouchEvent;
        this.thumbState = thumbState;
    }

    public GradientSeekBarState(Parcel source)
    {
        super(source);
        startColor = source.readInt();
        endColor = source.readInt();
        offset = source.readFloat();
        barSize = source.readInt();
        cornerRadius = source.readFloat();
        orientation = source.readInt();
        interceptTouchEvent = readBooleanCompat(source);
        thumbState = readThumbState(source);
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        out.writeInt(startColor);
        out.writeInt(endColor);
        out.writeFloat(offset);
        out.writeInt(barSize);
        out.writeFloat(cornerRadius);
        out.writeInt(orientation);
        writeBooleanCompat(out, interceptTouchEvent);
        writeThumbState(out, thumbState, flags);
    }

}
