package com.zeoflow.material.elements.colorwheel.utils;

import android.graphics.Color;

import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.ensureNumberWithinRange;

public class HsvColor
{

    private final float[] hsv;

    public HsvColor(float hue, float saturation, float value)
    {
        hsv = new float[3];
        hsv[0] = ensureHueWithinRange(hue);
        hsv[1] = ensureSaturationWithinRange(saturation);
        hsv[2] = ensureValueWithinRange(value);
    }

    private float ensureHueWithinRange(float hue)
    {
        return ensureNumberWithinRange(hue, 0f, 360f);
    }

    private float ensureValueWithinRange(float value)
    {
        return ensureNumberWithinRange(value, 0f, 1f);
    }

    private float ensureSaturationWithinRange(float saturation)
    {
        return ensureNumberWithinRange(saturation, 0f, 1f);
    }

    public float getHue()
    {
        return hsv[0];
    }
    public void setHue(float hue)
    {
        hsv[0] = ensureHueWithinRange(hue);
    }
    public float getSaturation()
    {
        return hsv[1];
    }
    public void setSaturation(float saturation)
    {
        hsv[1] = ensureSaturationWithinRange(saturation);
    }
    public float getValue()
    {
        return hsv[2];
    }
    public void setValue(float value)
    {
        hsv[2] = ensureValueWithinRange(value);
    }
    public int getRGB()
    {
        return Color.HSVToColor(hsv);
    }
    public void setRGB(int rgb)
    {
        Color.colorToHSV(rgb, hsv);
    }
    public void set(float hue, float saturation, float value)
    {
        hsv[0] = ensureHueWithinRange(hue);
        hsv[1] = ensureSaturationWithinRange(saturation);
        hsv[2] = ensureValueWithinRange(value);
    }

}
