package com.zeoflow.material.elements.colorwheel;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState;

import static com.zeoflow.material.elements.colorwheel.extensions.ParcelExtensions.readBooleanCompat;
import static com.zeoflow.material.elements.colorwheel.extensions.ParcelExtensions.writeBooleanCompat;
import static com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState.readThumbState;
import static com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState.writeThumbState;

public class ColorWheelState extends View.BaseSavedState
{

    ThumbDrawableState thumbState;
    boolean interceptTouchEvent;
    int rgb;

    public ColorWheelState(Parcelable superState, ColorWheel view, ThumbDrawableState thumbState)
    {
        super(superState);
        this.thumbState = thumbState;
        interceptTouchEvent = view.interceptTouchEvent;
        rgb = view.getRgb();
    }
    public ColorWheelState(Parcel source)
    {
        super(source);
        thumbState = readThumbState(source);
        interceptTouchEvent = readBooleanCompat(source);
        rgb = source.readInt();
    }
    public ThumbDrawableState getThumbState()
    {
        return thumbState;
    }
    public boolean getInterceptTouchEvent()
    {
        return interceptTouchEvent;
    }
    public int getRgb()
    {
        return rgb;
    }
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        writeThumbState(out, thumbState, flags);
        writeBooleanCompat(out, interceptTouchEvent);
        out.writeInt(rgb);
    }

}
