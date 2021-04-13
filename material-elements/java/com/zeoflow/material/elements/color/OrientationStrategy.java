package com.zeoflow.material.elements.color;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;

public interface OrientationStrategy
{

    GradientDrawable.Orientation getGradientOrientation();

    Rect measure(ColorSeekBar view, int widthSpec, int heightSpec);

    Rect calculateGradientBounds(ColorSeekBar view);

    float calculateOffsetOnMotionEvent(ColorSeekBar view, MotionEvent event, Rect barBounds);

    PointF calculateThumbCoordinates(ColorSeekBar view, Rect barBounds);

}
