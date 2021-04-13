package com.zeoflow.material.elements.colorwheel.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;

import java.util.Locale;

public class ColorUtils
{

    public static final int MAX_ALPHA = 255;

    public static int[] HUE_COLORS = {
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA,
            Color.RED
    };

    public static int[] SATURATION_COLORS = {
            Color.WHITE,
            setColorAlpha(Color.WHITE, 0)
    };

    public static int setColorAlpha(int argb, int alpha)
    {
        return Color.argb(
                alpha,
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    public static int interpolateColorLinear(int startColor, int endColor, float offset)
    {
        return Color.argb(
                (int) (Color.alpha(startColor) + offset * (Color.alpha(endColor) - Color.alpha(startColor))),
                (int) (Color.red(startColor) + offset * (Color.red(endColor) - Color.red(startColor))),
                (int) (Color.green(startColor) + offset * (Color.green(endColor) - Color.green(startColor))),
                (int) (Color.blue(startColor) + offset * (Color.blue(endColor) - Color.blue(startColor)))
        );
    }
    /**
     * changes color to string hex code.
     */
    public static String getHexCode(@ColorInt int color)
    {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "%02X%02X%02X%02X", a, r, g, b);
    }

    /**
     * changes color to argb integer array.
     */
    public static int[] getColorARGB(@ColorInt int color)
    {
        int[] argb = new int[4];
        argb[0] = Color.alpha(color);
        argb[1] = Color.red(color);
        argb[2] = Color.green(color);
        argb[3] = Color.blue(color);
        return argb;
    }

}
