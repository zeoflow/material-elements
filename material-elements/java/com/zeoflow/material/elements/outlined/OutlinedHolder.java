/*
 * Copyright (C) 2021 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.outlined;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews.RemoteView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

@RemoteView
public class OutlinedHolder extends LinearLayout
{

    private int mTotalLength = 0;
    private final int mBaselineAlignedChildIndex = -1;
    private boolean mUseLargestChild;
    private int mShowDividers;
    private int mDividerWidth;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mBaselineChildTop = 0;
    private final boolean mAllowInconsistentMeasurement;
    private static final boolean sRemeasureWeightedChildren = true;
    private float mWeightSum;
    private final int mGravity = Gravity.START | Gravity.TOP;

    public OutlinedHolder(Context context)
    {
        this(context, null);
    }

    public OutlinedHolder(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public OutlinedHolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }

    public OutlinedHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        final int version = context.getApplicationInfo().targetSdkVersion;
        mAllowInconsistentMeasurement = version <= Build.VERSION_CODES.M;

//        setPadding(0, 0, 0, 15);
        setPadding(15, 0, 15, 15);

        View view = new View(context);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
        addView(view);
    }

    public void setTopMargin(int margin)
    {
        if (getChildCount() > 0)
        {
            removeViewAt(0);
        }
        View view = new View(getContext());
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, margin));
        addView(view, 0);
    }

    @Override
    public void addView(View child)
    {
        super.addView(child);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    int getVirtualChildCount()
    {
        return getChildCount();
    }

    @Nullable
    View getVirtualChildAt(int index)
    {
        return getChildAt(index);
    }

    int measureNullChild(int childIndex)
    {
        return 0;
    }

    int getChildrenSkipCount(View child, int index)
    {
        return 0;
    }

    @LinearLayoutCompat.DividerMode
    public int getShowDividers()
    {
        return mShowDividers;
    }

    private boolean allViewsAreGoneBefore(int childIndex)
    {
        for (int i = childIndex - 1; i >= 0; i--)
        {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE)
            {
                return false;
            }
        }
        return true;
    }

    protected boolean hasDividerBeforeChildAt(int childIndex)
    {
        if (childIndex == getVirtualChildCount())
        {
            // Check whether the end divider should draw.
            return (mShowDividers & SHOW_DIVIDER_END) != 0;
        }
        boolean allViewsAreGoneBefore = allViewsAreGoneBefore(childIndex);
        if (allViewsAreGoneBefore)
        {
            // This is the first view that's not gone, check if beginning divider is enabled.
            return (mShowDividers & SHOW_DIVIDER_BEGINNING) != 0;
        } else
        {
            return (mShowDividers & SHOW_DIVIDER_MIDDLE) != 0;
        }
    }

    void measureChildBeforeLayout(View child, int childIndex,
                                  int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
                                  int totalHeight)
    {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth,
            heightMeasureSpec, totalHeight);
    }

    int getNextLocationOffset(View child)
    {
        return 0;
    }

    @SuppressLint("Range")
    void measureVertical(int widthMeasureSpec, int heightMeasureSpec)
    {
        mTotalLength = 0;
        int maxWidth = 0;
        int childState = 0;
        int alternativeMaxWidth = 0;
        int weightedMaxWidth = 0;
        boolean allFillParent = true;
        float totalWeight = 0;
        final int count = getVirtualChildCount();
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean matchWidth = false;
        boolean skippedMeasure = false;
        final int baselineChildIndex = mBaselineAlignedChildIndex;
        final boolean useLargestChild = mUseLargestChild;
        int largestChildHeight = Integer.MIN_VALUE;
        int consumedExcessSpace = 0;
        int nonSkippedChildCount = 0;
        // See how tall everyone is. Also remember max width.
        for (int i = 0; i < count; ++i)
        {
            final View child = getVirtualChildAt(i);
            if (child == null)
            {
                mTotalLength += measureNullChild(i);
                continue;
            }
            if (child.getVisibility() == View.GONE)
            {
                i += getChildrenSkipCount(child, i);
                continue;
            }
            nonSkippedChildCount++;
            if (hasDividerBeforeChildAt(i))
            {
                mTotalLength += mDividerHeight;
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
//            lp.setMargins(15 + lp.leftMargin, 0, 15 + lp.rightMargin, 0);
//            lp.setMargins(15, 0, 15, 0);
//            child.setLayoutParams(lp);
            totalWeight += lp.weight;
            final boolean useExcessSpace = lp.height == 0 && lp.weight > 0;
            if (heightMode == MeasureSpec.EXACTLY && useExcessSpace)
            {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + lp.topMargin + lp.bottomMargin);
                skippedMeasure = true;
            } else
            {
                if (useExcessSpace)
                {
                    // The heightMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal height. We'll restore the original height of 0
                    // after measurement.
                    lp.height = LayoutParams.WRAP_CONTENT;
                }
                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedHeight = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout(child, i, widthMeasureSpec, 0,
                    heightMeasureSpec, usedHeight);
                final int childHeight = child.getMeasuredHeight();
                if (useExcessSpace)
                {
                    // Restore the original height and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.height = 0;
                    consumedExcessSpace += childHeight;
                }
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                    lp.bottomMargin + getNextLocationOffset(child));
                if (useLargestChild)
                {
                    largestChildHeight = Math.max(childHeight, largestChildHeight);
                }
            }
            // if we are trying to use a child index for our baseline, the above
            // book keeping only works if there are no children above it with
            // weight.  fail fast to aid the developer.
            if (i < baselineChildIndex && lp.weight > 0)
            {
                throw new RuntimeException("A child of LinearLayout with index "
                    + "less than mBaselineAlignedChildIndex has weight > 0, which "
                    + "won't work.  Either remove the weight, or don't set "
                    + "mBaselineAlignedChildIndex.");
            }
            boolean matchWidthLocally = false;
            if (widthMode != MeasureSpec.EXACTLY && lp.width == LayoutParams.MATCH_PARENT)
            {
                // The width of the linear layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true;
                matchWidthLocally = true;
            }
            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            maxWidth = Math.max(maxWidth, measuredWidth);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
            if (lp.weight > 0)
            {
                /*
                 * Widths of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxWidth = Math.max(weightedMaxWidth,
                    matchWidthLocally ? margin : measuredWidth);
            } else
            {
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                    matchWidthLocally ? margin : measuredWidth);
            }
            i += getChildrenSkipCount(child, i);
        }
        if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt(count))
        {
            mTotalLength += mDividerHeight;
        }
        if (useLargestChild &&
            (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED))
        {
            mTotalLength = 0;
            for (int i = 0; i < count; ++i)
            {
                final View child = getVirtualChildAt(i);
                if (child == null)
                {
                    mTotalLength += measureNullChild(i);
                    continue;
                }
                if (child.getVisibility() == GONE)
                {
                    i += getChildrenSkipCount(child, i);
                    continue;
                }
                final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    child.getLayoutParams();
                // Account for negative margins
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + largestChildHeight +
                    lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
            }
        }
        // Add in our padding
        mTotalLength += getPaddingTop() + getPaddingBottom();
        int heightSize = mTotalLength;
        // Check against our minimum height
        heightSize = Math.max(heightSize, getSuggestedMinimumHeight());
        // Reconcile our calculated size with the heightMeasureSpec
        int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
        heightSize = heightSizeAndState & MEASURED_SIZE_MASK;
        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        int remainingExcess = heightSize - mTotalLength
            + (mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
        if (skippedMeasure
            || ((sRemeasureWeightedChildren || remainingExcess != 0) && totalWeight > 0.0f))
        {
            float remainingWeightSum = mWeightSum > 0.0f ? mWeightSum : totalWeight;
            mTotalLength = 0;
            for (int i = 0; i < count; ++i)
            {
                final View child = getVirtualChildAt(i);
                if (child == null || child.getVisibility() == View.GONE)
                {
                    continue;
                }
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final float childWeight = lp.weight;
                if (childWeight > 0)
                {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;
                    final int childHeight;
                    if (mUseLargestChild && heightMode != MeasureSpec.EXACTLY)
                    {
                        childHeight = largestChildHeight;
                    } else if (lp.height == 0 && (!mAllowInconsistentMeasurement
                        || heightMode == MeasureSpec.EXACTLY))
                    {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childHeight = share;
                    } else
                    {
                        // This child had some intrinsic height to which we
                        // need to add its share of excess space.
                        childHeight = child.getMeasuredHeight() + share;
                    }
                    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.max(0, childHeight), MeasureSpec.EXACTLY);
                    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                        lp.width);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    // Child may now not fit in vertical dimension.
                    childState = combineMeasuredStates(childState, child.getMeasuredState()
                        & (MEASURED_STATE_MASK >> MEASURED_HEIGHT_STATE_SHIFT));
                }
                final int margin = lp.leftMargin + lp.rightMargin;
                final int measuredWidth = child.getMeasuredWidth() + margin;
                maxWidth = Math.max(maxWidth, measuredWidth);
                boolean matchWidthLocally = widthMode != MeasureSpec.EXACTLY &&
                    lp.width == LayoutParams.MATCH_PARENT;
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                    matchWidthLocally ? margin : measuredWidth);
                allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                    lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
            }
            // Add in our padding
            mTotalLength += getPaddingTop() + getPaddingBottom();
            // TODO: Should we recompute the heightSpec based on the new total length?
        } else
        {
            alternativeMaxWidth = Math.max(alternativeMaxWidth,
                weightedMaxWidth);
            // We have no limit, so make all weighted views as tall as the largest child.
            // Children will have already been measured once.
            if (useLargestChild && heightMode != MeasureSpec.EXACTLY)
            {
                for (int i = 0; i < count; i++)
                {
                    final View child = getVirtualChildAt(i);
                    if (child == null || child.getVisibility() == View.GONE)
                    {
                        continue;
                    }
                    final LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) child.getLayoutParams();
                    float childExtra = lp.weight;
                    if (childExtra > 0)
                    {
                        child.measure(
                            MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                                MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(largestChildHeight,
                                MeasureSpec.EXACTLY));
                    }
                }
            }
        }
        if (!allFillParent && widthMode != MeasureSpec.EXACTLY)
        {
            maxWidth = alternativeMaxWidth;
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        // Check against our minimum width
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            heightSizeAndState);
        if (matchWidth)
        {
            forceUniformWidth(count, heightMeasureSpec);
        }
    }

    private void forceUniformWidth(int count, int heightMeasureSpec)
    {
        // Pretend that the linear layout has an exact size.
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
            MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i)
        {
            final View child = getVirtualChildAt(i);
            if (child != null && child.getVisibility() != GONE)
            {
                LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) child.getLayoutParams());
                if (lp.width == LayoutParams.MATCH_PARENT)
                {
                    // Temporarily force children to reuse their old measured height
                    // FIXME: this may not be right for something like wrapping text?
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    // Remeasue with new dimensions
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        layoutVertical(left, top, right, bottom);
    }

    void layoutVertical(int left, int top, int right, int bottom)
    {
        final int paddingLeft = getPaddingLeft();
        int childTop;
        int childLeft;
        // Where right end of child should go
        final int width = right - left;
        int childRight = width - getPaddingRight();
        // Space available for child
        int childSpace = width - paddingLeft - getPaddingRight();
        final int count = getVirtualChildCount();
        final int majorGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int minorGravity = mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        switch (majorGravity)
        {
            case Gravity.BOTTOM:
                // mTotalLength contains the padding already
                childTop = getPaddingTop() + bottom - top - mTotalLength;
                break;
            // mTotalLength contains the padding already
            case Gravity.CENTER_VERTICAL:
                childTop = getPaddingTop() + (bottom - top - mTotalLength) / 2;
                break;
            case Gravity.TOP:
            default:
                childTop = getPaddingTop();
                break;
        }
        for (int i = 0; i < count; i++)
        {
            final View child = getVirtualChildAt(i);
            if (child == null)
            {
                childTop += measureNullChild(i);
            } else if (child.getVisibility() != GONE)
            {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                final LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) child.getLayoutParams();
                int gravity = lp.gravity;
                if (gravity < 0)
                {
                    gravity = minorGravity;
                }
                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK)
                {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = paddingLeft + ((childSpace - childWidth) / 2)
                            + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = childRight - childWidth - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = paddingLeft + lp.leftMargin;
                        break;
                }
                if (hasDividerBeforeChildAt(i))
                {
                    childTop += mDividerHeight;
                }
                childTop += lp.topMargin;
                setChildFrame(child, childLeft, childTop + getLocationOffset(child),
                    childWidth, childHeight);
                childTop += childHeight + lp.bottomMargin + getNextLocationOffset(child);
                i += getChildrenSkipCount(child, i);
            }
        }
    }

    private void setChildFrame(View child, int left, int top, int width, int height)
    {
        child.layout(left, top, left + width, top + height);
    }

    int getLocationOffset(View child)
    {
        return 0;
    }
}
