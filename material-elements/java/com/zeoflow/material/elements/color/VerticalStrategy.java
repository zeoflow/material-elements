package com.zeoflow.material.elements.color;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;

import static com.zeoflow.material.elements.colorwheel.utils.MathUtils.ensureNumberWithinRange;

public class VerticalStrategy implements OrientationStrategy
{

    private final Rect rect = new Rect();
    private final PointF point = new PointF();

    @Override
    public GradientDrawable.Orientation getGradientOrientation()
    {
        return GradientDrawable.Orientation.BOTTOM_TOP;
    }
    @Override
    public Rect measure(ColorSeekBar view, int widthSpec, int heightSpec)
    {
        int preferredWidth = Math.max(view.getBarSize(), view.getThumbRadius() * 2) + view.getPaddingLeft() + view.getPaddingRight();
        int preferredHeight = View.MeasureSpec.getSize(heightSpec) + view.getPaddingTop() + view.getPaddingBottom();
        int finalWidth = View.resolveSize(preferredWidth, widthSpec);
        int finalHeight = View.resolveSize(preferredHeight, heightSpec);
        rect.set(0, 0, finalWidth, finalHeight);
        return rect;
    }
    @Override
    public Rect calculateGradientBounds(ColorSeekBar view)
    {
        int left = view.getPaddingLeft() + (view.getWidth() - view.getPaddingLeft() - view.getPaddingRight() - view.getBarSize()) / 2;
        int right = left + view.getBarSize();
        int top = view.getPaddingTop() + view.getThumbRadius();
        int bottom = view.getHeight() - view.getPaddingBottom() - view.getThumbRadius();
        rect.set(left, top, right, bottom);
        return rect;
    }
    @Override
    public float calculateOffsetOnMotionEvent(ColorSeekBar view, MotionEvent event, Rect barBounds)
    {
        int checkedY = ensureNumberWithinRange((int) event.getY(), barBounds.top, barBounds.bottom);
        float relativeY = (checkedY - barBounds.top);
        return 1f - relativeY / barBounds.height();
    }
    @Override
    public PointF calculateThumbCoordinates(ColorSeekBar view, Rect barBounds)
    {
        int y = (int) (barBounds.top + (1f - view.getOffset()) * barBounds.height());
        int x = (int) (view.getWidth() / 2f);
        point.set(x, y);
        return point;
    }

}
