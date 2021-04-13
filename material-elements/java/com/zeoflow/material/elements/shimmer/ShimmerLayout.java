/*
 * Copyright (C) 2021 ZeoFlow SRL
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

package com.zeoflow.material.elements.shimmer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.shimmer.Shimmer.ColorHighlightBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.zeoflow.material.elements.shimmer.ResourceExtension.dp2px;
import static com.zeoflow.material.elements.shimmer.ViewExtensions.invisible;
import static com.zeoflow.material.elements.shimmer.ViewExtensions.visibility;
import static com.zeoflow.material.elements.shimmer.ViewExtensions.visible;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class ShimmerLayout extends FrameLayout
{

    private final List<View> maskElements = new ArrayList<>();
    @Px
    public float radius = dp2px(8f, this);
    public Drawable drawable;
    public boolean isVeiled = false;
    public ShimmerFrameLayout shimmerContainer = new ShimmerFrameLayout(getContext());
    public Shimmer nonShimmer = new Shimmer.AlphaHighlightBuilder().setBaseAlpha(1.0f).setDropoff(1.0f).build();
    public Shimmer shimmer = new Shimmer.AlphaHighlightBuilder().build();
    public boolean shimmerEnable = true;
    public boolean defaultChildVisible = false;
    @ColorInt
    private int baseColor = Color.parseColor("#D4D4D4");
    @ColorInt
    private int highlightColor = Color.parseColor("#BDBDBD");
    @FloatRange(from = 0.0, to = 1.0)
    private float baseAlpha = 1.0f;
    @FloatRange(from = 0.0, to = 1.0)
    private float highlightAlpha = 1.0f;
    @FloatRange(from = 0.0, to = 1.0)
    private float dropOff = 0.5f;
    @LayoutRes
    private int layout = -1;

    public ShimmerLayout(@NonNull Context context)
    {
        this(context, null);
    }
    public ShimmerLayout(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    public ShimmerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        this(context, attrs, defStyleAttr, 0);
    }
    public ShimmerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttrs(attrs);
        onCreate();
    }
    public int getLayout()
    {
        return layout;
    }
    public void setLayout(@LayoutRes int layout)
    {
        this.layout = layout;
        invalidateLayout(layout);
    }
    /**
     * Remove previous views and inflate a new layout using an inflated view.
     */
    public void setLayout(View layout)
    {
        removeAllViews();
        addView(layout);
        onFinishInflate();
    }
    public void setShimmerContainer(Shimmer shimmer)
    {
        this.shimmer = shimmer;
        shimmerContainer.setShimmer(shimmer);
    }
    public void setEnabled(boolean enabled)
    {
        this.shimmerEnable = enabled;
        if (enabled)
        {
            shimmerContainer.setShimmer(shimmer);
        } else
        {
            shimmerContainer.setShimmer(nonShimmer);
        }
    }
    private void getAttrs(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ShimmerLayout);
        try
        {
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_veiled))
                isVeiled = a.getBoolean(R.styleable.ShimmerLayout_shimmerLayout_veiled, isVeiled);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_layout))
                layout = a.getResourceId(R.styleable.ShimmerLayout_shimmerLayout_layout, -1);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_drawable))
                drawable = a.getDrawable(R.styleable.ShimmerLayout_shimmerLayout_drawable);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_radius))
                radius = a.getDimension(R.styleable.ShimmerLayout_shimmerLayout_radius, radius);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_shimmerEnable))
                shimmerEnable = a.getBoolean(R.styleable.ShimmerLayout_shimmerLayout_shimmerEnable, shimmerEnable);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_baseColor))
                baseColor = a.getColor(R.styleable.ShimmerLayout_shimmerLayout_baseColor, baseColor);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_highlightColor))
                highlightColor =
                        a.getColor(R.styleable.ShimmerLayout_shimmerLayout_highlightColor, highlightColor);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_baseAlpha))
                baseAlpha = a.getFloat(R.styleable.ShimmerLayout_shimmerLayout_baseAlpha, baseAlpha);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_highlightAlpha))
                highlightAlpha =
                        a.getFloat(R.styleable.ShimmerLayout_shimmerLayout_highlightAlpha, highlightAlpha);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_dropOff))
                dropOff = a.getFloat(R.styleable.ShimmerLayout_shimmerLayout_dropOff, dropOff);
            if (a.hasValue(R.styleable.ShimmerLayout_shimmerLayout_defaultChildVisible))
                defaultChildVisible =
                        a.getBoolean(R.styleable.ShimmerLayout_shimmerLayout_defaultChildVisible, defaultChildVisible);
        } finally
        {
            a.recycle();
        }
    }
    private void onCreate()
    {
        invisible(this.shimmerContainer);
        this.shimmer = new ColorHighlightBuilder()
                .setBaseColor(baseColor)
                .setHighlightColor(highlightColor)
                .setBaseAlpha(baseAlpha)
                .setHighlightAlpha(highlightAlpha)
                .setDropoff(dropOff)
                .setAutoStart(false)
                .build();
        setEnabled(this.shimmerEnable);
    }
    /**
     * Remove previous views and inflate a new layout using layout resource
     */
    private void invalidateLayout(@LayoutRes int layout)
    {
        setLayout(LayoutInflater.from(getContext()).inflate(layout, this, false));
    }
    /**
     * Invokes addMaskElements method after inflating.
     */
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        removeView(shimmerContainer);
        addView(shimmerContainer);
        addMaskElements(this);
    }

    /**
     * Adds a masked skeleton views depending on the view tree structure of the [ShimmerLayout].
     * This method will ignore the ViewGroup for creating masked skeletons.
     *
     * @param parent A parent view for creating the masked skeleton.
     */
    private void addMaskElements(final ViewGroup parent)
    {
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            final View child = parent.getChildAt(i);
            child.post(() ->
            {
                if (child instanceof ViewGroup)
                {
                    addMaskElements((ViewGroup) child);
                } else
                {
                    float marginX = 0f;
                    float marginY = 0f;
                    ViewParent grandParent = parent.getParent();
                    while (!(grandParent instanceof ShimmerLayout))
                    {
                        if (grandParent instanceof ViewGroup)
                        {
                            ViewGroup.LayoutParams params = ((ViewGroup) grandParent).getLayoutParams();
                            if (params instanceof MarginLayoutParams)
                            {
                                marginX += ((ViewGroup) grandParent).getX();
                                marginY += ((ViewGroup) grandParent).getY();
                            }
                            grandParent = grandParent.getParent();
                        } else
                        {
                            break;
                        }
                    }

                    View view = new View(getContext());
                    view.setLayoutParams(new ViewGroup.LayoutParams(child.getWidth(), child.getHeight()));
                    view.setX(marginX + parent.getX() + child.getX());
                    view.setY(marginY + parent.getY() + child.getY());
                    view.setBackgroundColor(baseColor);
                    Drawable background = drawable;
                    if (background == null)
                    {
                        GradientDrawable backgroundGD = new GradientDrawable();
                        backgroundGD.setColor(Color.DKGRAY);
                        backgroundGD.setCornerRadius(radius);
                        background = backgroundGD;
                    }
                    view.setBackground(background);
                    maskElements.add(view);
                    shimmerContainer.addView(view);
                }
            });
        }

        // Invalidate the whole masked view.
        invalidate();

        // Auto veiled
        this.isVeiled = !this.isVeiled;
        if (this.isVeiled)
        {
            unVeil();
        } else
        {
            veil();
        }
    }

    /**
     * Make appear the mask.
     */
    public void veil()
    {
        if (!this.isVeiled)
        {
            this.isVeiled = true;
            startShimmer();
            invalidate();
        }
    }

    /**
     * Make disappear the mask.
     */
    public void unVeil()
    {
        if (this.isVeiled)
        {
            this.isVeiled = false;
            stopShimmer();
            invalidate();
        }
    }

    /**
     * Starts the shimmer animation.
     */
    public void startShimmer()
    {
        visible(this.shimmerContainer);
        if (this.shimmerEnable)
        {
            this.shimmerContainer.startShimmer();
        }
        if (!this.defaultChildVisible)
        {
            setChildVisibility(false);
        }
    }

    /**
     * Stops the shimmer animation.
     */
    public void stopShimmer()
    {
        invisible(this.shimmerContainer);
        this.shimmerContainer.stopShimmer();
        if (!this.defaultChildVisible)
        {
            setChildVisibility(true);
        }
    }

    private void setChildVisibility(boolean visible)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            if (child != this.shimmerContainer)
            {
                visibility(child, visible);
            }
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        this.shimmerContainer.invalidate();
    }

}
