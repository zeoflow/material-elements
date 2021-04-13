package com.zeoflow.material.elements.colorwheel.utils;

public class MathUtils
{

    public static final float PI = (float) Math.PI;

    public static float toRadians(float degrees)
    {
        return degrees / 180f * PI;
    }
    public static float toDegrees(float radians)
    {
        return radians * 180f / PI;
    }
    public static float ensureNumberWithinRange(float value, float start, float end)
    {
        if (value < start)
        {
            return start;
        } else if (value > end)
        {
            return end;
        }
        return value;
    }
    public static int ensureNumberWithinRange(int value, int start, int end)
    {
        if (value < start)
        {
            return start;
        } else if (value > end)
        {
            return end;
        }
        return value;
    }

}
