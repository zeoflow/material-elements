package com.zeoflow.material.elements.gradientseekbar;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;

import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.ensureNumberWithinRange;

public class HorizontalStrategy implements OrientationStrategy
{

    private final Rect rect = new Rect();
    private final PointF point = new PointF();

    @Override
    public GradientDrawable.Orientation getGradientOrientation()
    {
        return GradientDrawable.Orientation.LEFT_RIGHT;
    }
    @Override
    public Rect measure(GradientSeekBar view, int widthSpec, int heightSpec)
    {
        int preferredWidth = View.MeasureSpec.getSize(widthSpec) + view.getPaddingLeft() + view.getPaddingRight();
        int preferredHeight = Math.max(view.getBarSize(), view.getThumbRadius() * 2) + view.getPaddingTop() + view.getPaddingBottom();
        int finalWidth = View.resolveSize(preferredWidth, widthSpec);
        int finalHeight = View.resolveSize(preferredHeight, heightSpec);
        rect.set(0, 0, finalWidth, finalHeight);
        return rect;
    }
    @Override
    public Rect calculateGradientBounds(GradientSeekBar view)
    {
        int left = view.getPaddingLeft() + view.getThumbRadius();
        int right = view.getWidth() - view.getPaddingRight() - view.getThumbRadius();
        int top = view.getPaddingTop() + (view.getHeight() - view.getPaddingTop() - view.getPaddingRight() - view.getBarSize()) / 2;
        int bottom = top + view.getBarSize();
        rect.set(left, top, right, bottom);
        return rect;
    }
    @Override
    public float calculateOffsetOnMotionEvent(GradientSeekBar view, MotionEvent event, Rect barBounds)
    {
        int checkedX = ensureNumberWithinRange((int) event.getX(), barBounds.left, barBounds.right);
        float relativeX = (checkedX - barBounds.left);
        return relativeX / barBounds.width();
    }
    @Override
    public PointF calculateThumbCoordinates(GradientSeekBar view, Rect barBounds)
    {
        float x = (barBounds.left + view.getOffset() * barBounds.width());
        float y = view.getHeight() / 2f;
        point.set(x, y);
        return point;
    }

}
