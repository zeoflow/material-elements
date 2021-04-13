package com.zeoflow.material.elements.gradientseekbar;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;

public interface OrientationStrategy
{

    GradientDrawable.Orientation getGradientOrientation();

    Rect measure(GradientSeekBar view, int widthSpec, int heightSpec);

    Rect calculateGradientBounds(GradientSeekBar view);

    float calculateOffsetOnMotionEvent(GradientSeekBar view, MotionEvent event, Rect barBounds);

    PointF calculateThumbCoordinates(GradientSeekBar view, Rect barBounds);

}
