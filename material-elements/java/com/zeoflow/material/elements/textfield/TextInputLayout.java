

package com.zeoflow.material.elements.textfield;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.zeoflow.material.elements.textfield.IndicatorViewController.COUNTER_INDEX;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.BidiFormatter;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.TintTypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.CheckableImageButton;
import com.zeoflow.material.elements.internal.CollapsingTextHelper;
import com.zeoflow.material.elements.internal.DescendantOffsetUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;


public class TextInputLayout extends LinearLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_TextInputLayout;

  
  private static final int LABEL_SCALE_ANIMATION_DURATION = 167;

  private static final int INVALID_MAX_LENGTH = -1;

  private static final String LOG_TAG = "TextInputLayout";

  @NonNull private final FrameLayout inputFrame;
  @NonNull private final LinearLayout startLayout;
  @NonNull private final LinearLayout endLayout;
  @NonNull private final FrameLayout endIconFrame;
  EditText editText;
  private CharSequence originalHint;

  private final IndicatorViewController indicatorViewController = new IndicatorViewController(this);

  boolean counterEnabled;
  private int counterMaxLength;
  private boolean counterOverflowed;
  @Nullable private TextView counterView;
  private int counterOverflowTextAppearance;
  private int counterTextAppearance;

  private CharSequence placeholderText;
  private boolean placeholderEnabled;
  private TextView placeholderTextView;
  @Nullable private ColorStateList placeholderTextColor;
  private int placeholderTextAppearance;

  @Nullable private ColorStateList counterTextColor;
  @Nullable private ColorStateList counterOverflowTextColor;

  @Nullable private CharSequence prefixText;
  @NonNull private final TextView prefixTextView;
  @Nullable private CharSequence suffixText;
  @NonNull private final TextView suffixTextView;

  private boolean hintEnabled;
  private CharSequence hint;

  
  private boolean isProvidingHint;

  @Nullable private MaterialShapeDrawable boxBackground;
  @Nullable private MaterialShapeDrawable boxUnderline;
  @NonNull private ShapeAppearanceModel shapeAppearanceModel;

  private final int boxLabelCutoutPaddingPx;
  @BoxBackgroundMode private int boxBackgroundMode;
  private final int boxCollapsedPaddingTopPx;
  private int boxStrokeWidthPx;
  private int boxStrokeWidthDefaultPx;
  private int boxStrokeWidthFocusedPx;
  @ColorInt private int boxStrokeColor;
  @ColorInt private int boxBackgroundColor;

  
  @IntDef({BOX_BACKGROUND_NONE, BOX_BACKGROUND_FILLED, BOX_BACKGROUND_OUTLINE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface BoxBackgroundMode {}

  public static final int BOX_BACKGROUND_NONE = 0;
  public static final int BOX_BACKGROUND_FILLED = 1;
  public static final int BOX_BACKGROUND_OUTLINE = 2;

  private final Rect tmpRect = new Rect();
  private final Rect tmpBoundsRect = new Rect();
  private final RectF tmpRectF = new RectF();
  private Typeface typeface;

  @NonNull private final CheckableImageButton startIconView;
  private ColorStateList startIconTintList;
  private boolean hasStartIconTintList;
  private PorterDuff.Mode startIconTintMode;
  private boolean hasStartIconTintMode;
  @Nullable private Drawable startDummyDrawable;
  private int startDummyDrawableWidth;
  private OnLongClickListener startIconOnLongClickListener;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    END_ICON_CUSTOM,
    END_ICON_NONE,
    END_ICON_PASSWORD_TOGGLE,
    END_ICON_CLEAR_TEXT,
    END_ICON_DROPDOWN_MENU
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface EndIconMode {}

  
  public static final int END_ICON_CUSTOM = -1;

  
  public static final int END_ICON_NONE = 0;

  
  public static final int END_ICON_PASSWORD_TOGGLE = 1;

  
  public static final int END_ICON_CLEAR_TEXT = 2;

  
  public static final int END_ICON_DROPDOWN_MENU = 3;

  
  public interface OnEditTextAttachedListener {

    
    void onEditTextAttached(@NonNull TextInputLayout textInputLayout);
  }

  
  public interface OnEndIconChangedListener {

    
    void onEndIconChanged(@NonNull TextInputLayout textInputLayout, @EndIconMode int previousIcon);
  }

  private final LinkedHashSet<OnEditTextAttachedListener> editTextAttachedListeners =
      new LinkedHashSet<>();

  @EndIconMode private int endIconMode = END_ICON_NONE;
  private final SparseArray<EndIconDelegate> endIconDelegates = new SparseArray<>();
  @NonNull private final CheckableImageButton endIconView;
  private final LinkedHashSet<OnEndIconChangedListener> endIconChangedListeners =
      new LinkedHashSet<>();
  private ColorStateList endIconTintList;
  private boolean hasEndIconTintList;
  private PorterDuff.Mode endIconTintMode;
  private boolean hasEndIconTintMode;
  @Nullable private Drawable endDummyDrawable;
  private int endDummyDrawableWidth;
  private Drawable originalEditTextEndDrawable;
  private OnLongClickListener endIconOnLongClickListener;
  private OnLongClickListener errorIconOnLongClickListener;
  @NonNull private final CheckableImageButton errorIconView;
  private ColorStateList errorIconTintList;

  private ColorStateList defaultHintTextColor;
  private ColorStateList focusedTextColor;

  @ColorInt private int defaultStrokeColor;
  @ColorInt private int hoveredStrokeColor;
  @ColorInt private int focusedStrokeColor;
  private ColorStateList strokeErrorColor;

  @ColorInt private int defaultFilledBackgroundColor;
  @ColorInt private int disabledFilledBackgroundColor;
  @ColorInt private int focusedFilledBackgroundColor;
  @ColorInt private int hoveredFilledBackgroundColor;

  @ColorInt private int disabledColor;

  
  private boolean hintExpanded;

  final CollapsingTextHelper collapsingTextHelper = new CollapsingTextHelper(this);

  private boolean hintAnimationEnabled;
  private ValueAnimator animator;

  private boolean inDrawableStateChanged;

  private boolean restoringSavedState;

  public TextInputLayout(@NonNull Context context) {
    this(context, null);
  }

  public TextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.textInputStyle);
  }

  public TextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    setOrientation(VERTICAL);
    setWillNotDraw(false);
    setAddStatesFromChildren(true);

    inputFrame = new FrameLayout(context);
    inputFrame.setAddStatesFromChildren(true);
    addView(inputFrame);
    startLayout = new LinearLayout(context);
    startLayout.setOrientation(HORIZONTAL);
    startLayout.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.START | Gravity.LEFT));
    inputFrame.addView(startLayout);
    endLayout = new LinearLayout(context);
    endLayout.setOrientation(HORIZONTAL);
    endLayout.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.END | Gravity.RIGHT));
    inputFrame.addView(endLayout);
    endIconFrame = new FrameLayout(context);
    endIconFrame.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
            R.styleable.TextInputLayout_counterTextAppearance,
            R.styleable.TextInputLayout_counterOverflowTextAppearance,
            R.styleable.TextInputLayout_errorTextAppearance,
            R.styleable.TextInputLayout_helperTextTextAppearance,
            R.styleable.TextInputLayout_hintTextAppearance);

    hintEnabled = a.getBoolean(R.styleable.TextInputLayout_hintEnabled, true);
    setHint(a.getText(R.styleable.TextInputLayout_android_hint));
    hintAnimationEnabled = a.getBoolean(R.styleable.TextInputLayout_hintAnimationEnabled, true);

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
    if (boxCornerRadiusTopStart >= 0) {
      shapeBuilder.setTopLeftCornerSize(boxCornerRadiusTopStart);
    }
    if (boxCornerRadiusTopEnd >= 0) {
      shapeBuilder.setTopRightCornerSize(boxCornerRadiusTopEnd);
    }
    if (boxCornerRadiusBottomEnd >= 0) {
      shapeBuilder.setBottomRightCornerSize(boxCornerRadiusBottomEnd);
    }
    if (boxCornerRadiusBottomStart >= 0) {
      shapeBuilder.setBottomLeftCornerSize(boxCornerRadiusBottomStart);
    }
    shapeAppearanceModel = shapeBuilder.build();

    ColorStateList filledBackgroundColorStateList =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextInputLayout_boxBackgroundColor);
    if (filledBackgroundColorStateList != null) {
      defaultFilledBackgroundColor = filledBackgroundColorStateList.getDefaultColor();
      boxBackgroundColor = defaultFilledBackgroundColor;
      if (filledBackgroundColorStateList.isStateful()) {
        disabledFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {-android.R.attr.state_enabled}, -1);
        focusedFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
        hoveredFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
      } else {
        focusedFilledBackgroundColor = defaultFilledBackgroundColor;
        ColorStateList mtrlFilledBackgroundColorStateList =
            AppCompatResources.getColorStateList(context, R.color.mtrl_filled_background_color);
        disabledFilledBackgroundColor =
            mtrlFilledBackgroundColorStateList.getColorForState(
                new int[] {-android.R.attr.state_enabled}, -1);
        hoveredFilledBackgroundColor =
            mtrlFilledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_hovered}, -1);
      }
    } else {
      boxBackgroundColor = Color.TRANSPARENT;
      defaultFilledBackgroundColor = Color.TRANSPARENT;
      disabledFilledBackgroundColor = Color.TRANSPARENT;
      focusedFilledBackgroundColor = Color.TRANSPARENT;
      hoveredFilledBackgroundColor = Color.TRANSPARENT;
    }

    if (a.hasValue(R.styleable.TextInputLayout_android_textColorHint)) {
      defaultHintTextColor =
          focusedTextColor = a.getColorStateList(R.styleable.TextInputLayout_android_textColorHint);
    }

    ColorStateList boxStrokeColorStateList =
        MaterialResources.getColorStateList(context, a, R.styleable.TextInputLayout_boxStrokeColor);
    
    focusedStrokeColor = a.getColor(R.styleable.TextInputLayout_boxStrokeColor, Color.TRANSPARENT);
    defaultStrokeColor =
        ContextCompat.getColor(context, R.color.mtrl_textinput_default_box_stroke_color);
    disabledColor = ContextCompat.getColor(context, R.color.mtrl_textinput_disabled_color);
    hoveredStrokeColor =
        ContextCompat.getColor(context, R.color.mtrl_textinput_hovered_box_stroke_color);
    
    if (boxStrokeColorStateList != null) {
      setBoxStrokeColorStateList(boxStrokeColorStateList);
    }
    if (a.hasValue(R.styleable.TextInputLayout_boxStrokeErrorColor)) {
      setBoxStrokeErrorColor(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_boxStrokeErrorColor));
    }

    final int hintAppearance = a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, -1);
    if (hintAppearance != -1) {
      setHintTextAppearance(a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, 0));
    }

    final int errorTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
    final CharSequence errorContentDescription =
        a.getText(R.styleable.TextInputLayout_errorContentDescription);
    final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);
    
    errorIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_end_icon, endLayout, false);
    errorIconView.setVisibility(GONE);
    if (a.hasValue(R.styleable.TextInputLayout_errorIconDrawable)) {
      setErrorIconDrawable(a.getDrawable(R.styleable.TextInputLayout_errorIconDrawable));
    }
    if (a.hasValue(R.styleable.TextInputLayout_errorIconTint)) {
      setErrorIconTintList(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_errorIconTint));
    }
    if (a.hasValue(R.styleable.TextInputLayout_errorIconTintMode)) {
      setErrorIconTintMode(
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_errorIconTintMode, -1), null));
    }
    errorIconView.setContentDescription(
        getResources().getText(R.string.error_icon_content_description));
    ViewCompat.setImportantForAccessibility(
        errorIconView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    errorIconView.setClickable(false);
    errorIconView.setPressable(false);
    errorIconView.setFocusable(false);

    final int helperTextTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_helperTextTextAppearance, 0);
    final boolean helperTextEnabled =
        a.getBoolean(R.styleable.TextInputLayout_helperTextEnabled, false);
    final CharSequence helperText = a.getText(R.styleable.TextInputLayout_helperText);

    final int placeholderTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_placeholderTextAppearance, 0);
    final CharSequence placeholderText = a.getText(R.styleable.TextInputLayout_placeholderText);

    final int prefixTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_prefixTextAppearance, 0);
    final CharSequence prefixText = a.getText(R.styleable.TextInputLayout_prefixText);

    final int suffixTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_suffixTextAppearance, 0);
    final CharSequence suffixText = a.getText(R.styleable.TextInputLayout_suffixText);

    final boolean counterEnabled = a.getBoolean(R.styleable.TextInputLayout_counterEnabled, false);
    setCounterMaxLength(a.getInt(R.styleable.TextInputLayout_counterMaxLength, INVALID_MAX_LENGTH));
    counterTextAppearance = a.getResourceId(R.styleable.TextInputLayout_counterTextAppearance, 0);
    counterOverflowTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_counterOverflowTextAppearance, 0);

    
    startIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_start_icon, startLayout, false);
    startIconView.setVisibility(GONE);
    setStartIconOnClickListener(null);
    setStartIconOnLongClickListener(null);
    
    if (a.hasValue(R.styleable.TextInputLayout_startIconDrawable)) {
      setStartIconDrawable(a.getDrawable(R.styleable.TextInputLayout_startIconDrawable));
      if (a.hasValue(R.styleable.TextInputLayout_startIconContentDescription)) {
        setStartIconContentDescription(
            a.getText(R.styleable.TextInputLayout_startIconContentDescription));
      }
      setStartIconCheckable(a.getBoolean(R.styleable.TextInputLayout_startIconCheckable, true));
    }
    
    if (a.hasValue(R.styleable.TextInputLayout_startIconTint)) {
      setStartIconTintList(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_startIconTint));
    }
    
    if (a.hasValue(R.styleable.TextInputLayout_startIconTintMode)) {
      setStartIconTintMode(
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_startIconTintMode, -1), null));
    }

    setBoxBackgroundMode(
        a.getInt(R.styleable.TextInputLayout_boxBackgroundMode, BOX_BACKGROUND_NONE));

    
    endIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_end_icon, endIconFrame, false);
    endIconFrame.addView(endIconView);
    endIconView.setVisibility(GONE);
    endIconDelegates.append(END_ICON_CUSTOM, new CustomEndIconDelegate(this));
    endIconDelegates.append(END_ICON_NONE, new NoEndIconDelegate(this));
    endIconDelegates.append(END_ICON_PASSWORD_TOGGLE, new PasswordToggleEndIconDelegate(this));
    endIconDelegates.append(END_ICON_CLEAR_TEXT, new ClearTextEndIconDelegate(this));
    endIconDelegates.append(END_ICON_DROPDOWN_MENU, new DropdownMenuEndIconDelegate(this));
    
    if (a.hasValue(R.styleable.TextInputLayout_endIconMode)) {
      
      setEndIconMode(a.getInt(R.styleable.TextInputLayout_endIconMode, END_ICON_NONE));
      
      if (a.hasValue(R.styleable.TextInputLayout_endIconDrawable)) {
        setEndIconDrawable(a.getDrawable(R.styleable.TextInputLayout_endIconDrawable));
      }
      if (a.hasValue(R.styleable.TextInputLayout_endIconContentDescription)) {
        setEndIconContentDescription(
            a.getText(R.styleable.TextInputLayout_endIconContentDescription));
      }
      setEndIconCheckable(a.getBoolean(R.styleable.TextInputLayout_endIconCheckable, true));
    } else if (a.hasValue(R.styleable.TextInputLayout_passwordToggleEnabled)) {
      
      boolean passwordToggleEnabled =
          a.getBoolean(R.styleable.TextInputLayout_passwordToggleEnabled, false);
      setEndIconMode(passwordToggleEnabled ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
      setEndIconDrawable(a.getDrawable(R.styleable.TextInputLayout_passwordToggleDrawable));
      setEndIconContentDescription(
          a.getText(R.styleable.TextInputLayout_passwordToggleContentDescription));
      if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTint)) {
        setEndIconTintList(
            MaterialResources.getColorStateList(
                context, a, R.styleable.TextInputLayout_passwordToggleTint));
      }
      if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTintMode)) {
        setEndIconTintMode(
            ViewUtils.parseTintMode(
                a.getInt(R.styleable.TextInputLayout_passwordToggleTintMode, -1), null));
      }
    }

    if (!a.hasValue(R.styleable.TextInputLayout_passwordToggleEnabled)) {
      
      if (a.hasValue(R.styleable.TextInputLayout_endIconTint)) {
        setEndIconTintList(
            MaterialResources.getColorStateList(
                context, a, R.styleable.TextInputLayout_endIconTint));
      }
      
      if (a.hasValue(R.styleable.TextInputLayout_endIconTintMode)) {
        setEndIconTintMode(
            ViewUtils.parseTintMode(
                a.getInt(R.styleable.TextInputLayout_endIconTintMode, -1), null));
      }
    }

    
    prefixTextView = new AppCompatTextView(context);
    prefixTextView.setId(R.id.textinput_prefix_text);
    prefixTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    ViewCompat.setAccessibilityLiveRegion(
        prefixTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

    startLayout.addView(startIconView);
    startLayout.addView(prefixTextView);

    
    suffixTextView = new AppCompatTextView(context);
    suffixTextView.setId(R.id.textinput_suffix_text);
    suffixTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM));
    ViewCompat.setAccessibilityLiveRegion(
        suffixTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

    endLayout.addView(suffixTextView);
    endLayout.addView(errorIconView);
    endLayout.addView(endIconFrame);

    setHelperTextEnabled(helperTextEnabled);
    setHelperText(helperText);
    setHelperTextTextAppearance(helperTextTextAppearance);
    setErrorEnabled(errorEnabled);
    setErrorTextAppearance(errorTextAppearance);
    setErrorContentDescription(errorContentDescription);
    setCounterTextAppearance(counterTextAppearance);
    setCounterOverflowTextAppearance(counterOverflowTextAppearance);
    setPlaceholderText(placeholderText);
    setPlaceholderTextAppearance(placeholderTextAppearance);
    setPrefixText(prefixText);
    setPrefixTextAppearance(prefixTextAppearance);
    setSuffixText(suffixText);
    setSuffixTextAppearance(suffixTextAppearance);

    if (a.hasValue(R.styleable.TextInputLayout_errorTextColor)) {
      setErrorTextColor(a.getColorStateList(R.styleable.TextInputLayout_errorTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_helperTextTextColor)) {
      setHelperTextColor(a.getColorStateList(R.styleable.TextInputLayout_helperTextTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_hintTextColor)) {
      setHintTextColor(a.getColorStateList(R.styleable.TextInputLayout_hintTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_counterTextColor)) {
      setCounterTextColor(a.getColorStateList(R.styleable.TextInputLayout_counterTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_counterOverflowTextColor)) {
      setCounterOverflowTextColor(
          a.getColorStateList(R.styleable.TextInputLayout_counterOverflowTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_placeholderTextColor)) {
      setPlaceholderTextColor(
          a.getColorStateList(R.styleable.TextInputLayout_placeholderTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_prefixTextColor)) {
      setPrefixTextColor(a.getColorStateList(R.styleable.TextInputLayout_prefixTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_suffixTextColor)) {
      setSuffixTextColor(a.getColorStateList(R.styleable.TextInputLayout_suffixTextColor));
    }
    setCounterEnabled(counterEnabled);

    setEnabled(a.getBoolean(R.styleable.TextInputLayout_android_enabled, true));

    a.recycle();

    
    
    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  @Override
  public void addView(
      @NonNull View child, int index, @NonNull final ViewGroup.LayoutParams params) {
    if (child instanceof EditText) {
      
      
      FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
      flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
      inputFrame.addView(child, flp);

      
      
      inputFrame.setLayoutParams(params);
      updateInputLayoutMargins();

      setEditText((EditText) child);
    } else {
      
      super.addView(child, index, params);
    }
  }

  @NonNull
  MaterialShapeDrawable getBoxBackground() {
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED || boxBackgroundMode == BOX_BACKGROUND_OUTLINE) {
      return boxBackground;
    }
    throw new IllegalStateException();
  }

  
  public void setBoxBackgroundMode(@BoxBackgroundMode int boxBackgroundMode) {
    if (boxBackgroundMode == this.boxBackgroundMode) {
      return;
    }
    this.boxBackgroundMode = boxBackgroundMode;
    if (editText != null) {
      onApplyBoxBackgroundMode();
    }
  }

  
  @BoxBackgroundMode
  public int getBoxBackgroundMode() {
    return boxBackgroundMode;
  }

  private void onApplyBoxBackgroundMode() {
    assignBoxBackgroundByMode();
    setEditTextBoxBackground();
    updateTextInputBoxState();
    if (boxBackgroundMode != BOX_BACKGROUND_NONE) {
      updateInputLayoutMargins();
    }
  }

  private void assignBoxBackgroundByMode() {
    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_FILLED:
        boxBackground = new MaterialShapeDrawable(shapeAppearanceModel);
        boxUnderline = new MaterialShapeDrawable();
        break;
      case BOX_BACKGROUND_OUTLINE:
        if (hintEnabled && !(boxBackground instanceof CutoutDrawable)) {
          boxBackground = new CutoutDrawable(shapeAppearanceModel);
        } else {
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

  private void setEditTextBoxBackground() {
    
    if (shouldUseEditTextBackgroundForBoxBackground()) {
      ViewCompat.setBackground(editText, boxBackground);
    }
  }

  private boolean shouldUseEditTextBackgroundForBoxBackground() {
    
    
    return editText != null
        && boxBackground != null
        && editText.getBackground() == null
        && boxBackgroundMode != BOX_BACKGROUND_NONE;
  }

  
  public void setBoxStrokeWidthResource(@DimenRes int boxStrokeWidthResId) {
    setBoxStrokeWidth(getResources().getDimensionPixelSize(boxStrokeWidthResId));
  }

  
  public void setBoxStrokeWidth(int boxStrokeWidth) {
    boxStrokeWidthDefaultPx = boxStrokeWidth;
    updateTextInputBoxState();
  }

  
  public int getBoxStrokeWidth() {
    return boxStrokeWidthDefaultPx;
  }

  
  public void setBoxStrokeWidthFocusedResource(@DimenRes int boxStrokeWidthFocusedResId) {
    setBoxStrokeWidthFocused(getResources().getDimensionPixelSize(boxStrokeWidthFocusedResId));
  }

  
  public void setBoxStrokeWidthFocused(int boxStrokeWidthFocused) {
    boxStrokeWidthFocusedPx = boxStrokeWidthFocused;
    updateTextInputBoxState();
  }

  
  public int getBoxStrokeWidthFocused() {
    return boxStrokeWidthFocusedPx;
  }

  
  public void setBoxStrokeColor(@ColorInt int boxStrokeColor) {
    if (focusedStrokeColor != boxStrokeColor) {
      focusedStrokeColor = boxStrokeColor;
      updateTextInputBoxState();
    }
  }

  
  public int getBoxStrokeColor() {
    return focusedStrokeColor;
  }

  
  public void setBoxStrokeColorStateList(@NonNull ColorStateList boxStrokeColorStateList) {
    if (boxStrokeColorStateList.isStateful()) {
      defaultStrokeColor = boxStrokeColorStateList.getDefaultColor();
      disabledColor =
          boxStrokeColorStateList.getColorForState(new int[] {-android.R.attr.state_enabled}, -1);
      hoveredStrokeColor =
          boxStrokeColorStateList.getColorForState(
              new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
      focusedStrokeColor =
          boxStrokeColorStateList.getColorForState(
              new int[] {android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
    } else if (focusedStrokeColor != boxStrokeColorStateList.getDefaultColor()) {
      
      
      focusedStrokeColor = boxStrokeColorStateList.getDefaultColor();
    }
    updateTextInputBoxState();
  }

  
  public void setBoxStrokeErrorColor(@Nullable ColorStateList strokeErrorColor) {
    if (this.strokeErrorColor != strokeErrorColor) {
      this.strokeErrorColor = strokeErrorColor;
      updateTextInputBoxState();
    }
  }

  
  @Nullable
  public ColorStateList getBoxStrokeErrorColor() {
    return strokeErrorColor;
  }

  
  public void setBoxBackgroundColorResource(@ColorRes int boxBackgroundColorId) {
    setBoxBackgroundColor(ContextCompat.getColor(getContext(), boxBackgroundColorId));
  }

  
  public void setBoxBackgroundColor(@ColorInt int boxBackgroundColor) {
    if (this.boxBackgroundColor != boxBackgroundColor) {
      this.boxBackgroundColor = boxBackgroundColor;
      defaultFilledBackgroundColor = boxBackgroundColor;
      focusedFilledBackgroundColor = boxBackgroundColor;
      hoveredFilledBackgroundColor = boxBackgroundColor;
      applyBoxAttributes();
    }
  }

  
  public void setBoxBackgroundColorStateList(@NonNull ColorStateList boxBackgroundColorStateList) {
    defaultFilledBackgroundColor = boxBackgroundColorStateList.getDefaultColor();
    boxBackgroundColor = defaultFilledBackgroundColor;
    disabledFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(
            new int[]{-android.R.attr.state_enabled}, -1);
    focusedFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(
            new int[]{android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
    hoveredFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(
              new int[]{android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
    applyBoxAttributes();
  }

  
  public int getBoxBackgroundColor() {
    return boxBackgroundColor;
  }

  
  public void setBoxCornerRadiiResources(
      @DimenRes int boxCornerRadiusTopStartId,
      @DimenRes int boxCornerRadiusTopEndId,
      @DimenRes int boxCornerRadiusBottomEndId,
      @DimenRes int boxCornerRadiusBottomStartId) {
    setBoxCornerRadii(
        getContext().getResources().getDimension(boxCornerRadiusTopStartId),
        getContext().getResources().getDimension(boxCornerRadiusTopEndId),
        getContext().getResources().getDimension(boxCornerRadiusBottomStartId),
        getContext().getResources().getDimension(boxCornerRadiusBottomEndId));
  }

  
  public void setBoxCornerRadii(
      float boxCornerRadiusTopStart,
      float boxCornerRadiusTopEnd,
      float boxCornerRadiusBottomStart,
      float boxCornerRadiusBottomEnd) {
    if (boxBackground == null
        || boxBackground.getTopLeftCornerResolvedSize() != boxCornerRadiusTopStart
        || boxBackground.getTopRightCornerResolvedSize() != boxCornerRadiusTopEnd
        || boxBackground.getBottomRightCornerResolvedSize() != boxCornerRadiusBottomEnd
        || boxBackground.getBottomLeftCornerResolvedSize() != boxCornerRadiusBottomStart) {
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

  
  public float getBoxCornerRadiusTopStart() {
    return boxBackground.getTopLeftCornerResolvedSize();
  }

  
  public float getBoxCornerRadiusTopEnd() {
    return boxBackground.getTopRightCornerResolvedSize();
  }

  
  public float getBoxCornerRadiusBottomEnd() {
    return boxBackground.getBottomLeftCornerResolvedSize();
  }

  
  public float getBoxCornerRadiusBottomStart() {
    return boxBackground.getBottomRightCornerResolvedSize();
  }

  
  @SuppressWarnings("ReferenceEquality") 
  public void setTypeface(@Nullable Typeface typeface) {
    if (typeface != this.typeface) {
      this.typeface = typeface;

      collapsingTextHelper.setTypefaces(typeface);
      indicatorViewController.setTypefaces(typeface);

      if (counterView != null) {
        counterView.setTypeface(typeface);
      }
    }
  }

  
  @Nullable
  public Typeface getTypeface() {
    return typeface;
  }

  @Override
  public void dispatchProvideAutofillStructure(@NonNull ViewStructure structure, int flags) {
    if (originalHint == null || editText == null) {
      super.dispatchProvideAutofillStructure(structure, flags);
      return;
    }

    
    
    boolean wasProvidingHint = isProvidingHint;
    
    isProvidingHint = false;
    final CharSequence hint = editText.getHint();
    editText.setHint(originalHint);
    try {
      super.dispatchProvideAutofillStructure(structure, flags);
    } finally {
      editText.setHint(hint);
      isProvidingHint = wasProvidingHint;
    }
  }

  private void setEditText(EditText editText) {
    
    if (this.editText != null) {
      throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    if (endIconMode != END_ICON_DROPDOWN_MENU && !(editText instanceof TextInputEditText)) {
      Log.i(
          LOG_TAG,
          "EditText added is not a TextInputEditText. Please switch to using that"
              + " class instead.");
    }

    this.editText = editText;
    onApplyBoxBackgroundMode();
    setTextInputAccessibilityDelegate(new AccessibilityDelegate(this));

    
    collapsingTextHelper.setTypefaces(this.editText.getTypeface());
    collapsingTextHelper.setExpandedTextSize(this.editText.getTextSize());

    final int editTextGravity = this.editText.getGravity();
    collapsingTextHelper.setCollapsedTextGravity(
        Gravity.TOP | (editTextGravity & ~Gravity.VERTICAL_GRAVITY_MASK));
    collapsingTextHelper.setExpandedTextGravity(editTextGravity);

    
    this.editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void afterTextChanged(@NonNull Editable s) {
            updateLabelState(!restoringSavedState);
            if (counterEnabled) {
              updateCounter(s.length());
            }
            if (placeholderEnabled) {
              updatePlaceholderText(s.length());
            }
          }

          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    
    if (defaultHintTextColor == null) {
      defaultHintTextColor = this.editText.getHintTextColors();
    }

    
    if (hintEnabled) {
      if (TextUtils.isEmpty(hint)) {
        
        originalHint = this.editText.getHint();
        setHint(originalHint);
        
        this.editText.setHint(null);
      }
      this.isProvidingHint = true;
    }

    if (counterView != null) {
      updateCounter(this.editText.getText().length());
    }
    updateEditTextBackground();

    indicatorViewController.adjustIndicatorPadding();

    startLayout.bringToFront();
    endLayout.bringToFront();
    endIconFrame.bringToFront();
    errorIconView.bringToFront();
    dispatchOnEditTextAttached();
    updatePrefixTextViewPadding();
    updateSuffixTextViewPadding();

    
    
    if (!isEnabled()) {
      editText.setEnabled(false);
    }

    
    updateLabelState(false, true);
  }

  private void updateInputLayoutMargins() {
    
    
    if (boxBackgroundMode != BOX_BACKGROUND_FILLED) {
      final LayoutParams lp = (LayoutParams) inputFrame.getLayoutParams();
      final int newTopMargin = calculateLabelMarginTop();

      if (newTopMargin != lp.topMargin) {
        lp.topMargin = newTopMargin;
        inputFrame.requestLayout();
      }
    }
  }

  @Override
  public int getBaseline() {
    if (editText != null) {
      return editText.getBaseline() + getPaddingTop() + calculateLabelMarginTop();
    } else {
      return super.getBaseline();
    }
  }

  void updateLabelState(boolean animate) {
    updateLabelState(animate, false);
  }

  private void updateLabelState(boolean animate, boolean force) {
    final boolean isEnabled = isEnabled();
    final boolean hasText = editText != null && !TextUtils.isEmpty(editText.getText());
    final boolean hasFocus = editText != null && editText.hasFocus();
    final boolean errorShouldBeShown = indicatorViewController.errorShouldBeShown();

    
    if (defaultHintTextColor != null) {
      collapsingTextHelper.setCollapsedTextColor(defaultHintTextColor);
      collapsingTextHelper.setExpandedTextColor(defaultHintTextColor);
    }

    
    if (!isEnabled) {
      int disabledHintColor =
          defaultHintTextColor != null
              ? defaultHintTextColor.getColorForState(
                  new int[] {-android.R.attr.state_enabled}, disabledColor)
              : disabledColor;
      collapsingTextHelper.setCollapsedTextColor(ColorStateList.valueOf(disabledHintColor));
      collapsingTextHelper.setExpandedTextColor(ColorStateList.valueOf(disabledHintColor));
    } else if (errorShouldBeShown) {
      collapsingTextHelper.setCollapsedTextColor(indicatorViewController.getErrorViewTextColors());
    } else if (counterOverflowed && counterView != null) {
      collapsingTextHelper.setCollapsedTextColor(counterView.getTextColors());
    } else if (hasFocus && focusedTextColor != null) {
      collapsingTextHelper.setCollapsedTextColor(focusedTextColor);
    } 

    if (hasText || (isEnabled() && (hasFocus || errorShouldBeShown))) {
      
      if (force || hintExpanded) {
        collapseHint(animate);
      }
    } else {
      
      if (force || !hintExpanded) {
        expandHint(animate);
      }
    }
  }

  
  @Nullable
  public EditText getEditText() {
    return editText;
  }

  
  public void setHint(@Nullable CharSequence hint) {
    if (hintEnabled) {
      setHintInternal(hint);
      sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
  }

  private void setHintInternal(CharSequence hint) {
    if (!TextUtils.equals(hint, this.hint)) {
      this.hint = hint;
      collapsingTextHelper.setText(hint);
      
      if (!hintExpanded) {
        openCutout();
      }
    }
  }

  
  @Nullable
  public CharSequence getHint() {
    return hintEnabled ? hint : null;
  }

  
  public void setHintEnabled(boolean enabled) {
    if (enabled != hintEnabled) {
      hintEnabled = enabled;
      if (!hintEnabled) {
        
        isProvidingHint = false;
        if (!TextUtils.isEmpty(hint) && TextUtils.isEmpty(editText.getHint())) {
          
          editText.setHint(hint);
        }
        
        setHintInternal(null);
      } else {
        final CharSequence editTextHint = editText.getHint();
        if (!TextUtils.isEmpty(editTextHint)) {
          
          
          if (TextUtils.isEmpty(hint)) {
            setHint(editTextHint);
          }
          editText.setHint(null);
        }
        isProvidingHint = true;
      }

      
      if (editText != null) {
        updateInputLayoutMargins();
      }
    }
  }

  
  public boolean isHintEnabled() {
    return hintEnabled;
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public boolean isProvidingHint() {
    return isProvidingHint;
  }

  
  public void setHintTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setCollapsedTextAppearance(resId);
    focusedTextColor = collapsingTextHelper.getCollapsedTextColor();

    if (editText != null) {
      updateLabelState(false);
      
      updateInputLayoutMargins();
    }
  }
  
  public void setHintTextColor(@Nullable ColorStateList hintTextColor) {
    if (focusedTextColor != hintTextColor) {
      if (defaultHintTextColor == null) {
        collapsingTextHelper.setCollapsedTextColor(hintTextColor);
      }

      focusedTextColor = hintTextColor;

      if (editText != null) {
        updateLabelState(false);
      }
    }
  }

  
  @Nullable
  public ColorStateList getHintTextColor() {
    return focusedTextColor;
  }

  
  public void setDefaultHintTextColor(@Nullable ColorStateList textColor) {
    defaultHintTextColor = textColor;
    focusedTextColor = textColor;

    if (editText != null) {
      updateLabelState(false);
    }
  }

  
  @Nullable
  public ColorStateList getDefaultHintTextColor() {
    return defaultHintTextColor;
  }

  
  public void setErrorEnabled(boolean enabled) {
    indicatorViewController.setErrorEnabled(enabled);
  }

  
  public void setErrorTextAppearance(@StyleRes int errorTextAppearance) {
    indicatorViewController.setErrorTextAppearance(errorTextAppearance);
  }

  
  public void setErrorTextColor(@Nullable ColorStateList errorTextColor) {
    indicatorViewController.setErrorViewTextColor(errorTextColor);
  }

  
  @ColorInt
  public int getErrorCurrentTextColors() {
    return indicatorViewController.getErrorViewCurrentTextColor();
  }

  
  public void setHelperTextTextAppearance(@StyleRes int helperTextTextAppearance) {
    indicatorViewController.setHelperTextAppearance(helperTextTextAppearance);
  }

  
  public void setHelperTextColor(@Nullable ColorStateList helperTextColor) {
    indicatorViewController.setHelperTextViewTextColor(helperTextColor);
  }

  
  public boolean isErrorEnabled() {
    return indicatorViewController.isErrorEnabled();
  }

  
  public void setHelperTextEnabled(boolean enabled) {
    indicatorViewController.setHelperTextEnabled(enabled);
  }

  
  public void setHelperText(@Nullable final CharSequence helperText) {
    
    if (TextUtils.isEmpty(helperText)) {
      if (isHelperTextEnabled()) {
        setHelperTextEnabled(false);
      }
    } else {
      if (!isHelperTextEnabled()) {
        setHelperTextEnabled(true);
      }
      indicatorViewController.showHelper(helperText);
    }
  }

  
  public boolean isHelperTextEnabled() {
    return indicatorViewController.isHelperTextEnabled();
  }

  
  @ColorInt
  public int getHelperTextCurrentTextColor() {
    return indicatorViewController.getHelperTextViewCurrentTextColor();
  }

  
  public void setErrorContentDescription(@Nullable final CharSequence errorContentDecription) {
    indicatorViewController.setErrorContentDescription(errorContentDecription);
  }

  
  @Nullable
  public CharSequence getErrorContentDescription() {
    return indicatorViewController.getErrorContentDescription();
  }

  
  public void setError(@Nullable final CharSequence errorText) {
    if (!indicatorViewController.isErrorEnabled()) {
      if (TextUtils.isEmpty(errorText)) {
        
        return;
      }
      
      setErrorEnabled(true);
    }

    if (!TextUtils.isEmpty(errorText)) {
      indicatorViewController.showError(errorText);
    } else {
      indicatorViewController.hideError();
    }
  }

  
  public void setErrorIconDrawable(@DrawableRes int resId) {
    setErrorIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  
  public void setErrorIconDrawable(@Nullable Drawable errorIconDrawable) {
    errorIconView.setImageDrawable(errorIconDrawable);
    setErrorIconVisible(errorIconDrawable != null && indicatorViewController.isErrorEnabled());
  }

  
  @Nullable
  public Drawable getErrorIconDrawable() {
    return errorIconView.getDrawable();
  }

  
  public void setErrorIconTintList(@Nullable ColorStateList errorIconTintList) {
    this.errorIconTintList = errorIconTintList;
    Drawable icon = errorIconView.getDrawable();
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintList(icon, errorIconTintList);
    }

    if (errorIconView.getDrawable() != icon) {
      errorIconView.setImageDrawable(icon);
    }
  }

  
  public void setErrorIconTintMode(@Nullable PorterDuff.Mode errorIconTintMode) {
    Drawable icon = errorIconView.getDrawable();
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintMode(icon, errorIconTintMode);
    }

    if (errorIconView.getDrawable() != icon) {
      errorIconView.setImageDrawable(icon);
    }
  }

  
  public void setCounterEnabled(boolean enabled) {
    if (counterEnabled != enabled) {
      if (enabled) {
        counterView = new AppCompatTextView(getContext());
        counterView.setId(R.id.textinput_counter);
        if (typeface != null) {
          counterView.setTypeface(typeface);
        }
        counterView.setMaxLines(1);
        indicatorViewController.addIndicator(counterView, COUNTER_INDEX);
        MarginLayoutParamsCompat.setMarginStart(
            (MarginLayoutParams) counterView.getLayoutParams(),
            getResources().getDimensionPixelOffset(R.dimen.mtrl_textinput_counter_margin_start));
        updateCounterTextAppearanceAndColor();
        updateCounter();
      } else {
        indicatorViewController.removeIndicator(counterView, COUNTER_INDEX);
        counterView = null;
      }
      counterEnabled = enabled;
    }
  }

  
  public void setCounterTextAppearance(int counterTextAppearance) {
    if (this.counterTextAppearance != counterTextAppearance) {
      this.counterTextAppearance = counterTextAppearance;
      updateCounterTextAppearanceAndColor();
    }
  }

  
  public void setCounterTextColor(@Nullable ColorStateList counterTextColor) {
    if (this.counterTextColor != counterTextColor) {
      this.counterTextColor = counterTextColor;
      updateCounterTextAppearanceAndColor();
    }
  }

  
  @Nullable
  public ColorStateList getCounterTextColor() {
    return counterTextColor;
  }

  
  public void setCounterOverflowTextAppearance(int counterOverflowTextAppearance) {
    if (this.counterOverflowTextAppearance != counterOverflowTextAppearance) {
      this.counterOverflowTextAppearance = counterOverflowTextAppearance;
      updateCounterTextAppearanceAndColor();
    }
  }

  
  public void setCounterOverflowTextColor(@Nullable ColorStateList counterOverflowTextColor) {
    if (this.counterOverflowTextColor != counterOverflowTextColor) {
      this.counterOverflowTextColor = counterOverflowTextColor;
      updateCounterTextAppearanceAndColor();
    }
  }

  
  @Nullable
  public ColorStateList getCounterOverflowTextColor() {
    return counterTextColor;
  }

  
  public boolean isCounterEnabled() {
    return counterEnabled;
  }

  
  public void setCounterMaxLength(int maxLength) {
    if (counterMaxLength != maxLength) {
      if (maxLength > 0) {
        counterMaxLength = maxLength;
      } else {
        counterMaxLength = INVALID_MAX_LENGTH;
      }
      if (counterEnabled) {
        updateCounter();
      }
    }
  }

  private void updateCounter() {
    if (counterView != null) {
      updateCounter(editText == null ? 0 : editText.getText().length());
    }
  }

  void updateCounter(int length) {
    boolean wasCounterOverflowed = counterOverflowed;
    if (counterMaxLength == INVALID_MAX_LENGTH) {
      counterView.setText(String.valueOf(length));
      counterView.setContentDescription(null);
      counterOverflowed = false;
    } else {
      counterOverflowed = length > counterMaxLength;
      updateCounterContentDescription(
          getContext(), counterView, length, counterMaxLength, counterOverflowed);

      if (wasCounterOverflowed != counterOverflowed) {
        updateCounterTextAppearanceAndColor();
      }
      BidiFormatter bidiFormatter = BidiFormatter.getInstance();
      counterView.setText(
          bidiFormatter.unicodeWrap(
              getContext()
                  .getString(R.string.character_counter_pattern, length, counterMaxLength)));
    }
    if (editText != null && wasCounterOverflowed != counterOverflowed) {
      updateLabelState(false);
      updateTextInputBoxState();
      updateEditTextBackground();
    }
  }

  private static void updateCounterContentDescription(
      @NonNull Context context,
      @NonNull TextView counterView,
      int length,
      int counterMaxLength,
      boolean counterOverflowed) {
    counterView.setContentDescription(
        context.getString(
            counterOverflowed
                ? R.string.character_counter_overflowed_content_description
                : R.string.character_counter_content_description,
            length,
            counterMaxLength));
  }

  
  public void setPlaceholderText(@Nullable final CharSequence placeholderText) {
    
    if (placeholderEnabled && TextUtils.isEmpty(placeholderText)) {
      setPlaceholderTextEnabled(false);
    } else {
      if (!placeholderEnabled) {
        
        setPlaceholderTextEnabled(true);
      }
      this.placeholderText = placeholderText;
    }
    updatePlaceholderText();
  }

  
  @Nullable
  public CharSequence getPlaceholderText() {
    return placeholderEnabled ? placeholderText : null;
  }

  private void setPlaceholderTextEnabled(boolean placeholderEnabled) {
    
    if (this.placeholderEnabled == placeholderEnabled) {
      return;
    }

    
    if (placeholderEnabled) {
      placeholderTextView = new AppCompatTextView(getContext());
      placeholderTextView.setId(R.id.textinput_placeholder);

      ViewCompat.setAccessibilityLiveRegion(
          placeholderTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

      setPlaceholderTextAppearance(placeholderTextAppearance);
      setPlaceholderTextColor(placeholderTextColor);
      addPlaceholderTextView();
    } else {
      removePlaceholderTextView();
      placeholderTextView = null;
    }
    this.placeholderEnabled = placeholderEnabled;
  }

  private void updatePlaceholderText() {
    updatePlaceholderText(editText == null ? 0 : editText.getText().length());
  }

  private void updatePlaceholderText(int inputTextLength) {
    if (inputTextLength == 0 && !hintExpanded) {
      showPlaceholderText();
    } else {
      hidePlaceholderText();
    }
  }

  private void showPlaceholderText() {
    if (placeholderTextView != null && placeholderEnabled) {
      placeholderTextView.setText(placeholderText);
      placeholderTextView.setVisibility(VISIBLE);
      placeholderTextView.bringToFront();
    }
  }

  private void hidePlaceholderText() {
    if (placeholderTextView != null && placeholderEnabled) {
      placeholderTextView.setText(null);
      placeholderTextView.setVisibility(INVISIBLE);
    }
  }

  private void addPlaceholderTextView() {
    if (placeholderTextView != null) {
      inputFrame.addView(placeholderTextView);
      placeholderTextView.setVisibility(VISIBLE);
    }
  }

  private void removePlaceholderTextView() {
    if (placeholderTextView != null) {
      placeholderTextView.setVisibility(GONE);
    }
  }

  
  public void setPlaceholderTextColor(@Nullable ColorStateList placeholderTextColor) {
    if (this.placeholderTextColor != placeholderTextColor) {
      this.placeholderTextColor = placeholderTextColor;
      if (placeholderTextView != null && placeholderTextColor != null) {
        placeholderTextView.setTextColor(placeholderTextColor);
      }
    }
  }

  
  @Nullable
  public ColorStateList getPlaceholderTextColor() {
    return placeholderTextColor;
  }

  
  public void setPlaceholderTextAppearance(@StyleRes int placeholderTextAppearance) {
    this.placeholderTextAppearance = placeholderTextAppearance;
    if (placeholderTextView != null) {
      TextViewCompat.setTextAppearance(placeholderTextView, placeholderTextAppearance);
    }
  }

  
  @StyleRes
  public int getPlaceholderTextAppearance() {
    return placeholderTextAppearance;
  }

  
  public void setPrefixText(@Nullable final CharSequence prefixText) {
    this.prefixText = TextUtils.isEmpty(prefixText) ? null : prefixText;
    prefixTextView.setText(prefixText);
    updatePrefixTextVisibility();
  }

  
  @Nullable
  public CharSequence getPrefixText() {
    return prefixText;
  }

  
  @NonNull
  public TextView getPrefixTextView() {
    return prefixTextView;
  }

  private void updatePrefixTextVisibility() {
    prefixTextView.setVisibility((prefixText != null && !isHintExpanded()) ? VISIBLE : GONE);
    updateDummyDrawables();
  }

  
  public void setPrefixTextColor(@NonNull ColorStateList prefixTextColor) {
    prefixTextView.setTextColor(prefixTextColor);
  }

  
  @Nullable
  public ColorStateList getPrefixTextColor() {
    return prefixTextView.getTextColors();
  }

  
  public void setPrefixTextAppearance(@StyleRes int prefixTextAppearance) {
    TextViewCompat.setTextAppearance(prefixTextView, prefixTextAppearance);
  }

  private void updatePrefixTextViewPadding() {
    if (editText == null) {
      return;
    }
    int startPadding = isStartIconVisible() ? 0 : ViewCompat.getPaddingStart(editText);
    ViewCompat.setPaddingRelative(
        prefixTextView,
        startPadding,
        editText.getCompoundPaddingTop(),
        0,
        editText.getCompoundPaddingBottom());
  }

  
  public void setSuffixText(@Nullable final CharSequence suffixText) {
    this.suffixText = TextUtils.isEmpty(suffixText) ? null : suffixText;
    suffixTextView.setText(suffixText);
    updateSuffixTextVisibility();
  }

  
  @Nullable
  public CharSequence getSuffixText() {
    return suffixText;
  }

  
  @NonNull
  public TextView getSuffixTextView() {
    return suffixTextView;
  }

  private void updateSuffixTextVisibility() {
    int oldSuffixVisibility = suffixTextView.getVisibility();
    boolean visible = suffixText != null && !isHintExpanded();
    suffixTextView.setVisibility(visible ? VISIBLE : GONE);
    if (oldSuffixVisibility != suffixTextView.getVisibility()) {
      getEndIconDelegate().onSuffixVisibilityChanged(visible);
    }
    updateDummyDrawables();
  }

  
  public void setSuffixTextColor(@NonNull ColorStateList suffixTextColor) {
    suffixTextView.setTextColor(suffixTextColor);
  }

  
  @Nullable
  public ColorStateList getSuffixTextColor() {
    return suffixTextView.getTextColors();
  }

  
  public void setSuffixTextAppearance(@StyleRes int suffixTextAppearance) {
    TextViewCompat.setTextAppearance(suffixTextView, suffixTextAppearance);
  }

  private void updateSuffixTextViewPadding() {
    if (editText == null) {
      return;
    }
    int endPadding =
        (isEndIconVisible() || isErrorIconVisible()) ? 0 : ViewCompat.getPaddingEnd(editText);
    ViewCompat.setPaddingRelative(
        suffixTextView, 0, editText.getPaddingTop(), endPadding, editText.getPaddingBottom());
  }

  @Override
  public void setEnabled(boolean enabled) {
    
    
    
    recursiveSetEnabled(this, enabled);
    super.setEnabled(enabled);
  }

  private static void recursiveSetEnabled(@NonNull final ViewGroup vg, final boolean enabled) {
    for (int i = 0, count = vg.getChildCount(); i < count; i++) {
      final View child = vg.getChildAt(i);
      child.setEnabled(enabled);
      if (child instanceof ViewGroup) {
        recursiveSetEnabled((ViewGroup) child, enabled);
      }
    }
  }

  
  public int getCounterMaxLength() {
    return counterMaxLength;
  }

  
  @Nullable
  CharSequence getCounterOverflowDescription() {
    if (counterEnabled && counterOverflowed && (counterView != null)) {
      return counterView.getContentDescription();
    }
    return null;
  }

  private void updateCounterTextAppearanceAndColor() {
    if (counterView != null) {
      setTextAppearanceCompatWithErrorFallback(
          counterView, counterOverflowed ? counterOverflowTextAppearance : counterTextAppearance);
      if (!counterOverflowed && counterTextColor != null) {
        counterView.setTextColor(counterTextColor);
      }
      if (counterOverflowed && counterOverflowTextColor != null) {
        counterView.setTextColor(counterOverflowTextColor);
      }
    }
  }

  void setTextAppearanceCompatWithErrorFallback(
      @NonNull TextView textView, @StyleRes int textAppearance) {
    boolean useDefaultColor = false;
    try {
      TextViewCompat.setTextAppearance(textView, textAppearance);

      if (VERSION.SDK_INT >= VERSION_CODES.M
          && textView.getTextColors().getDefaultColor() == Color.MAGENTA) {
        
        
        
        useDefaultColor = true;
      }
    } catch (Exception e) {
      
      
      useDefaultColor = true;
    }
    if (useDefaultColor) {
      
      
      TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_AppCompat_Caption);
      textView.setTextColor(ContextCompat.getColor(getContext(), R.color.design_error));
    }
  }

  private int calculateLabelMarginTop() {
    if (!hintEnabled) {
      return 0;
    }

    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_OUTLINE:
        return (int) (collapsingTextHelper.getCollapsedTextHeight() / 2);
      case BOX_BACKGROUND_FILLED:
      case BOX_BACKGROUND_NONE:
        return (int) collapsingTextHelper.getCollapsedTextHeight();
      default:
        return 0;
    }
  }

  @NonNull
  private Rect calculateCollapsedTextBounds(@NonNull Rect rect) {
    if (editText == null) {
      throw new IllegalStateException();
    }
    Rect bounds = tmpBoundsRect;
    boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

    bounds.bottom = rect.bottom;
    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_OUTLINE:
        bounds.left = rect.left + editText.getPaddingLeft();
        bounds.top = rect.top - calculateLabelMarginTop();
        bounds.right = rect.right - editText.getPaddingRight();
        return bounds;
      case BOX_BACKGROUND_FILLED:
        bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left, isRtl);
        bounds.top = rect.top + boxCollapsedPaddingTopPx;
        bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right, isRtl);
        return bounds;
      case BOX_BACKGROUND_NONE:
      default:
        bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left, isRtl);
        bounds.top = getPaddingTop();
        bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right, isRtl);
        return bounds;
    }
  }

  private int getLabelLeftBoundAlightWithPrefix(int rectLeft, boolean isRtl) {
    int left = rectLeft + editText.getCompoundPaddingLeft();
    if (prefixText != null && !isRtl) {
      
      left = left - prefixTextView.getMeasuredWidth() + prefixTextView.getPaddingLeft();
    }
    return left;
  }

  private int getLabelRightBoundAlignedWithSuffix(int rectRight, boolean isRtl) {
    int right = rectRight - editText.getCompoundPaddingRight();
    if (prefixText != null && isRtl) {
      
      right += prefixTextView.getMeasuredWidth() - prefixTextView.getPaddingRight();
    }
    return right;
  }

  @NonNull
  private Rect calculateExpandedTextBounds(@NonNull Rect rect) {
    if (editText == null) {
      throw new IllegalStateException();
    }

    Rect bounds = tmpBoundsRect;

    float labelHeight = collapsingTextHelper.getExpandedTextHeight();

    bounds.left = rect.left + editText.getCompoundPaddingLeft();
    bounds.top = calculateExpandedLabelTop(rect, labelHeight);
    bounds.right = rect.right - editText.getCompoundPaddingRight();
    bounds.bottom = calculateExpandedLabelBottom(rect, bounds, labelHeight);

    return bounds;
  }

  private int calculateExpandedLabelTop(@NonNull Rect rect, float labelHeight) {
    if (isSingleLineFilledTextField()) {
      return (int) (rect.centerY() - labelHeight / 2);
    }
    return rect.top + editText.getCompoundPaddingTop();
  }

  private int calculateExpandedLabelBottom(
      @NonNull Rect rect, @NonNull Rect bounds, float labelHeight) {
    if (isSingleLineFilledTextField()) {
      
      
      
      return (int) (bounds.top + labelHeight);
    }
    return rect.bottom - editText.getCompoundPaddingBottom();
  }

  private boolean isSingleLineFilledTextField() {
    return boxBackgroundMode == BOX_BACKGROUND_FILLED
        && (VERSION.SDK_INT < 16 || editText.getMinLines() <= 1);
  }

  
  private int calculateBoxBackgroundColor() {
    int backgroundColor = boxBackgroundColor;
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED) {
      int surfaceLayerColor = MaterialColors.getColor(this, R.attr.colorSurface, Color.TRANSPARENT);
      backgroundColor = MaterialColors.layer(surfaceLayerColor, boxBackgroundColor);
    }
    return backgroundColor;
  }

  private void applyBoxAttributes() {
    if (boxBackground == null) {
      return;
    }

    boxBackground.setShapeAppearanceModel(shapeAppearanceModel);

    if (canDrawOutlineStroke()) {
      boxBackground.setStroke(boxStrokeWidthPx, boxStrokeColor);
    }

    boxBackgroundColor = calculateBoxBackgroundColor();
    boxBackground.setFillColor(ColorStateList.valueOf(boxBackgroundColor));
    if (endIconMode == END_ICON_DROPDOWN_MENU) {
      
      editText.getBackground().invalidateSelf();
    }
    applyBoxUnderlineAttributes();
    invalidate();
  }

  private void applyBoxUnderlineAttributes() {
    
    if (boxUnderline == null) {
      return;
    }

    if (canDrawStroke()) {
      boxUnderline.setFillColor(ColorStateList.valueOf(boxStrokeColor));
    }
    invalidate();
  }

  private boolean canDrawOutlineStroke() {
    return boxBackgroundMode == BOX_BACKGROUND_OUTLINE && canDrawStroke();
  }

  private boolean canDrawStroke() {
    return boxStrokeWidthPx > -1 && boxStrokeColor != Color.TRANSPARENT;
  }

  void updateEditTextBackground() {
    
    
    if (editText == null || boxBackgroundMode != BOX_BACKGROUND_NONE) {
      return;
    }

    Drawable editTextBackground = editText.getBackground();
    if (editTextBackground == null) {
      return;
    }

    if (androidx.appcompat.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
      editTextBackground = editTextBackground.mutate();
    }

    if (indicatorViewController.errorShouldBeShown()) {
      
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              indicatorViewController.getErrorViewCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else if (counterOverflowed && counterView != null) {
      
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              counterView.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else {
      
      
      DrawableCompat.clearColorFilter(editTextBackground);
      editText.refreshDrawableState();
    }
  }

  static class SavedState extends AbsSavedState {
    @Nullable CharSequence error;
    boolean isEndIconChecked;

    SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
      isEndIconChecked = (source.readInt() == 1);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      TextUtils.writeToParcel(error, dest, flags);
      dest.writeInt(isEndIconChecked ? 1 : 0);
    }

    @NonNull
    @Override
    public String toString() {
      return "TextInputLayout.SavedState{"
          + Integer.toHexString(System.identityHashCode(this))
          + " error="
          + error
          + "}";
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  @Nullable
  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    if (indicatorViewController.errorShouldBeShown()) {
      ss.error = getError();
    }
    ss.isEndIconChecked = hasEndIcon() && endIconView.isChecked();
    return ss;
  }

  @Override
  protected void onRestoreInstanceState(@Nullable Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    setError(ss.error);
    if (ss.isEndIconChecked) {
      
      endIconView.post(
          new Runnable() {
            @Override
            public void run() {
              endIconView.performClick();
              
              endIconView.jumpDrawablesToCurrentState();
            }
          });
    }
    requestLayout();
  }

  @Override
  protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
    restoringSavedState = true;
    super.dispatchRestoreInstanceState(container);
    restoringSavedState = false;
  }

  
  @Nullable
  public CharSequence getError() {
    return indicatorViewController.isErrorEnabled() ? indicatorViewController.getErrorText() : null;
  }

  
  @Nullable
  public CharSequence getHelperText() {
    return indicatorViewController.isHelperTextEnabled()
        ? indicatorViewController.getHelperText()
        : null;
  }

  
  public boolean isHintAnimationEnabled() {
    return hintAnimationEnabled;
  }

  
  public void setHintAnimationEnabled(boolean enabled) {
    hintAnimationEnabled = enabled;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    boolean updatedHeight = updateEditTextHeightBasedOnIcon();
    boolean updatedIcon = updateDummyDrawables();
    if (updatedHeight || updatedIcon) {
      editText.post(
          new Runnable() {
            @Override
            public void run() {
              editText.requestLayout();
            }
          });
    }
    updatePlaceholderMeasurementsBasedOnEditText();
    updatePrefixTextViewPadding();
    updateSuffixTextViewPadding();
  }

  private boolean updateEditTextHeightBasedOnIcon() {
    if (editText == null) {
      return false;
    }

    
    
    
    int maxIconHeight = Math.max(endLayout.getMeasuredHeight(), startLayout.getMeasuredHeight());
    if (editText.getMeasuredHeight() < maxIconHeight) {
      editText.setMinimumHeight(maxIconHeight);
      return true;
    }

    return false;
  }

  private void updatePlaceholderMeasurementsBasedOnEditText() {
    if (placeholderTextView != null && editText != null) {
      
      final int editTextGravity = this.editText.getGravity();
      placeholderTextView.setGravity(editTextGravity);

      placeholderTextView.setPadding(
          editText.getCompoundPaddingLeft(),
          editText.getCompoundPaddingTop(),
          editText.getCompoundPaddingRight(),
          editText.getCompoundPaddingBottom());
    }
  }

  
  public void setStartIconDrawable(@DrawableRes int resId) {
    setStartIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  
  public void setStartIconDrawable(@Nullable Drawable startIconDrawable) {
    startIconView.setImageDrawable(startIconDrawable);
    if (startIconDrawable != null) {
      setStartIconVisible(true);
      applyStartIconTint();
    } else {
      setStartIconVisible(false);
      setStartIconOnClickListener(null);
      setStartIconOnLongClickListener(null);
      setStartIconContentDescription(null);
    }
  }

  
  @Nullable
  public Drawable getStartIconDrawable() {
    return startIconView.getDrawable();
  }

  
  public void setStartIconOnClickListener(@Nullable OnClickListener startIconOnClickListener) {
    setIconOnClickListener(startIconView, startIconOnClickListener, startIconOnLongClickListener);
  }

  
  public void setStartIconOnLongClickListener(
      @Nullable OnLongClickListener startIconOnLongClickListener) {
    this.startIconOnLongClickListener = startIconOnLongClickListener;
    setIconOnLongClickListener(startIconView, startIconOnLongClickListener);
  }

  
  public void setStartIconVisible(boolean visible) {
    if (isStartIconVisible() != visible) {
      startIconView.setVisibility(visible ? View.VISIBLE : View.GONE);
      updatePrefixTextViewPadding();
      updateDummyDrawables();
    }
  }

  
  public boolean isStartIconVisible() {
    return startIconView.getVisibility() == View.VISIBLE;
  }

  
  public void setStartIconCheckable(boolean startIconCheckable) {
    startIconView.setCheckable(startIconCheckable);
  }

  
  public boolean isStartIconCheckable() {
    return startIconView.isCheckable();
  }

  
  public void setStartIconContentDescription(@StringRes int resId) {
    setStartIconContentDescription(resId != 0 ? getResources().getText(resId) : null);
  }

  
  public void setStartIconContentDescription(@Nullable CharSequence startIconContentDescription) {
    if (getStartIconContentDescription() != startIconContentDescription) {
      startIconView.setContentDescription(startIconContentDescription);
    }
  }

  
  @Nullable
  public CharSequence getStartIconContentDescription() {
    return startIconView.getContentDescription();
  }

  
  public void setStartIconTintList(@Nullable ColorStateList startIconTintList) {
    if (this.startIconTintList != startIconTintList) {
      this.startIconTintList = startIconTintList;
      hasStartIconTintList = true;
      applyStartIconTint();
    }
  }

  
  public void setStartIconTintMode(@Nullable PorterDuff.Mode startIconTintMode) {
    if (this.startIconTintMode != startIconTintMode) {
      this.startIconTintMode = startIconTintMode;
      hasStartIconTintMode = true;
      applyStartIconTint();
    }
  }

  
  public void setEndIconMode(@EndIconMode int endIconMode) {
    int previousEndIconMode = this.endIconMode;
    this.endIconMode = endIconMode;
    dispatchOnEndIconChanged(previousEndIconMode);
    setEndIconVisible(endIconMode != END_ICON_NONE);
    if (getEndIconDelegate().isBoxBackgroundModeSupported(boxBackgroundMode)) {
      getEndIconDelegate().initialize();
    } else {
      throw new IllegalStateException(
          "The current box background mode "
              + boxBackgroundMode
              + " is not supported by the end icon mode "
              + endIconMode);
    }
    applyEndIconTint();
  }

  
  @EndIconMode
  public int getEndIconMode() {
    return endIconMode;
  }

  
  public void setEndIconOnClickListener(@Nullable OnClickListener endIconOnClickListener) {
    setIconOnClickListener(endIconView, endIconOnClickListener, endIconOnLongClickListener);
  }

  
  public void setErrorIconOnClickListener(@Nullable OnClickListener errorIconOnClickListener) {
    setIconOnClickListener(errorIconView, errorIconOnClickListener, errorIconOnLongClickListener);
  }

  
  public void setEndIconOnLongClickListener(
      @Nullable OnLongClickListener endIconOnLongClickListener) {
    this.endIconOnLongClickListener = endIconOnLongClickListener;
    setIconOnLongClickListener(endIconView, endIconOnLongClickListener);
  }

  
  public void setErrorIconOnLongClickListener(
      @Nullable OnLongClickListener errorIconOnLongClickListener) {
    this.errorIconOnLongClickListener = errorIconOnLongClickListener;
    setIconOnLongClickListener(errorIconView, errorIconOnLongClickListener);
  }

  
  public void setEndIconVisible(boolean visible) {
    if (isEndIconVisible() != visible) {
      endIconView.setVisibility(visible ? View.VISIBLE : View.GONE);
      updateSuffixTextViewPadding();
      updateDummyDrawables();
    }
  }

  
  public boolean isEndIconVisible() {
    return endIconFrame.getVisibility() == VISIBLE && endIconView.getVisibility() == VISIBLE;
  }

  
  public void setEndIconActivated(boolean endIconActivated) {
    endIconView.setActivated(endIconActivated);
  }

  
  public void setEndIconCheckable(boolean endIconCheckable) {
    endIconView.setCheckable(endIconCheckable);
  }

  
  public boolean isEndIconCheckable() {
    return endIconView.isCheckable();
  }

  
  public void setEndIconDrawable(@DrawableRes int resId) {
    setEndIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  
  public void setEndIconDrawable(@Nullable Drawable endIconDrawable) {
    endIconView.setImageDrawable(endIconDrawable);
  }

  
  @Nullable
  public Drawable getEndIconDrawable() {
    return endIconView.getDrawable();
  }

  
  public void setEndIconContentDescription(@StringRes int resId) {
    setEndIconContentDescription(resId != 0 ? getResources().getText(resId) : null);
  }

  
  public void setEndIconContentDescription(@Nullable CharSequence endIconContentDescription) {
    if (getEndIconContentDescription() != endIconContentDescription) {
      endIconView.setContentDescription(endIconContentDescription);
    }
  }

  
  @Nullable
  public CharSequence getEndIconContentDescription() {
    return endIconView.getContentDescription();
  }

  
  public void setEndIconTintList(@Nullable ColorStateList endIconTintList) {
    if (this.endIconTintList != endIconTintList) {
      this.endIconTintList = endIconTintList;
      hasEndIconTintList = true;
      applyEndIconTint();
    }
  }

  
  public void setEndIconTintMode(@Nullable PorterDuff.Mode endIconTintMode) {
    if (this.endIconTintMode != endIconTintMode) {
      this.endIconTintMode = endIconTintMode;
      hasEndIconTintMode = true;
      applyEndIconTint();
    }
  }

  
  public void addOnEndIconChangedListener(@NonNull OnEndIconChangedListener listener) {
    endIconChangedListeners.add(listener);
  }

  
  public void removeOnEndIconChangedListener(@NonNull OnEndIconChangedListener listener) {
    endIconChangedListeners.remove(listener);
  }

  
  public void clearOnEndIconChangedListeners() {
    endIconChangedListeners.clear();
  }

  
  public void addOnEditTextAttachedListener(@NonNull OnEditTextAttachedListener listener) {
    editTextAttachedListeners.add(listener);
    if (editText != null) {
      listener.onEditTextAttached(this);
    }
  }

  
  public void removeOnEditTextAttachedListener(@NonNull OnEditTextAttachedListener listener) {
    editTextAttachedListeners.remove(listener);
  }

  
  public void clearOnEditTextAttachedListeners() {
    editTextAttachedListeners.clear();
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleDrawable(@DrawableRes int resId) {
    setPasswordVisibilityToggleDrawable(
        resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleDrawable(@Nullable Drawable icon) {
    endIconView.setImageDrawable(icon);
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleContentDescription(@StringRes int resId) {
    setPasswordVisibilityToggleContentDescription(
        resId != 0 ? getResources().getText(resId) : null);
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleContentDescription(@Nullable CharSequence description) {
    endIconView.setContentDescription(description);
  }

  
  @Nullable
  @Deprecated
  public Drawable getPasswordVisibilityToggleDrawable() {
    return endIconView.getDrawable();
  }

  
  @Nullable
  @Deprecated
  public CharSequence getPasswordVisibilityToggleContentDescription() {
    return endIconView.getContentDescription();
  }

  
  @Deprecated
  public boolean isPasswordVisibilityToggleEnabled() {
    return endIconMode == END_ICON_PASSWORD_TOGGLE;
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleEnabled(final boolean enabled) {
    if (enabled && endIconMode != END_ICON_PASSWORD_TOGGLE) {
      
      setEndIconMode(END_ICON_PASSWORD_TOGGLE);
    } else if (!enabled) {
      
      setEndIconMode(END_ICON_NONE);
    }
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleTintList(@Nullable ColorStateList tintList) {
    endIconTintList = tintList;
    hasEndIconTintList = true;
    applyEndIconTint();
  }

  
  @Deprecated
  public void setPasswordVisibilityToggleTintMode(@Nullable PorterDuff.Mode mode) {
    endIconTintMode = mode;
    hasEndIconTintMode = true;
    applyEndIconTint();
  }

  
  @Deprecated
  public void passwordVisibilityToggleRequested(boolean shouldSkipAnimations) {
    if (endIconMode == END_ICON_PASSWORD_TOGGLE) {
      endIconView.performClick();
      if (shouldSkipAnimations) {
        endIconView.jumpDrawablesToCurrentState();
      }
    }
  }

  
  public void setTextInputAccessibilityDelegate(
      @Nullable TextInputLayout.AccessibilityDelegate delegate) {
    if (editText != null) {
      ViewCompat.setAccessibilityDelegate(editText, delegate);
    }
  }

  @NonNull
  CheckableImageButton getEndIconView() {
    return endIconView;
  }

  private EndIconDelegate getEndIconDelegate() {
    EndIconDelegate endIconDelegate = endIconDelegates.get(endIconMode);
    return endIconDelegate != null ? endIconDelegate : endIconDelegates.get(END_ICON_NONE);
  }

  private void dispatchOnEditTextAttached() {
    for (OnEditTextAttachedListener listener : editTextAttachedListeners) {
      listener.onEditTextAttached(this);
    }
  }

  private void applyStartIconTint() {
    applyIconTint(
        startIconView,
        hasStartIconTintList,
        startIconTintList,
        hasStartIconTintMode,
        startIconTintMode);
  }

  private boolean hasEndIcon() {
    return endIconMode != END_ICON_NONE;
  }

  private void dispatchOnEndIconChanged(@EndIconMode int previousIcon) {
    for (OnEndIconChangedListener listener : endIconChangedListeners) {
      listener.onEndIconChanged(this, previousIcon);
    }
  }

  private void tintEndIconOnError(boolean tintEndIconOnError) {
    if (tintEndIconOnError && getEndIconDrawable() != null) {
      
      
      Drawable endIconDrawable = DrawableCompat.wrap(getEndIconDrawable()).mutate();
      DrawableCompat.setTint(
          endIconDrawable, indicatorViewController.getErrorViewCurrentTextColor());
      endIconView.setImageDrawable(endIconDrawable);
    } else {
      applyEndIconTint();
    }
  }

  private void applyEndIconTint() {
    applyIconTint(
        endIconView, hasEndIconTintList, endIconTintList, hasEndIconTintMode, endIconTintMode);
  }

  
  private boolean updateDummyDrawables() {
    if (editText == null) {
      return false;
    }

    boolean updatedIcon = false;
    
    if (shouldUpdateStartDummyDrawable()) {
      int right = startLayout.getMeasuredWidth() - editText.getPaddingLeft();
      if (startDummyDrawable == null || startDummyDrawableWidth != right) {
        startDummyDrawable = new ColorDrawable();
        startDummyDrawableWidth = right;
        startDummyDrawable.setBounds(0, 0, startDummyDrawableWidth, 1);
      }
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (compounds[0] != startDummyDrawable) {
        TextViewCompat.setCompoundDrawablesRelative(
            editText, startDummyDrawable, compounds[1], compounds[2], compounds[3]);
        updatedIcon = true;
      }
    } else if (startDummyDrawable != null) {
      
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      TextViewCompat.setCompoundDrawablesRelative(
          editText, null, compounds[1], compounds[2], compounds[3]);
      startDummyDrawable = null;
      updatedIcon = true;
    }

    
    if (shouldUpdateEndDummyDrawable()) {
      int right = suffixTextView.getMeasuredWidth() - editText.getPaddingRight();
      View iconView = getEndIconToUpdateDummyDrawable();
      if (iconView != null) {
        right =
            right
                + iconView.getMeasuredWidth()
                + MarginLayoutParamsCompat.getMarginStart(
                    ((MarginLayoutParams) iconView.getLayoutParams()));
      }
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (endDummyDrawable != null && endDummyDrawableWidth != right) {
        
        
        endDummyDrawableWidth = right;
        endDummyDrawable.setBounds(0, 0, endDummyDrawableWidth, 1);
        TextViewCompat.setCompoundDrawablesRelative(
            editText, compounds[0], compounds[1], endDummyDrawable, compounds[3]);
        updatedIcon = true;
      } else {
        if (endDummyDrawable == null) {
          endDummyDrawable = new ColorDrawable();
          endDummyDrawableWidth = right;
          endDummyDrawable.setBounds(0, 0, endDummyDrawableWidth, 1);
        }
        
        if (compounds[2] != endDummyDrawable) {
          originalEditTextEndDrawable = compounds[2];
          TextViewCompat.setCompoundDrawablesRelative(
              editText, compounds[0], compounds[1], endDummyDrawable, compounds[3]);
          updatedIcon = true;
        }
      }
    } else if (endDummyDrawable != null) {
      
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (compounds[2] == endDummyDrawable) {
        TextViewCompat.setCompoundDrawablesRelative(
            editText, compounds[0], compounds[1], originalEditTextEndDrawable, compounds[3]);
        updatedIcon = true;
      }
      endDummyDrawable = null;
    }

    return updatedIcon;
  }

  private boolean shouldUpdateStartDummyDrawable() {
    return (getStartIconDrawable() != null || prefixText != null)
        && (startLayout.getMeasuredWidth() > 0);
  }

  private boolean shouldUpdateEndDummyDrawable() {
    return (errorIconView.getVisibility() == VISIBLE
            || (hasEndIcon() && isEndIconVisible())
            || suffixText != null)
        && (endLayout.getMeasuredWidth() > 0);
  }

  @Nullable
  private CheckableImageButton getEndIconToUpdateDummyDrawable() {
    if (errorIconView.getVisibility() == VISIBLE) {
      return errorIconView;
    } else if (hasEndIcon() && isEndIconVisible()) {
      return endIconView;
    } else {
      return null;
    }
  }

  private void applyIconTint(
      @NonNull CheckableImageButton iconView,
      boolean hasIconTintList,
      ColorStateList iconTintList,
      boolean hasIconTintMode,
      PorterDuff.Mode iconTintMode) {
    Drawable icon = iconView.getDrawable();
    if (icon != null && (hasIconTintList || hasIconTintMode)) {
      icon = DrawableCompat.wrap(icon).mutate();

      if (hasIconTintList) {
        DrawableCompat.setTintList(icon, iconTintList);
      }
      if (hasIconTintMode) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }
    }

    if (iconView.getDrawable() != icon) {
      iconView.setImageDrawable(icon);
    }
  }

  private static void setIconOnClickListener(
      @NonNull CheckableImageButton iconView,
      @Nullable OnClickListener onClickListener,
      @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnClickListener(onClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  private static void setIconOnLongClickListener(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnLongClickListener(onLongClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  private static void setIconClickable(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
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
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (editText != null) {
      Rect rect = tmpRect;
      DescendantOffsetUtils.getDescendantRect(this, editText, rect);
      updateBoxUnderlineBounds(rect);

      if (hintEnabled) {
        collapsingTextHelper.setExpandedTextSize(editText.getTextSize());
        final int editTextGravity = editText.getGravity();
        collapsingTextHelper.setCollapsedTextGravity(
            Gravity.TOP | (editTextGravity & ~Gravity.VERTICAL_GRAVITY_MASK));
        collapsingTextHelper.setExpandedTextGravity(editTextGravity);
        collapsingTextHelper.setCollapsedBounds(calculateCollapsedTextBounds(rect));
        collapsingTextHelper.setExpandedBounds(calculateExpandedTextBounds(rect));
        collapsingTextHelper.recalculate();

        
        
        if (cutoutEnabled() && !hintExpanded) {
          openCutout();
        }
      }
    }
  }

  private void updateBoxUnderlineBounds(@NonNull Rect bounds) {
    if (boxUnderline != null) {
      int top = bounds.bottom - boxStrokeWidthFocusedPx;
      boxUnderline.setBounds(bounds.left, top, bounds.right, bounds.bottom);
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);
    drawHint(canvas);
    drawBoxUnderline(canvas);
  }

  private void drawHint(@NonNull Canvas canvas) {
    if (hintEnabled) {
      collapsingTextHelper.draw(canvas);
    }
  }

  private void drawBoxUnderline(Canvas canvas) {
    if (boxUnderline != null) {
      
      Rect underlineBounds = boxUnderline.getBounds();
      underlineBounds.top = underlineBounds.bottom - boxStrokeWidthPx;
      boxUnderline.draw(canvas);
    }
  }

  private void collapseHint(boolean animate) {
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    if (animate && hintAnimationEnabled) {
      animateToExpansionFraction(1f);
    } else {
      collapsingTextHelper.setExpansionFraction(1f);
    }
    hintExpanded = false;
    if (cutoutEnabled()) {
      openCutout();
    }
    updatePlaceholderText();

    updatePrefixTextVisibility();
    updateSuffixTextVisibility();
  }

  private boolean cutoutEnabled() {
    return hintEnabled && !TextUtils.isEmpty(hint) && boxBackground instanceof CutoutDrawable;
  }

  private void openCutout() {
    if (!cutoutEnabled()) {
      return;
    }
    final RectF cutoutBounds = tmpRectF;
    collapsingTextHelper.getCollapsedTextActualBounds(
        cutoutBounds, editText.getWidth(), editText.getGravity());
    applyCutoutPadding(cutoutBounds);
    
    
    cutoutBounds.offset(-getPaddingLeft(), -getPaddingTop());
    ((CutoutDrawable) boxBackground).setCutout(cutoutBounds);
  }

  private void closeCutout() {
    if (cutoutEnabled()) {
      ((CutoutDrawable) boxBackground).removeCutout();
    }
  }

  private void applyCutoutPadding(@NonNull RectF cutoutBounds) {
    cutoutBounds.left -= boxLabelCutoutPaddingPx;
    cutoutBounds.top -= boxLabelCutoutPaddingPx;
    cutoutBounds.right += boxLabelCutoutPaddingPx;
    cutoutBounds.bottom += boxLabelCutoutPaddingPx;
  }

  @VisibleForTesting
  boolean cutoutIsOpen() {
    return cutoutEnabled() && ((CutoutDrawable) boxBackground).hasCutout();
  }

  @Override
  protected void drawableStateChanged() {
    if (inDrawableStateChanged) {
      
      
      
      return;
    }

    inDrawableStateChanged = true;

    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    if (collapsingTextHelper != null) {
      changed |= collapsingTextHelper.setState(state);
    }

    
    if (editText != null) {
      updateLabelState(ViewCompat.isLaidOut(this) && isEnabled());
    }
    updateEditTextBackground();
    updateTextInputBoxState();

    if (changed) {
      invalidate();
    }

    inDrawableStateChanged = false;
  }

  void updateTextInputBoxState() {
    if (boxBackground == null || boxBackgroundMode == BOX_BACKGROUND_NONE) {
      return;
    }

    final boolean hasFocus = isFocused() || (editText != null && editText.hasFocus());
    final boolean isHovered = isHovered() || (editText != null && editText.isHovered());

    
    if (!isEnabled()) {
      boxStrokeColor = disabledColor;
    } else if (indicatorViewController.errorShouldBeShown()) {
      if (strokeErrorColor != null) {
        updateStrokeErrorColor(hasFocus, isHovered);
      } else {
        boxStrokeColor = indicatorViewController.getErrorViewCurrentTextColor();
      }
    } else if (counterOverflowed && counterView != null) {
      if (strokeErrorColor != null) {
        updateStrokeErrorColor(hasFocus, isHovered);
      } else {
        boxStrokeColor = counterView.getCurrentTextColor();
      }
    } else if (hasFocus) {
      boxStrokeColor = focusedStrokeColor;
    } else if (isHovered) {
      boxStrokeColor = hoveredStrokeColor;
    } else {
      boxStrokeColor = defaultStrokeColor;
    }

    setErrorIconVisible(
        getErrorIconDrawable() != null
            && indicatorViewController.isErrorEnabled()
            && indicatorViewController.errorShouldBeShown());

    
    updateIconColorOnState(errorIconView, errorIconTintList);
    updateIconColorOnState(startIconView, startIconTintList);
    updateIconColorOnState(endIconView, endIconTintList);

    if (getEndIconDelegate().shouldTintIconOnError()) {
      tintEndIconOnError(indicatorViewController.errorShouldBeShown());
    }

    
    if (hasFocus && isEnabled()) {
      boxStrokeWidthPx = boxStrokeWidthFocusedPx;
    } else {
      boxStrokeWidthPx = boxStrokeWidthDefaultPx;
    }

    
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED) {
      if (!isEnabled()) {
        boxBackgroundColor = disabledFilledBackgroundColor;
      } else if (isHovered && !hasFocus) {
        boxBackgroundColor = hoveredFilledBackgroundColor;
      } else if (hasFocus) {
        boxBackgroundColor = focusedFilledBackgroundColor;
      } else {
        boxBackgroundColor = defaultFilledBackgroundColor;
      }
    }

    applyBoxAttributes();
  }

  private void updateStrokeErrorColor(boolean hasFocus, boolean isHovered) {
    int defaultStrokeErrorColor = strokeErrorColor.getDefaultColor();
    int hoveredStrokeErrorColor =
        strokeErrorColor.getColorForState(
            new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled},
            defaultStrokeErrorColor);
    int focusedStrokeErrorColor =
        strokeErrorColor.getColorForState(
            new int[] {android.R.attr.state_activated, android.R.attr.state_enabled},
            defaultStrokeErrorColor);
    if (hasFocus) {
      boxStrokeColor = focusedStrokeErrorColor;
    } else if (isHovered) {
      boxStrokeColor = hoveredStrokeErrorColor;
    } else {
      boxStrokeColor = defaultStrokeErrorColor;
    }
  }

  private void setErrorIconVisible(boolean errorIconVisible) {
    errorIconView.setVisibility(errorIconVisible ? VISIBLE : GONE);
    endIconFrame.setVisibility(errorIconVisible ? GONE : VISIBLE);
    updateSuffixTextViewPadding();
    if (!hasEndIcon()) {
      updateDummyDrawables();
    }
  }

  private boolean isErrorIconVisible() {
    return errorIconView.getVisibility() == VISIBLE;
  }

  private void updateIconColorOnState(
      CheckableImageButton iconView, ColorStateList colorStateList) {
    Drawable icon = iconView.getDrawable();
    if (iconView.getDrawable() == null || colorStateList == null || !colorStateList.isStateful()) {
      return;
    }

    int color =
        colorStateList.getColorForState(this.getDrawableState(), colorStateList.getDefaultColor());

    icon = DrawableCompat.wrap(icon).mutate();
    DrawableCompat.setTintList(icon, ColorStateList.valueOf(color));
    iconView.setImageDrawable(icon);
  }

  private void expandHint(boolean animate) {
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    if (animate && hintAnimationEnabled) {
      animateToExpansionFraction(0f);
    } else {
      collapsingTextHelper.setExpansionFraction(0f);
    }
    if (cutoutEnabled() && ((CutoutDrawable) boxBackground).hasCutout()) {
      closeCutout();
    }
    hintExpanded = true;
    hidePlaceholderText();

    updatePrefixTextVisibility();
    updateSuffixTextVisibility();
  }

  @VisibleForTesting
  void animateToExpansionFraction(final float target) {
    if (collapsingTextHelper.getExpansionFraction() == target) {
      return;
    }
    if (this.animator == null) {
      this.animator = new ValueAnimator();
      this.animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      this.animator.setDuration(LABEL_SCALE_ANIMATION_DURATION);
      this.animator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              collapsingTextHelper.setExpansionFraction((float) animator.getAnimatedValue());
            }
          });
    }
    this.animator.setFloatValues(collapsingTextHelper.getExpansionFraction(), target);
    this.animator.start();
  }

  @VisibleForTesting
  final boolean isHintExpanded() {
    return hintExpanded;
  }

  @VisibleForTesting
  final boolean isHelperTextDisplayed() {
    return indicatorViewController.helperTextIsDisplayed();
  }

  @VisibleForTesting
  final int getHintCurrentCollapsedTextColor() {
    return collapsingTextHelper.getCurrentCollapsedTextColor();
  }

  @VisibleForTesting
  final float getHintCollapsedTextHeight() {
    return collapsingTextHelper.getCollapsedTextHeight();
  }

  @VisibleForTesting
  final int getErrorTextCurrentColor() {
    return indicatorViewController.getErrorViewCurrentTextColor();
  }

  
  public static class AccessibilityDelegate extends AccessibilityDelegateCompat {
    private final TextInputLayout layout;

    public AccessibilityDelegate(@NonNull TextInputLayout layout) {
      this.layout = layout;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(
        @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
      super.onInitializeAccessibilityNodeInfo(host, info);
      EditText editText = layout.getEditText();
      CharSequence inputText = (editText != null) ? editText.getText() : null;
      CharSequence hintText = layout.getHint();
      CharSequence helperText = layout.getHelperText();
      CharSequence errorText = layout.getError();
      int maxCharLimit = layout.getCounterMaxLength();
      CharSequence counterOverflowDesc = layout.getCounterOverflowDescription();
      boolean showingText = !TextUtils.isEmpty(inputText);
      boolean hasHint = !TextUtils.isEmpty(hintText);
      boolean hasHelperText = !TextUtils.isEmpty(helperText);
      boolean showingError = !TextUtils.isEmpty(errorText);
      boolean contentInvalid = showingError || !TextUtils.isEmpty(counterOverflowDesc);

      String hint = hasHint ? hintText.toString() : "";
      hint += ((showingError || hasHelperText) && !TextUtils.isEmpty(hint)) ? ", " : "";
      hint += showingError ? errorText : (hasHelperText ? helperText : "");

      if (showingText) {
        info.setText(inputText);
      } else if (!TextUtils.isEmpty(hint)) {
        info.setText(hint);
      }

      if (!TextUtils.isEmpty(hint)) {
        if (VERSION.SDK_INT >= 26) {
          info.setHintText(hint);
        } else {
          
          
          String text = showingText ? (inputText + ", " + hint) : hint;
          info.setText(text);
        }
        info.setShowingHintText(!showingText);
      }

      
      info.setMaxTextLength(
          (inputText != null && inputText.length() == maxCharLimit) ? maxCharLimit : -1);

      if (contentInvalid) {
        info.setError(showingError ? errorText : counterOverflowDesc);
      }
    }
  }
}
