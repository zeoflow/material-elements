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

package com.zeoflow.material.elements.bottomsheet;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.shape.CornerFamily;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;

public class BottomDrawer extends FrameLayout {

    public static float offsetTrigger = 0.85f;
    private final ViewGroup container;
    private final Rect rect = new Rect();
    private final MaterialShapeDrawable backgroundDrawable;
    Activity activity;
    boolean isDarkOnNormal = false;
    boolean isDarkOnFull = true;
    @LayoutRes
    private int contentViewRes = View.NO_ID;
    private View content;
    private int marginSide;
    private int currentMargin;
    private int drawerBackground = 0;
    private float cornerRadius = 0f;
    private int extraPadding = 0;
    private int diffWithStatusBar = 0;
    private float translationView = 0f;
    private TranslationUpdater translationUpdater;
    private View handleView;
    private boolean isEnoughToFullExpand = false;
    private boolean isEnoughToCollapseExpand = false;
    private int heightPixels;
    private int fullHeight;
    private int collapseHeight;
    private boolean shouldDrawUnderStatus = false;
    private boolean shouldDrawUnderHandle = false;

    public BottomDrawer(@NonNull Context context) {
        this(context, null);
    }

    public BottomDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);
        initAttributes(context, attrs);

        if (contentViewRes != View.NO_ID) {
            content = LayoutInflater.from(context).inflate(contentViewRes, null);
            content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        backgroundDrawable = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder(
                        context,
                        attrs,
                        R.attr.bottomSheetStyle,
                        0
                ).build()
        );
        backgroundDrawable.setFillColor(ColorStateList.valueOf(drawerBackground));

        calculateDiffStatusBar(0);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point smallest = new Point();
        Point tallest = new Point();
        display.getCurrentSizeRange(smallest, tallest);

        heightPixels = tallest.y;
        fullHeight = heightPixels;
        collapseHeight = heightPixels / 2;

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    heightPixels = getContext().getResources().getDisplayMetrics().heightPixels;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        heightPixels -= insets.getSystemWindowInsetTop();
                    }

                    fullHeight = heightPixels;
                    collapseHeight = heightPixels / 2;

                    calculateDiffStatusBar(insets.getSystemWindowInsetTop());
                }
                insets.consumeSystemWindowInsets();
                return insets;
            }
        });

        container = new FrameLayout(context);
        container.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        super.addView(container);
        if (contentViewRes != View.NO_ID) {
            addView(content);
        }
        onSlide(0f);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomDrawer, 0, 0);
        extraPadding = a.getDimensionPixelSize(
                R.styleable.BottomDrawer_bottom_sheet_extra_padding,
                getResources().getDimensionPixelSize(R.dimen.bottom_sheet_extra_padding)
        );

        cornerRadius = a.getDimensionPixelSize(
                R.styleable.BottomDrawer_bottom_sheet_corner_radius,
                getResources().getDimensionPixelSize(R.dimen.bottom_sheet_corner_radius)
        );

        shouldDrawUnderStatus = a.getBoolean(
                R.styleable.BottomDrawer_should_draw_under_status_bar,
                false
        );

        shouldDrawUnderHandle = a.getBoolean(
                R.styleable.BottomDrawer_should_draw_content_under_handle_view,
                false
        );

        drawerBackground = a.getColor(
                R.styleable.BottomDrawer_bottom_drawer_background,
                ContextCompat.getColor(context, R.color.bottom_drawer_background)
        );

        contentViewRes = a.getResourceId(R.styleable.BottomDrawer_content_view, View.NO_ID);

        a.recycle();
    }

    @Override
    public void addView(View child) {
        container.addView(child);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!rect.isEmpty()) {
            backgroundDrawable.setBounds(rect);
            backgroundDrawable.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        left += currentMargin;
        rect.set(left, top, right - left, bottom - top);

        int measuredHeight = ((ViewGroup) container.getParent()).getMeasuredHeight();
        isEnoughToFullExpand = measuredHeight >= fullHeight;
        isEnoughToCollapseExpand = measuredHeight >= collapseHeight;

        backgroundDrawable.setInterpolation((!isEnoughToFullExpand) ? 1f : 0f);
    }

    public void onSlide(float value) {
        if (handleNonExpandableViews()) {
            return;
        }

        int oldMargin = currentMargin;
        if (value <= offsetTrigger) {
            backgroundDrawable.setInterpolation(1f);
            container.setTranslationY(0f);
            if (!shouldDrawUnderStatus) {
                if (handleView != null) {
                    handleView.setTranslationY(0f);
                }
            }
            if (translationUpdater != null) {
                translationUpdater.updateTranslation(0f);
            }
            currentMargin = (int) (backgroundDrawable.getInterpolation() * marginSide);
            if (oldMargin != currentMargin) {
                int currentLeft = rect.left - oldMargin + currentMargin;
                int currentRight = rect.right + rect.left - currentLeft;
                rect.set(currentLeft, rect.top, currentRight, rect.bottom);
                invalidate();
            }
            return;
        }
        float offset = ((value - offsetTrigger) * (1f / (1f - offsetTrigger)));
        translateViews(offset);
        if (translationUpdater != null) {
            translationUpdater.updateTranslation(offset);
        }
        backgroundDrawable.setInterpolation(1.0f - offset);
        currentMargin = (int) (backgroundDrawable.getInterpolation() * marginSide);
        if (oldMargin != currentMargin) {
            int currentLeft = rect.left - oldMargin + currentMargin;
            int currentRight = rect.right + rect.left - currentLeft;
            rect.set(currentLeft, rect.top, currentRight, rect.bottom);
        }
        invalidate();
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return !(darkness < 0.5);
    }

    private boolean handleNonExpandableViews() {
        if (!isEnoughToFullExpand) {
            backgroundDrawable.setInterpolation(1f);
            if (translationUpdater != null) {
                translationUpdater.updateTranslation(0f);
            }
            translateViews(0f);
            return true;
        }
        return false;
    }

    private void translateViews(float value) {
        translateViews(value, diffWithStatusBar);
    }

    public void globalTranslationViews() {
        if (isEnoughToFullExpand && getTop() < fullHeight - collapseHeight) {
            updateTranslationOnGlobalLayoutChanges();
        } else {
            if (container.getPaddingBottom() != 0) {
                container.setPadding(0, 0, 0, 0);
            }
            if (translationUpdater != null) {
                translationUpdater.updateTranslation(0f);
            }
            if (getTop() == fullHeight - collapseHeight || !rect.isEmpty()) {
                backgroundDrawable.setInterpolation(1f);
                translateViews(0f);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isEnoughToFullExpand) {
                updateTranslationOnGlobalLayoutChanges();
            }
        }
    }

    private void updateTranslationOnGlobalLayoutChanges() {
        //if view is expanded, we need to make a correct translation depends on change orientation
        int diff = diffWithStatusBar - getTop();

        float translationView = rangeContains(diff, 0, diffWithStatusBar) ? diff : 0f;

        translateViews(1f, (int) translationView);
        if (translationView == 0f && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (translationUpdater != null) {
                translationUpdater.updateTranslation(0f);
            }
            backgroundDrawable.setInterpolation(1f);
        } else if (getTop() == 0) {
            if (translationUpdater != null) {
                translationUpdater.updateTranslation(1f);
            }
            backgroundDrawable.setInterpolation(0f);
        }
    }

    private void translateViews(float offset, int height) {
        translationView = height * offset;
        container.setTranslationY(translationView);
        if (!shouldDrawUnderStatus) {
            if (handleView != null) {
                handleView.setTranslationY(translationView);
            }
        }

        int paddingBottom = (int) translationView;
        if (getTop() == 0 && translationView != 0f && container.getPaddingBottom() != paddingBottom) {
            container.setPadding(0, 0, 0, paddingBottom);
        }
    }

    private void calculateDiffStatusBar(int topInset) {
        diffWithStatusBar = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ? 0 : topInset;
        diffWithStatusBar += extraPadding;
    }

    private boolean rangeContains(int value, int first, int last) {
        return first <= value && value <= last;
    }

    public void changeCornerTreatment(@CornerFamily int cornerFamily) {
        ShapeAppearanceModel.Builder shapeAppearanceModel = new ShapeAppearanceModel().toBuilder();
        shapeAppearanceModel.setTopLeftCorner(cornerFamily, cornerRadius);
        shapeAppearanceModel.setTopRightCorner(cornerFamily, cornerRadius);

        backgroundDrawable.setShapeAppearanceModel(shapeAppearanceModel.build());
        backgroundDrawable.setInterpolation(1f);

        invalidate();
    }

    public void changeBackgroundColor(int color) {
        drawerBackground = color;
        backgroundDrawable.setTint(drawerBackground);
        invalidate();
    }

    public void changeExtraPadding(int extraPadding) {
        this.extraPadding = extraPadding;
        invalidate();
    }

    public void changeSideMargins(int margins) {
        marginSide = margins;
        currentMargin = marginSide;
        setExtensionsPadding(margins);
    }

    public void setAutoStatusBar(boolean isDarkOnNormal, boolean isDarkOnFull, Activity activity) {
        this.isDarkOnNormal = isDarkOnNormal;
        this.isDarkOnFull = isDarkOnFull;
        this.activity = activity;
    }

    private void setExtensionsPadding(int margins) {
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) handleView.getLayoutParams();
        int height = marginLayoutParams.height + marginLayoutParams.topMargin;
        int extensionPadding = margins + 20;
        if (!shouldDrawUnderHandle) {
            setMarginExtensionFunction(extensionPadding, height, extensionPadding, 0, container);
        } else {
            setMarginExtensionFunction(extensionPadding, 0, extensionPadding, 0, container);
        }
    }

    public void changeCornerRadius(float radius) {
        cornerRadius = radius;
        ShapeAppearanceModel.Builder shapeAppearanceModel = new ShapeAppearanceModel().toBuilder();
        shapeAppearanceModel.setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius);
        shapeAppearanceModel.setTopRightCorner(CornerFamily.ROUNDED, cornerRadius);

        backgroundDrawable.setShapeAppearanceModel(shapeAppearanceModel.build());
        backgroundDrawable.setInterpolation((!isEnoughToFullExpand) ? 1f : 0f);

        invalidate();
    }

    public void addHandleView(View newHandleView) {
        handleView = newHandleView;
        if (handleView == null) {
            return;
        }
        super.addView(handleView);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) handleView.getLayoutParams();
        int height = marginLayoutParams.height + marginLayoutParams.topMargin;
        if (!shouldDrawUnderHandle) {
            setMarginExtensionFunction(0, height, 0, 0, container);
        } else {
            setMarginExtensionFunction(0, 0, 0, 0, container);
        }

        translationUpdater = (TranslationUpdater) handleView;
    }

    private void setMarginExtensionFunction(int left, int top, int right, int bottom, View view) {
        MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

}
