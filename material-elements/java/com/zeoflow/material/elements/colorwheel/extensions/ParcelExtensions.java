package com.zeoflow.material.elements.colorwheel.extensions;

import android.os.Build;
import android.os.Parcel;

public class ParcelExtensions
{

    public static void writeBooleanCompat(Parcel parcel, boolean value)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            parcel.writeBoolean(value);
        } else
        {
            parcel.writeInt(value ? 1 : 0);
        }
    }

    public static boolean readBooleanCompat(Parcel parcel)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return parcel.readBoolean();
        } else
        {
            return parcel.readInt() == 1;
        }
    }

}
