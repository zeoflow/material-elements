package com.zeoflow.material.elements.colorwheel.thumb;

import android.os.Parcel;
import android.os.Parcelable;

public class ThumbDrawableState implements Parcelable
{

    public static ThumbDrawableState EMPTY_STATE = new ThumbDrawableState(0, 0, 0, 0f);
    public static Parcelable.Creator<ThumbDrawableState> CREATOR = new Creator<ThumbDrawableState>()
    {
        @Override
        public ThumbDrawableState createFromParcel(Parcel parcel)
        {
            return new ThumbDrawableState(parcel);
        }
        @Override
        public ThumbDrawableState[] newArray(int size)
        {
            return new ThumbDrawableState[0];
        }
    };
    int radius;
    int thumbColor;
    int strokeColor;
    float colorCircleScale;
    public ThumbDrawableState(int radius, int thumbColor, int strokeColor, float colorCircleScale)
    {
        this.radius = radius;
        this.thumbColor = thumbColor;
        this.strokeColor = strokeColor;
        this.colorCircleScale = colorCircleScale;
    }
    public ThumbDrawableState(Parcel parcel)
    {
        parcel.readInt();
        parcel.readInt();
        parcel.readInt();
        parcel.readFloat();
    }
    public static void writeThumbState(Parcel parcel, ThumbDrawableState state, int flags)
    {
        parcel.writeParcelable(state, flags);
    }
    public static ThumbDrawableState readThumbState(Parcel parcel)
    {
        Parcelable p = parcel.readParcelable(ThumbDrawableState.class.getClassLoader());
        if (p == null)
        {
            return ThumbDrawableState.EMPTY_STATE;
        }
        return (ThumbDrawableState) p;
    }
    public int getRadius()
    {
        return radius;
    }
    public int getThumbColor()
    {
        return thumbColor;
    }
    public int getStrokeColor()
    {
        return strokeColor;
    }
    public float getColorCircleScale()
    {
        return colorCircleScale;
    }
    @Override
    public int describeContents()
    {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(radius);
        dest.writeInt(thumbColor);
        dest.writeInt(strokeColor);
        dest.writeFloat(colorCircleScale);
    }

}
