package com.zeoflow.material.elements.outlined;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.customview.view.AbsSavedState;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.CheckableImageButton;
import com.zeoflow.material.elements.internal.CollapsingTextHelper;
import com.zeoflow.material.elements.internal.DescendantOffsetUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class OutlinedLayout extends LinearLayout
{

    public static final int BOX_BACKGROUND_NONE = 0;
    public static final int BOX_BACKGROUND_FILLED = 1;
    public static final int BOX_BACKGROUND_OUTLINE = 2;
    private static final int DEF_STYLE_RES = R.style.Design_OutlinedLayout;
    private static final String LOG_TAG = "OutlinedLayout";
    final CollapsingTextHelper collapsingTextHelper = new CollapsingTextHelper(this);
    @NonNull
    private final FrameLayout inputFrame;
    private final IndicatorViewController indicatorViewController = new IndicatorViewController(this);
    private final int boxLabelCutoutPaddingPx;
    private final int boxCollapsedPaddingTopPx;
    private final Rect tmpRect = new Rect();
    private final Rect tmpBoundsRect = new Rect();
    private final RectF tmpRectF = new RectF();
    View linearLayout;
    private boolean titleEnabled;
    private CharSequence title;
    @Nullable
    private MaterialShapeDrawable boxBackground;
    @Nullable
    private MaterialShapeDrawable boxUnderline;
    @NonNull
    private ShapeAppearanceModel shapeAppearanceModel;
    @BoxBackgroundMode
    private int boxBackgroundMode;
    private int boxStrokeWidthPx;
    private int boxStrokeWidthDefaultPx;
    private int boxStrokeWidthFocusedPx;
    @ColorInt
    private int boxStrokeColour;
    @ColorInt
    private int boxBackgroundColour;
    private Typeface typeface;
    private ColorStateList hintTextColour;
    private ColorStateList textColour;
    @ColorInt
    private int strokeColour;
    private ColorStateList strokeErrorColour;
    @ColorInt
    private int filledBackgroundColour;
    // Only used for testing
    private boolean hintExpanded;
    private boolean inDrawableStateChanged;

    public OutlinedLayout(@NonNull Context context)
    {
        this(context, null);
    }

    public OutlinedLayout(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, R.attr.textInputStyle);
    }

    public OutlinedLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();

        setOrientation(VERTICAL);
        setWillNotDraw(false);
        setAddStatesFromChildren(true);

        inputFrame = new FrameLayout(context);
        inputFrame.setAddStatesFromChildren(true);
        addView(inputFrame);

        collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
        collapsingTextHelper.setPositionInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
        collapsingTextHelper.setCollapsedTextGravity(Gravity.TOP | GravityCompat.START);

        final TintTypedArray a =
            ThemeEnforcement.obtainTintedStyledAttributes(
                context,
                attrs,
                R.styleable.TextInputLayout,
                defStyleAttr,
                DEF_STYLE_RES,
                R.styleable.TextInputLayout_hintTextAppearance);

        final TintTypedArray b =
            ThemeEnforcement.obtainTintedStyledAttributes(
                context,
                attrs,
                R.styleable.CollapsingToolbarLayout,
                defStyleAttr,
                DEF_STYLE_RES);

        final TintTypedArray c =
            ThemeEnforcement.obtainTintedStyledAttributes(
                context,
                attrs,
                R.styleable.OutlinedLayout,
                defStyleAttr,
                DEF_STYLE_RES,
                R.styleable.OutlinedLayout_titleAppearance);

        titleEnabled = b.getBoolean(R.styleable.CollapsingToolbarLayout_titleEnabled, false);
        setTitle(b.getText(R.styleable.CollapsingToolbarLayout_title));

        shapeAppearanceModel =
            ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();

        boxLabelCutoutPaddingPx =
            context
                .getResources()
                .getDimensionPixelOffset(R.dimen.mtrl_textinput_box_label_cutout_padding);
        boxCollapsedPaddingTopPx =
            a.getDimensionPixelOffset(R.styleable.TextInputLayout_boxCollapsedPaddingTop, 0);

        boxStrokeWidthDefaultPx =
            a.getDimensionPixelSize(
                R.styleable.TextInputLayout_boxStrokeWidth,
                context
                    .getResources()
                    .getDimensionPixelSize(R.dimen.mtrl_textinput_box_stroke_width_default));
        boxStrokeWidthFocusedPx =
            a.getDimensionPixelSize(
                R.styleable.TextInputLayout_boxStrokeWidthFocused,
                context
                    .getResources()
                    .getDimensionPixelSize(R.dimen.mtrl_textinput_box_stroke_width_focused));
        boxStrokeWidthPx = boxStrokeWidthDefaultPx;

        float boxCornerRadiusTopStart =
            a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusTopStart, -1f);
        float boxCornerRadiusTopEnd =
            a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusTopEnd, -1f);
        float boxCornerRadiusBottomEnd =
            a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusBottomEnd, -1f);
        float boxCornerRadiusBottomStart =
            a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusBottomStart, -1f);
        ShapeAppearanceModel.Builder shapeBuilder = shapeAppearanceModel.toBuilder();
        if (boxCornerRadiusTopStart >= 0)
        {
            shapeBuilder.setTopLeftCornerSize(boxCornerRadiusTopStart);
        }
        if (boxCornerRadiusTopEnd >= 0)
        {
            shapeBuilder.setTopRightCornerSize(boxCornerRadiusTopEnd);
        }
        if (boxCornerRadiusBottomEnd >= 0)
        {
            shapeBuilder.setBottomRightCornerSize(boxCornerRadiusBottomEnd);
        }
        if (boxCornerRadiusBottomStart >= 0)
        {
            shapeBuilder.setBottomLeftCornerSize(boxCornerRadiusBottomStart);
        }
        shapeAppearanceModel = shapeBuilder.build();

        ColorStateList filledBackgroundColorStateList =
            MaterialResources.getColorStateList(
                context, a, R.styleable.TextInputLayout_boxBackgroundColor);
        if (filledBackgroundColorStateList != null)
        {
            filledBackgroundColour = filledBackgroundColorStateList.getDefaultColor();
            boxBackgroundColour = filledBackgroundColour;
        } else
        {
            boxBackgroundColour = Color.TRANSPARENT;
            filledBackgroundColour = Color.TRANSPARENT;
        }

        ColorStateList boxStrokeColorStateList =
            MaterialResources.getColorStateList(context, a, R.styleable.TextInputLayout_boxStrokeColor);
        // Default values for stroke colors if boxStrokeColorStateList is not stateful
        strokeColour = a.getColor(R.styleable.TextInputLayout_boxStrokeColor, Color.TRANSPARENT);
        // Values from boxStrokeColorStateList
        if (boxStrokeColorStateList != null)
        {
            setBoxStrokeColorStateList(boxStrokeColorStateList);
        }
        if (a.hasValue(R.styleable.TextInputLayout_boxStrokeErrorColor))
        {
            setBoxStrokeErrorColor(
                MaterialResources.getColorStateList(
                    context, a, R.styleable.TextInputLayout_boxStrokeErrorColor));
        }

        final int hintAppearance = c.getResourceId(R.styleable.OutlinedLayout_titleAppearance, -1);
        if (hintAppearance != -1)
        {
            setHintTextAppearance(c.getResourceId(R.styleable.OutlinedLayout_titleAppearance, 0));
        }

        final int errorTextAppearance =
            a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
        final CharSequence errorContentDescription =
            a.getText(R.styleable.TextInputLayout_errorContentDescription);
        final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);

        setBoxBackgroundMode(
            a.getInt(R.styleable.TextInputLayout_boxBackgroundMode, BOX_BACKGROUND_NONE));

        setErrorEnabled(errorEnabled);
        setErrorTextAppearance(errorTextAppearance);
        setErrorContentDescription(errorContentDescription);

        if (a.hasValue(R.styleable.TextInputLayout_errorTextColor))
        {
            setErrorTextColor(a.getColorStateList(R.styleable.TextInputLayout_errorTextColor));
        }
        if (c.hasValue(R.styleable.OutlinedLayout_titleColor))
        {
            setHintTextColor(c.getColorStateList(R.styleable.OutlinedLayout_titleColor));
        }

        setEnabled(a.getBoolean(R.styleable.TextInputLayout_android_enabled, true));

        a.recycle();
        b.recycle();
        c.recycle();

        // For accessibility, consider TextInputLayout itself to be a simple container for an EditText,
        // and do not expose it to accessibility services.
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private static void recursiveSetEnabled(@NonNull final ViewGroup vg, final boolean enabled)
    {
        for (int i = 0, count = vg.getChildCount(); i < count; i++)
        {
            final View child = vg.getChildAt(i);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup)
            {
                recursiveSetEnabled((ViewGroup) child, enabled);
            }
        }
    }

    private static void setIconOnClickListener(
        @NonNull CheckableImageButton iconView,
        @Nullable OnClickListener onClickListener,
        @Nullable OnLongClickListener onLongClickListener)
    {
        iconView.setOnClickListener(onClickListener);
        setIconClickable(iconView, onLongClickListener);
    }

    private static void setIconOnLongClickListener(
        @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener)
    {
        iconView.setOnLongClickListener(onLongClickListener);
        setIconClickable(iconView, onLongClickListener);
    }

    private static void setIconClickable(
        @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener)
    {
        boolean iconClickable = ViewCompat.hasOnClickListeners(iconView);
        boolean iconLongClickable = onLongClickListener != null;
        boolean iconFocusable = iconClickable || iconLongClickable;
        iconView.setFocusable(iconFocusable);
        iconView.setClickable(iconClickable);
        iconView.setPressable(iconClickable);
        iconView.setLongClickable(iconLongClickable);
        ViewCompat.setImportantForAccessibility(
            iconView,
            iconFocusable
                ? ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
                : ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    public void addView(
        @NonNull View child, int index, @NonNull final ViewGroup.LayoutParams params)
    {
        if (child instanceof TextView)
        {
            // Make sure that the TextView is vertically at the bottom, so that it sits on the
            // TextView's underline
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
            flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
            inputFrame.addView(child, flp);

            // Now use the TextView's LayoutParams as our own and update them to make enough space
            // for the label
            inputFrame.setLayoutParams(params);
            updateInputLayoutMargins();

            setLinearLayout((TextView) child);
        } else if (child instanceof OutlinedHolder)
        {
            // Make sure that the OutlinedHolder is vertically at the bottom, so that it sits on the
            // OutlinedHolder's underline
            OutlinedHolder outlinedHolder = (OutlinedHolder) child;
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
            flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
            outlinedHolder.setTopMargin((int) (calculateLabelMarginTop() * 1.5));
            inputFrame.addView(outlinedHolder, flp);

            // Now use the OutlinedHolder's LayoutParams as our own and update them to make enough space
            // for the label
            inputFrame.setLayoutParams(params);
            updateInputLayoutMargins();

            setLinearLayout((OutlinedHolder) outlinedHolder);
        } else
        {
            // Carry on adding the View...
            super.addView(child, index, params);
        }
    }

    @NonNull
    MaterialShapeDrawable getBoxBackground()
    {
        if (boxBackgroundMode == BOX_BACKGROUND_FILLED || boxBackgroundMode == BOX_BACKGROUND_OUTLINE)
        {
            return boxBackground;
        }
        throw new IllegalStateException();
    }

    /**
     * Get the box background mode (filled, outline, or none).
     *
     * <p>May be one of {@link #BOX_BACKGROUND_NONE}, {@link #BOX_BACKGROUND_FILLED}, or {@link
     * #BOX_BACKGROUND_OUTLINE}.
     */
    @BoxBackgroundMode
    public int getBoxBackgroundMode()
    {
        return boxBackgroundMode;
    }

    /**
     * Set the box background mode (filled, outline, or none).
     *
     * <p>May be one of {@link #BOX_BACKGROUND_NONE}, {@link #BOX_BACKGROUND_FILLED}, or {@link
     * #BOX_BACKGROUND_OUTLINE}.
     *
     * <p>Note: This method defines TextInputLayout's internal behavior (for example, it allows the
     * hint to be displayed inline with the stroke in a cutout), but doesn't set all attributes that
     * are set in the styles provided for the box background modes. To achieve the look of an outlined
     * or filled text field, supplement this method with other methods that modify the box, such as
     * {@link #setBoxStrokeColour(int)} and {@link #setBoxBackgroundColour(int)}.
     *
     * @param boxBackgroundMode box's background mode
     * @throws IllegalArgumentException if boxBackgroundMode is not a @BoxBackgroundMode constant
     */
    public void setBoxBackgroundMode(@BoxBackgroundMode int boxBackgroundMode)
    {
        if (boxBackgroundMode == this.boxBackgroundMode)
        {
            return;
        }
        this.boxBackgroundMode = boxBackgroundMode;
        if (linearLayout != null)
        {
            onApplyBoxBackgroundMode();
        }
    }

    private void onApplyBoxBackgroundMode()
    {
        assignBoxBackgroundByMode();
        setEditTextBoxBackground();
        updateTextInputBoxState();
        if (boxBackgroundMode != BOX_BACKGROUND_NONE)
        {
            updateInputLayoutMargins();
        }
    }

    private void assignBoxBackgroundByMode()
    {
        switch (boxBackgroundMode)
        {
            case BOX_BACKGROUND_FILLED:
                boxBackground = new MaterialShapeDrawable(shapeAppearanceModel);
                boxUnderline = new MaterialShapeDrawable();
                break;
            case BOX_BACKGROUND_OUTLINE:
                if (titleEnabled && !(boxBackground instanceof CutoutDrawable))
                {
                    boxBackground = new CutoutDrawable(shapeAppearanceModel);
                } else
                {
                    boxBackground = new MaterialShapeDrawable(shapeAppearanceModel);
                }
                boxUnderline = null;
                break;
            case BOX_BACKGROUND_NONE:
                boxBackground = null;
                boxUnderline = null;
                break;
            default:
                throw new IllegalArgumentException(
                    boxBackgroundMode + " is illegal; only @BoxBackgroundMode constants are supported.");
        }
    }

    private void setEditTextBoxBackground()
    {
        // Set the EditText background to boxBackground if we should use that as the box background.
        if (shouldUseEditTextBackgroundForBoxBackground())
        {
            ViewCompat.setBackground(linearLayout, boxBackground);
        }
    }

    private boolean shouldUseEditTextBackgroundForBoxBackground()
    {
        // When the text field's EditText's background is null, use the EditText's background for the
        // box background.
        return linearLayout != null
            && boxBackground != null
            && linearLayout.getBackground() == null
            && boxBackgroundMode != BOX_BACKGROUND_NONE;
    }

    /**
     * Set the resource dimension to use for the box's stroke when in outline box mode, or for the
     * underline stroke in filled mode.
     *
     * @param boxStrokeWidthResId the resource dimension to use for the box's stroke width
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_boxStrokeWidth
     * @see #setBoxStrokeWidth(int)
     * @see #getBoxStrokeWidth()
     */
    public void setBoxStrokeWidthResource(@DimenRes int boxStrokeWidthResId)
    {
        setBoxStrokeWidth(getResources().getDimensionPixelSize(boxStrokeWidthResId));
    }

    /**
     * Returns the box's stroke width.
     *
     * @return the value used for the box's stroke width
     * @see #setBoxStrokeWidth(int)
     */
    public int getBoxStrokeWidth()
    {
        return boxStrokeWidthDefaultPx;
    }

    /**
     * Set the value to use for the box's stroke when in outline box mode, or for the underline stroke
     * in filled mode.
     *
     * @param boxStrokeWidth the value to use for the box's stroke
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_boxStrokeWidth
     * @see #getBoxStrokeWidth()
     */
    public void setBoxStrokeWidth(int boxStrokeWidth)
    {
        boxStrokeWidthDefaultPx = boxStrokeWidth;
        updateTextInputBoxState();
    }

    /**
     * Set the resource dimension to use for the focused box's stroke when in outline box mode, or for
     * the focused underline stroke in filled mode.
     *
     * @param boxStrokeWidthFocusedResId the resource dimension to use for the box's stroke width
     *                                   when focused
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_boxStrokeWidthFocused
     * @see #setBoxStrokeWidthFocused(int)
     * @see #getBoxStrokeWidthFocused()
     */
    public void setBoxStrokeWidthFocusedResource(@DimenRes int boxStrokeWidthFocusedResId)
    {
        setBoxStrokeWidthFocused(getResources().getDimensionPixelSize(boxStrokeWidthFocusedResId));
    }

    /**
     * Returns the box's stroke focused width.
     *
     * @return the value used for the box's stroke width when focused
     * @see #setBoxStrokeWidthFocused(int)
     */
    public int getBoxStrokeWidthFocused()
    {
        return boxStrokeWidthFocusedPx;
    }

    /**
     * Set the value to use for the focused box's stroke when in outline box mode, or for the focused
     * underline stroke in filled mode.
     *
     * @param boxStrokeWidthFocused the value to use for the box's stroke when focused
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_boxStrokeWidthFocused
     * @see #getBoxStrokeWidthFocused()
     */
    public void setBoxStrokeWidthFocused(int boxStrokeWidthFocused)
    {
        boxStrokeWidthFocusedPx = boxStrokeWidthFocused;
        updateTextInputBoxState();
    }

    /**
     * Returns the box's stroke focused color.
     *
     * @return the color used for the box's stroke when focused
     * @see #setBoxStrokeColour(int)
     */
    public int getBoxStrokeColour()
    {
        return strokeColour;
    }

    /**
     * Set the outline box's stroke focused color.
     *
     * <p>Calling this method when not in outline box mode will do nothing.
     *
     * @param boxStrokeColour the color to use for the box's stroke when focused
     * @see #getBoxStrokeColour()
     */
    public void setBoxStrokeColour(@ColorInt int boxStrokeColour)
    {
        if (strokeColour != boxStrokeColour)
        {
            strokeColour = boxStrokeColour;
            updateTextInputBoxState();
        }
    }

    /**
     * Set the box's stroke color state list.
     *
     * @param boxStrokeColorStateList the color state list to use for the box's stroke
     */
    public void setBoxStrokeColorStateList(@NonNull ColorStateList boxStrokeColorStateList)
    {
        if (boxStrokeColorStateList.isStateful())
        {
            strokeColour =
                boxStrokeColorStateList.getColorForState(
                    new int[]{android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
        } else if (strokeColour != boxStrokeColorStateList.getDefaultColor())
        {
            // If attribute boxStrokeColor is not a color state list but only a single value, its value
            // will be applied to the box's focus state.
            strokeColour = boxStrokeColorStateList.getDefaultColor();
        }
        updateTextInputBoxState();
    }

    /**
     * Returns the box's stroke color when an error is being displayed.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_boxStrokeErrorColor
     * @see #setBoxStrokeErrorColor(ColorStateList)
     */
    @Nullable
    public ColorStateList getBoxStrokeErrorColor()
    {
        return strokeErrorColour;
    }

    /**
     * Set the outline box's stroke color when an error is being displayed.
     *
     * <p>Calling this method when not in outline box mode will do nothing.
     *
     * @param strokeErrorColor the error color to use for the box's stroke
     * @see #getBoxStrokeErrorColor()
     */
    public void setBoxStrokeErrorColor(@Nullable ColorStateList strokeErrorColor)
    {
        if (this.strokeErrorColour != strokeErrorColor)
        {
            this.strokeErrorColour = strokeErrorColor;
            updateTextInputBoxState();
        }
    }

    /**
     * Set the resource used for the filled box's background color.
     *
     * <p>Note: The background color is only supported for filled boxes. When used with box variants
     * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
     * work as intended.
     *
     * @param boxBackgroundColorId the resource to use for the box's background color
     */
    public void setBoxBackgroundColorResource(@ColorRes int boxBackgroundColorId)
    {
        setBoxBackgroundColour(ContextCompat.getColor(getContext(), boxBackgroundColorId));
    }

    /**
     * Sets the box's background color state list.
     *
     * <p>Note: The background color is only supported for filled boxes. When used with box variants
     * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
     * work as intended.
     *
     * @param boxBackgroundColorStateList the color state list to use for the box's background color
     */
    public void setBoxBackgroundColorStateList(@NonNull ColorStateList boxBackgroundColorStateList)
    {
        filledBackgroundColour = boxBackgroundColorStateList.getDefaultColor();
        boxBackgroundColour = filledBackgroundColour;
        applyBoxAttributes();
    }

    /**
     * Returns the filled box's default background color.
     *
     * @return the color used for the filled box's background
     * @see #setBoxBackgroundColour(int)
     */
    public int getBoxBackgroundColour()
    {
        return boxBackgroundColour;
    }

    /**
     * Sets the filled box's default background color. Calling this method will make the background
     * color not be stateful, if it was before.
     *
     * <p>Note: The background color is only supported for filled boxes. When used with box variants
     * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
     * work as intended.
     *
     * @param boxBackgroundColour the color to use for the filled box's background
     * @see #getBoxBackgroundColour()
     */
    public void setBoxBackgroundColour(@ColorInt int boxBackgroundColour)
    {
        if (this.boxBackgroundColour != boxBackgroundColour)
        {
            this.boxBackgroundColour = boxBackgroundColour;
            filledBackgroundColour = boxBackgroundColour;
            applyBoxAttributes();
        }
    }

    /**
     * Set the resources used for the box's corner radii.
     *
     * @param boxCornerRadiusTopStartId    the resource to use for the box's top start corner radius
     * @param boxCornerRadiusTopEndId      the resource to use for the box's top end corner radius
     * @param boxCornerRadiusBottomEndId   the resource to use for the box's bottom end corner radius
     * @param boxCornerRadiusBottomStartId the resource to use for the box's bottom start corner
     *                                     radius
     */
    public void setBoxCornerRadiiResources(
        @DimenRes int boxCornerRadiusTopStartId,
        @DimenRes int boxCornerRadiusTopEndId,
        @DimenRes int boxCornerRadiusBottomEndId,
        @DimenRes int boxCornerRadiusBottomStartId)
    {
        setBoxCornerRadii(
            getContext().getResources().getDimension(boxCornerRadiusTopStartId),
            getContext().getResources().getDimension(boxCornerRadiusTopEndId),
            getContext().getResources().getDimension(boxCornerRadiusBottomStartId),
            getContext().getResources().getDimension(boxCornerRadiusBottomEndId));
    }

    /**
     * Set the box's corner radii.
     *
     * @param boxCornerRadiusTopStart    the value to use for the box's top start corner radius
     * @param boxCornerRadiusTopEnd      the value to use for the box's top end corner radius
     * @param boxCornerRadiusBottomEnd   the value to use for the box's bottom end corner radius
     * @param boxCornerRadiusBottomStart the value to use for the box's bottom start corner radius
     * @see #getBoxCornerRadiusTopStart()
     * @see #getBoxCornerRadiusTopEnd()
     * @see #getBoxCornerRadiusBottomEnd()
     * @see #getBoxCornerRadiusBottomStart()
     */
    public void setBoxCornerRadii(
        float boxCornerRadiusTopStart,
        float boxCornerRadiusTopEnd,
        float boxCornerRadiusBottomStart,
        float boxCornerRadiusBottomEnd)
    {
        if (boxBackground == null
            || boxBackground.getTopLeftCornerResolvedSize() != boxCornerRadiusTopStart
            || boxBackground.getTopRightCornerResolvedSize() != boxCornerRadiusTopEnd
            || boxBackground.getBottomRightCornerResolvedSize() != boxCornerRadiusBottomEnd
            || boxBackground.getBottomLeftCornerResolvedSize() != boxCornerRadiusBottomStart)
        {
            shapeAppearanceModel =
                shapeAppearanceModel.toBuilder()
                    .setTopLeftCornerSize(boxCornerRadiusTopStart)
                    .setTopRightCornerSize(boxCornerRadiusTopEnd)
                    .setBottomRightCornerSize(boxCornerRadiusBottomEnd)
                    .setBottomLeftCornerSize(boxCornerRadiusBottomStart)
                    .build();
            applyBoxAttributes();
        }
    }

    /**
     * Returns the box's top start corner radius.
     *
     * @return the value used for the box's top start corner radius
     * @see #setBoxCornerRadii(float, float, float, float)
     */
    public float getBoxCornerRadiusTopStart()
    {
        return boxBackground.getTopLeftCornerResolvedSize();
    }

    /**
     * Returns the box's top end corner radius.
     *
     * @return the value used for the box's top end corner radius
     * @see #setBoxCornerRadii(float, float, float, float)
     */
    public float getBoxCornerRadiusTopEnd()
    {
        return boxBackground.getTopRightCornerResolvedSize();
    }

    /**
     * Returns the box's bottom end corner radius.
     *
     * @return the value used for the box's bottom end corner radius
     * @see #setBoxCornerRadii(float, float, float, float)
     */
    public float getBoxCornerRadiusBottomEnd()
    {
        return boxBackground.getBottomLeftCornerResolvedSize();
    }

    /**
     * Returns the box's bottom start corner radius.
     *
     * @return the value used for the box's bottom start corner radius
     * @see #setBoxCornerRadii(float, float, float, float)
     */
    public float getBoxCornerRadiusBottomStart()
    {
        return boxBackground.getBottomRightCornerResolvedSize();
    }

    /**
     * Returns the typeface used for the hint and any label views (such as counter and error views).
     */
    @Nullable
    public Typeface getTypeface()
    {
        return typeface;
    }

    /**
     * Set the typeface to use for the hint and any label views (such as counter and error views).
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    public void setTypeface(@Nullable Typeface typeface)
    {
        if (typeface != this.typeface)
        {
            this.typeface = typeface;

            collapsingTextHelper.setTypefaces(typeface);
            indicatorViewController.setTypefaces(typeface);
        }
    }

    private void updateInputLayoutMargins()
    {
        // Create/update the LayoutParams so that we can add enough top margin
        // to the EditText to make room for the label.
        if (boxBackgroundMode != BOX_BACKGROUND_FILLED)
        {
            final LayoutParams lp = (LayoutParams) inputFrame.getLayoutParams();
            final int newTopMargin = calculateLabelMarginTop();

            if (newTopMargin != lp.topMargin)
            {
                lp.topMargin = newTopMargin;
                inputFrame.requestLayout();
            }
        }
    }

    @Override
    public int getBaseline()
    {
        if (linearLayout != null)
        {
            return linearLayout.getBaseline() + getPaddingTop() + calculateLabelMarginTop();
        } else
        {
            return super.getBaseline();
        }
    }

    void updateLabelState()
    {
        collapsingTextHelper.setCollapsedTextColor(textColour);
        collapseHint();
    }

    /**
     * Returns the {@link android.widget.View} used for text input.
     */
    @Nullable
    public View getLinearLayout()
    {
        return linearLayout;
    }

    private void setLinearLayout(View linearLayout)
    {
        // If we already have an EditText, throw an exception
        if (this.linearLayout != null)
        {
            throw new IllegalArgumentException("We already have an View, can only have one");
        }

        if (!(linearLayout instanceof View))
        {
            Log.i(
                LOG_TAG,
                "EditText added is not a TextInputEditText. Please switch to using that"
                    + " class instead.");
        }

        this.linearLayout = linearLayout;
        onApplyBoxBackgroundMode();

        // Use the EditText's typeface, and its text size for our expanded text.

        collapsingTextHelper.setCollapsedTextGravity(
            Gravity.TOP | (Gravity.START & ~Gravity.VERTICAL_GRAVITY_MASK));
        collapsingTextHelper.setCollapsedTextSize(30);

        updateEditTextBackground();

        indicatorViewController.adjustIndicatorPadding();

        // Only call setEnabled on the edit text if the layout is disabled, to prevent reenabling an
        // already disabled edit text.
        if (!isEnabled())
        {
            linearLayout.setEnabled(false);
        }

        // Update the label visibility with no animation, but force a state change
        updateLabelState();
    }

    public void setText(String text)
    {
        if (this.collapsingTextHelper != null)
        {
            collapsingTextHelper.setText(text);
        }
    }

    private void setHintInternal(CharSequence hint)
    {
        if (!TextUtils.equals(hint, this.title))
        {
            this.title = hint;
            collapsingTextHelper.setText(hint);
            // Reset the cutout to make room for a larger hint.
            if (!hintExpanded)
            {
                openCutout();
            }
        }
    }

    /**
     * Returns the hint which is displayed in the floating label, if enabled.
     *
     * @return the hint, or null if there isn't one set, or the hint is not enabled.
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_android_hint
     */
    @Nullable
    public CharSequence getTitle()
    {
        return titleEnabled ? title : null;
    }

    /**
     * Set the hint to be displayed in the floating label, if enabled.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_android_hint
     * @see #setTitleEnabled(boolean)
     */
    public void setTitle(@Nullable CharSequence title)
    {
        if (titleEnabled)
        {
            setHintInternal(title);
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    /**
     * Returns whether the floating label functionality is enabled or not in this layout.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_hintEnabled
     * @see #setTitleEnabled(boolean)
     */
    public boolean isTitleEnabled()
    {
        return titleEnabled;
    }

    /**
     * Sets whether the floating label functionality is enabled or not in this layout.
     *
     * <p>If enabled, any non-empty hint in the child EditText will be moved into the floating hint,
     * and its existing hint will be cleared. If disabled, then any non-empty floating hint in this
     * layout will be moved into the EditText, and this layout's hint will be cleared.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_hintEnabled
     * @see #setTitle(CharSequence)
     * @see #isTitleEnabled()
     */
    public void setTitleEnabled(boolean enabled)
    {
        if (enabled != titleEnabled)
        {
            titleEnabled = enabled;
            // Now update the EditText top margin
            if (linearLayout != null)
            {
                updateInputLayoutMargins();
            }
        }
    }

    /**
     * Sets the collapsed hint text color, size, style from the specified TextAppearance resource.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_hintTextAppearance
     */
    public void setHintTextAppearance(@StyleRes int resId)
    {
        collapsingTextHelper.setCollapsedTextAppearance(resId);
        textColour = collapsingTextHelper.getCollapsedTextColor();

        if (linearLayout != null)
        {
            updateLabelState();
            // Text size might have changed so update the top margin
            updateInputLayoutMargins();
        }
    }

    /**
     * Sets the collapsed hint text color from the specified ColorStateList resource.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_hintTextColor
     */
    public void setHintTextColor(@Nullable ColorStateList hintTextColor)
    {
        if (textColour != hintTextColor)
        {
            if (hintTextColour == null)
            {
                collapsingTextHelper.setCollapsedTextColor(hintTextColor);
            }

            textColour = hintTextColor;

            if (linearLayout != null)
            {
                updateLabelState();
            }
        }
    }

    /**
     * Sets the text color and size for the error message from the specified TextAppearance resource.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_errorTextAppearance
     */
    public void setErrorTextAppearance(@StyleRes int errorTextAppearance)
    {
        indicatorViewController.setErrorTextAppearance(errorTextAppearance);
    }

    /**
     * Sets the text color used by the error message in all states.
     */
    public void setErrorTextColor(@Nullable ColorStateList errorTextColor)
    {
        indicatorViewController.setErrorViewTextColor(errorTextColor);
    }

    /**
     * Returns the text color used by the error message in current state.
     */
    @ColorInt
    public int getErrorCurrentTextColors()
    {
        return indicatorViewController.getErrorViewCurrentTextColor();
    }

    /**
     * Returns whether the error functionality is enabled or not in this layout.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_errorEnabled
     * @see #setErrorEnabled(boolean)
     */
    public boolean isErrorEnabled()
    {
        return indicatorViewController.isErrorEnabled();
    }

    /**
     * Whether the error functionality is enabled or not in this layout. Enabling this functionality
     * before setting an error message via {@link #setError(CharSequence)}, will mean that this layout
     * will not change size when an error is displayed.
     *
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_errorEnabled
     */
    public void setErrorEnabled(boolean enabled)
    {
        indicatorViewController.setErrorEnabled(enabled);
    }

    /**
     * Returns the content description of the error message, or null if not set.
     *
     * @see #setErrorContentDescription(CharSequence)
     */
    @Nullable
    public CharSequence getErrorContentDescription()
    {
        return indicatorViewController.getErrorContentDescription();
    }

    /**
     * Sets a content description for the error message.
     *
     * <p>A content description should be set when the error message contains special characters that
     * screen readers or other accessibility systems are not able to read, so that they announce the
     * content description instead.
     *
     * @param errorContentDecription Content description to set, or null to clear it
     * @attr ref com.zeoflow.material.elements.R.styleable#TextInputLayout_errorContentDescription
     */
    public void setErrorContentDescription(@Nullable final CharSequence errorContentDecription)
    {
        indicatorViewController.setErrorContentDescription(errorContentDecription);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        // Since we're set to addStatesFromChildren, we need to make sure that we set all
        // children to enabled/disabled otherwise any enabled children will wipe out our disabled
        // drawable state
        recursiveSetEnabled(this, enabled);
        super.setEnabled(enabled);
    }

    void setTextAppearanceCompatWithErrorFallback(
        @NonNull TextView textView, @StyleRes int textAppearance)
    {
        boolean useDefaultColor = false;
        try
        {
            TextViewCompat.setTextAppearance(textView, textAppearance);

            if (VERSION.SDK_INT >= VERSION_CODES.M
                && textView.getTextColors().getDefaultColor() == Color.MAGENTA)
            {
                // Caused by our theme not extending from Theme.Design*. On API 23 and
                // above, unresolved theme attrs result in MAGENTA rather than an exception.
                // Flag so that we use a decent default
                useDefaultColor = true;
            }
        } catch (Exception e)
        {
            // Caused by our theme not extending from Theme.Design*. Flag so that we use
            // a decent default
            useDefaultColor = true;
        }
        if (useDefaultColor)
        {
            // Probably caused by our theme not extending from Theme.Design*. Instead
            // we manually set something appropriate
            TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_AppCompat_Caption);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.design_error));
        }
    }

    private int calculateLabelMarginTop()
    {
        if (!titleEnabled)
        {
            return 0;
        }

        switch (boxBackgroundMode)
        {
            case BOX_BACKGROUND_OUTLINE:
                return (int) (collapsingTextHelper.getCollapsedTextHeight() / 2 * 1.1);
            case BOX_BACKGROUND_FILLED:
            case BOX_BACKGROUND_NONE:
                return (int) collapsingTextHelper.getCollapsedTextHeight();
            default:
                return 0;
        }
    }

    @NonNull
    private Rect calculateCollapsedTextBounds(@NonNull Rect rect)
    {
        if (linearLayout == null)
        {
            throw new IllegalStateException();
        }
        Rect bounds = tmpBoundsRect;

        bounds.bottom = rect.bottom;
        switch (boxBackgroundMode)
        {
            case BOX_BACKGROUND_OUTLINE:
                bounds.left = rect.left;
                bounds.top = rect.top - calculateLabelMarginTop();
                bounds.right = rect.right - linearLayout.getPaddingRight();
                if (bounds.left == 0) bounds.left = 24;
                return bounds;
            case BOX_BACKGROUND_FILLED:
                bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left);
                bounds.top = rect.top + boxCollapsedPaddingTopPx;
                bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right);
                if (bounds.left == 0) bounds.left = 24;
                return bounds;
            case BOX_BACKGROUND_NONE:
            default:
                bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left);
                bounds.top = getPaddingTop();
                bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right);
                if (bounds.left == 0) bounds.left = 24;
                return bounds;
        }
    }

    private int getLabelLeftBoundAlightWithPrefix(int rectLeft)
    {
        return rectLeft;// + linearLayout.getPaddingLeft();
    }

    private int getLabelRightBoundAlignedWithSuffix(int rectRight)
    {
        return rectRight - linearLayout.getPaddingRight();
    }

    @NonNull
    private Rect calculateExpandedTextBounds(@NonNull Rect rect)
    {
        if (linearLayout == null)
        {
            throw new IllegalStateException();
        }

        Rect bounds = tmpBoundsRect;

        float labelHeight = collapsingTextHelper.getExpandedTextHeight();

        bounds.left = rect.left + linearLayout.getPaddingLeft();
        bounds.top = calculateExpandedLabelTop(rect, labelHeight);
        bounds.right = rect.right - linearLayout.getPaddingRight();
        bounds.bottom = calculateExpandedLabelBottom(rect, bounds, labelHeight);

        return bounds;
    }

    private int calculateExpandedLabelTop(@NonNull Rect rect, float labelHeight)
    {
        return rect.top + linearLayout.getPaddingTop();
    }

    private int calculateExpandedLabelBottom(
        @NonNull Rect rect, @NonNull Rect bounds, float labelHeight)
    {
        return rect.bottom - linearLayout.getPaddingBottom();
    }

    /*
     * Calculates the box background color that should be set.
     *
     * The filled text field has a surface layer with value {@code ?attr/colorSurface} underneath its
     * background that is taken into account when calculating the background color.
     */
    private int calculateBoxBackgroundColor()
    {
        int backgroundColor = boxBackgroundColour;
        if (boxBackgroundMode == BOX_BACKGROUND_FILLED)
        {
            int surfaceLayerColor = MaterialColors.getColor(this, R.attr.colorSurface, Color.TRANSPARENT);
            backgroundColor = MaterialColors.layer(surfaceLayerColor, boxBackgroundColour);
        }
        return backgroundColor;
    }

    private void applyBoxAttributes()
    {
        if (boxBackground == null)
        {
            return;
        }

        boxBackground.setShapeAppearanceModel(shapeAppearanceModel);

        if (canDrawOutlineStroke())
        {
            boxBackground.setStroke(boxStrokeWidthPx, boxStrokeColour);
        }

        boxBackgroundColour = calculateBoxBackgroundColor();
        boxBackground.setFillColor(ColorStateList.valueOf(boxBackgroundColour));
        applyBoxUnderlineAttributes();
        invalidate();
    }

    private void applyBoxUnderlineAttributes()
    {
        // Exit if the underline is not being drawn by TextInputLayout.
        if (boxUnderline == null)
        {
            return;
        }

        if (canDrawStroke())
        {
            boxUnderline.setFillColor(ColorStateList.valueOf(boxStrokeColour));
        }
        invalidate();
    }

    private boolean canDrawOutlineStroke()
    {
        return boxBackgroundMode == BOX_BACKGROUND_OUTLINE && canDrawStroke();
    }

    private boolean canDrawStroke()
    {
        return boxStrokeWidthPx > -1 && boxStrokeColour != Color.TRANSPARENT;
    }

    void updateEditTextBackground()
    {
        // Only update the color filter for the legacy text field, since we can directly change the
        // Paint colors of the MaterialShapeDrawable box background without having to use color filters.
        if (linearLayout == null || boxBackgroundMode != BOX_BACKGROUND_NONE)
        {
            return;
        }

        Drawable editTextBackground = linearLayout.getBackground();
        if (editTextBackground == null)
        {
            return;
        }

        if (androidx.appcompat.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground))
        {
            editTextBackground = editTextBackground.mutate();
        }

        if (indicatorViewController.errorShouldBeShown())
        {
            // Set a color filter for the error color
            editTextBackground.setColorFilter(
                AppCompatDrawableManager.getPorterDuffColorFilter(
                    indicatorViewController.getErrorViewCurrentTextColor(), PorterDuff.Mode.SRC_IN));
        } else
        {
            // Else reset the color filter and refresh the drawable state so that the
            // normal tint is used
            DrawableCompat.clearColorFilter(editTextBackground);
            linearLayout.refreshDrawableState();
        }
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        if (indicatorViewController.errorShouldBeShown())
        {
            ss.error = getError();
        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state)
    {
        if (!(state instanceof SavedState))
        {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setError(ss.error);
        requestLayout();
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container)
    {
        super.dispatchRestoreInstanceState(container);
    }

    /**
     * Returns the error message that was set to be displayed with {@link #setError(CharSequence)}, or
     * <code>null</code> if no error was set or if error displaying is not enabled.
     *
     * @see #setError(CharSequence)
     */
    @Nullable
    public CharSequence getError()
    {
        return indicatorViewController.isErrorEnabled() ? indicatorViewController.getErrorText() : null;
    }

    /**
     * Sets an error message that will be displayed below our {@link EditText}. If the {@code error}
     * is {@code null}, the error message will be cleared.
     *
     * <p>If the error functionality has not been enabled via {@link #setErrorEnabled(boolean)}, then
     * it will be automatically enabled if {@code error} is not empty.
     *
     * @param errorText Error message to display, or null to clear
     * @see #getError()
     */
    public void setError(@Nullable final CharSequence errorText)
    {
        if (!indicatorViewController.isErrorEnabled())
        {
            if (TextUtils.isEmpty(errorText))
            {
                // If error isn't enabled, and the error is empty, just return
                return;
            }
            // Else, we'll assume that they want to enable the error functionality
            setErrorEnabled(true);
        }

        if (!TextUtils.isEmpty(errorText))
        {
            indicatorViewController.showError(errorText);
        } else
        {
            indicatorViewController.hideError();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        if (linearLayout != null)
        {
            Rect rect = tmpRect;
            DescendantOffsetUtils.getDescendantRect(this, linearLayout, rect);
            updateBoxUnderlineBounds(rect);

            if (titleEnabled)
            {
                collapsingTextHelper.setCollapsedTextGravity(
                    Gravity.TOP | (Gravity.START & ~Gravity.VERTICAL_GRAVITY_MASK));
                collapsingTextHelper.setCollapsedBounds(calculateCollapsedTextBounds(rect));
                collapsingTextHelper.recalculate();

                // If the label should be collapsed, set the cutout bounds on the CutoutDrawable to make
                // sure it draws with a cutout in draw().
                if (cutoutEnabled() && !hintExpanded)
                {
                    openCutout();
                }
            }
        }
    }

    private void updateBoxUnderlineBounds(@NonNull Rect bounds)
    {
        if (boxUnderline != null)
        {
            int top = bounds.bottom - boxStrokeWidthFocusedPx;
            boxUnderline.setBounds(bounds.left, top, bounds.right, bounds.bottom);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas)
    {
        super.draw(canvas);
        drawHint(canvas);
        drawBoxUnderline(canvas);
    }

    private void drawHint(@NonNull Canvas canvas)
    {
        if (titleEnabled)
        {
            collapsingTextHelper.draw(canvas);
        }
    }

    private void drawBoxUnderline(Canvas canvas)
    {
        if (boxUnderline != null)
        {
            // Draw using the current boxStrokeWidth.
            Rect underlineBounds = boxUnderline.getBounds();
            underlineBounds.top = underlineBounds.bottom - boxStrokeWidthPx;
            boxUnderline.draw(canvas);
        }
    }

    private void collapseHint()
    {
        collapsingTextHelper.setExpansionFraction(1f);
        hintExpanded = false;
        if (cutoutEnabled())
        {
            openCutout();
        }

    }

    private boolean cutoutEnabled()
    {
        return titleEnabled && !TextUtils.isEmpty(title) && boxBackground instanceof CutoutDrawable;
    }

    private void openCutout()
    {
        if (!cutoutEnabled())
        {
            return;
        }
        final RectF cutoutBounds = tmpRectF;
        collapsingTextHelper.getCollapsedTextActualBounds(
            cutoutBounds, linearLayout.getWidth(), GravityCompat.START);
        applyCutoutPadding(cutoutBounds);
        // Offset the cutout bounds by the TextInputLayout's left and top paddings to ensure that the
        // cutout is inset relative to the TextInputLayout's bounds.
        cutoutBounds.offset(-getPaddingLeft(), -getPaddingTop());
        ((CutoutDrawable) boxBackground).setCutout(cutoutBounds);
    }

    private void closeCutout()
    {
        if (cutoutEnabled())
        {
            ((CutoutDrawable) boxBackground).removeCutout();
        }
    }

    private void applyCutoutPadding(@NonNull RectF cutoutBounds)
    {
        cutoutBounds.left -= boxLabelCutoutPaddingPx;
        cutoutBounds.top -= boxLabelCutoutPaddingPx;
        cutoutBounds.right += boxLabelCutoutPaddingPx;
        cutoutBounds.bottom += boxLabelCutoutPaddingPx;
    }

    @VisibleForTesting
    boolean cutoutIsOpen()
    {
        return cutoutEnabled() && ((CutoutDrawable) boxBackground).hasCutout();
    }

    @Override
    protected void drawableStateChanged()
    {
        if (inDrawableStateChanged)
        {
            // Some of the calls below will update the drawable state of child views. Since we're
            // using addStatesFromChildren we can get into infinite recursion, hence we'll just
            // exit in this instance
            return;
        }

        inDrawableStateChanged = true;

        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        if (collapsingTextHelper != null)
        {
            changed |= collapsingTextHelper.setState(state);
        }

        // Drawable state has changed so see if we need to update the label
        if (linearLayout != null)
        {
            updateLabelState();
        }
        updateEditTextBackground();
        updateTextInputBoxState();

        if (changed)
        {
            invalidate();
        }

        inDrawableStateChanged = false;
    }

    void updateTextInputBoxState()
    {
        if (boxBackground == null || boxBackgroundMode == BOX_BACKGROUND_NONE)
        {
            return;
        }

        // Update the text box's stroke color based on the current state.
        boxStrokeColour = strokeColour;

        // Update the text box's stroke width based on the current state.
        boxStrokeWidthPx = boxStrokeWidthFocusedPx;

        // Update the text box's background color based on the current state.
        if (boxBackgroundMode == BOX_BACKGROUND_FILLED)
        {
            boxBackgroundColour = filledBackgroundColour;
        }

        applyBoxAttributes();
    }

    private void updateStrokeErrorColor()
    {
        int defaultStrokeErrorColor = strokeErrorColour.getDefaultColor();
        boxStrokeColour = strokeErrorColour.getColorForState(
            new int[]{android.R.attr.state_activated, android.R.attr.state_enabled},
            defaultStrokeErrorColor);
    }

    /**
     * Values for box background mode. There is either a filled background, an outline background, or
     * no background.
     */
    @IntDef({BOX_BACKGROUND_NONE, BOX_BACKGROUND_FILLED, BOX_BACKGROUND_OUTLINE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BoxBackgroundMode
    {
    }

    static class SavedState extends AbsSavedState
    {
        public static final Creator<SavedState> CREATOR =
            new ClassLoaderCreator<SavedState>()
            {
                @NonNull
                @Override
                public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader)
                {
                    return new SavedState(in, loader);
                }

                @Nullable
                @Override
                public SavedState createFromParcel(@NonNull Parcel in)
                {
                    return new SavedState(in, null);
                }

                @NonNull
                @Override
                public SavedState[] newArray(int size)
                {
                    return new SavedState[size];
                }
            };
        @Nullable
        CharSequence error;

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        SavedState(@NonNull Parcel source, ClassLoader loader)
        {
            super(source, loader);
            error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            TextUtils.writeToParcel(error, dest, flags);
        }

        @NonNull
        @Override
        public String toString()
        {
            return "TextInputLayout.SavedState{"
                + Integer.toHexString(System.identityHashCode(this))
                + " error="
                + error
                + "}";
        }
    }
}
