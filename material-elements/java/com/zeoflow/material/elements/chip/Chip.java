

package com.zeoflow.material.elements.chip;

import com.google.android.material.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.AnimatorRes;
import androidx.annotation.BoolRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.zeoflow.material.elements.animation.MotionSpec;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.resources.TextAppearance;
import com.zeoflow.material.elements.resources.TextAppearanceFontCallback;
import com.zeoflow.material.elements.ripple.RippleUtils;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class Chip extends AppCompatCheckBox implements ChipDrawable.Delegate, Shapeable
{

  private static final String TAG = "Chip";

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Chip_Action;

  private static final int CHIP_BODY_VIRTUAL_ID = 0;
  private static final int CLOSE_ICON_VIRTUAL_ID = 1;
  private static final Rect EMPTY_BOUNDS = new Rect();

  private static final int[] SELECTED_STATE = new int[] {android.R.attr.state_selected};
  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};

  private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";


  private static final int MIN_TOUCH_TARGET_DP = 48;

  @Nullable private ChipDrawable chipDrawable;
  @Nullable private InsetDrawable insetBackgroundDrawable;

  @Nullable private RippleDrawable ripple;

  @Nullable private OnClickListener onCloseIconClickListener;
  @Nullable private OnCheckedChangeListener onCheckedChangeListenerInternal;
  private boolean deferredCheckedValue;
  private boolean closeIconPressed;
  private boolean closeIconHovered;
  private boolean closeIconFocused;
  private boolean ensureMinTouchTargetSize;
  private int lastLayoutDirection;

  @Dimension(unit = Dimension.PX)
  private int minTouchTargetSize;

  private static final String BUTTON_ACCESSIBILITY_CLASS_NAME = "android.widget.Button";
  private static final String COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME =
      "android.widget.CompoundButton";
  private static final String GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME = "android.view.View";

  @NonNull private final ChipTouchHelper touchHelper;
  private final Rect rect = new Rect();
  private final RectF rectF = new RectF();
  private final TextAppearanceFontCallback fontCallback =
      new TextAppearanceFontCallback() {
        @Override
        public void onFontRetrieved(@NonNull Typeface typeface, boolean fontResolvedSynchronously) {

          setText(chipDrawable.shouldDrawText() ? chipDrawable.getText() : getText());
          requestLayout();
          invalidate();
        }

        @Override
        public void onFontRetrievalFailed(int reason) {}
      };

  public Chip(Context context) {
    this(context, null);
  }

  public Chip(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipStyle);
  }

  public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

    context = getContext();

    validateAttributes(attrs);
    ChipDrawable drawable =
        ChipDrawable.createFromAttributes(
            context, attrs, defStyleAttr, DEF_STYLE_RES);
    initMinTouchTarget(context, attrs, defStyleAttr);
    setChipDrawable(drawable);
    drawable.setElevation(ViewCompat.getElevation(this));
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.Chip,
            defStyleAttr,
            DEF_STYLE_RES);
    if (VERSION.SDK_INT < VERSION_CODES.M) {


      setTextColor(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_android_textColor));
    }
    boolean hasShapeAppearanceAttribute = a.hasValue(R.styleable.Chip_shapeAppearance);
    a.recycle();

    touchHelper = new ChipTouchHelper(this);
    updateAccessibilityDelegate();
    if (!hasShapeAppearanceAttribute) {
      initOutlineProvider();
    }

    setChecked(deferredCheckedValue);
    setText(drawable.getText());
    setEllipsize(drawable.getEllipsize());

    updateTextPaintDrawState();


    if (!chipDrawable.shouldDrawText()) {
      setLines(1);
      setHorizontallyScrolling(true);
    }


    setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

    updatePaddingInternal();
    if (shouldEnsureMinTouchTargetSize()) {
      setMinHeight(minTouchTargetSize);
    }
    lastLayoutDirection = ViewCompat.getLayoutDirection(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, chipDrawable);
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (chipDrawable != null) {
      chipDrawable.setElevation(elevation);
    }
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    if (isCheckable() || isClickable()) {
      info.setClassName(
          isCheckable()
              ? COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME
              : BUTTON_ACCESSIBILITY_CLASS_NAME);
    } else {
      info.setClassName(GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME);
    }
    info.setCheckable(isCheckable());
    info.setClickable(isClickable());

    if (getParent() instanceof ChipGroup) {
      ChipGroup chipGroup = ((ChipGroup) getParent());
      AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);

      int columnIndex = chipGroup.isSingleLine() ? chipGroup.getIndexOfChip(this) : -1;
      infoCompat.setCollectionItemInfo(
          CollectionItemInfoCompat.obtain(
               chipGroup.getRowIndex(this),
               1,
               columnIndex,
               1,
               false,
               isChecked()));
    }
  }



  private void updateAccessibilityDelegate() {
    if (hasCloseIcon() && isCloseIconVisible() && onCloseIconClickListener != null) {
      ViewCompat.setAccessibilityDelegate(this, touchHelper);
    } else {

      ViewCompat.setAccessibilityDelegate(this, null);
    }
  }

  private void initMinTouchTarget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.Chip,
            defStyleAttr,
            DEF_STYLE_RES);
    ensureMinTouchTargetSize = a.getBoolean(R.styleable.Chip_ensureMinTouchTargetSize, false);

    float defaultMinTouchTargetSize =
        (float) Math.ceil(ViewUtils.dpToPx(getContext(), MIN_TOUCH_TARGET_DP));
    minTouchTargetSize =
        (int)
            Math.ceil(
                a.getDimension(R.styleable.Chip_chipMinTouchTargetSize, defaultMinTouchTargetSize));

    a.recycle();
  }


  private void updatePaddingInternal() {
    if (TextUtils.isEmpty(getText()) || chipDrawable == null) {
      return;
    }
    int paddingEnd =
        (int)
            (chipDrawable.getChipEndPadding()
                + chipDrawable.getTextEndPadding()
                + chipDrawable.calculateCloseIconWidth());
    int paddingStart =
        (int)
            (chipDrawable.getChipStartPadding()
                + chipDrawable.getTextStartPadding()
                + chipDrawable.calculateChipIconWidth());
    if (insetBackgroundDrawable != null) {
      Rect padding = new Rect();
      insetBackgroundDrawable.getPadding(padding);
      paddingStart += padding.left;
      paddingEnd += padding.right;
    }

    ViewCompat.setPaddingRelative(
        this, paddingStart, getPaddingTop(), paddingEnd, getPaddingBottom());
  }

  @Override
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void onRtlPropertiesChanged(int layoutDirection) {
    super.onRtlPropertiesChanged(layoutDirection);



    if (lastLayoutDirection != layoutDirection) {
      lastLayoutDirection = layoutDirection;
      updatePaddingInternal();
    }
  }

  private void validateAttributes(@Nullable AttributeSet attributeSet) {
    if (attributeSet == null) {
      return;
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "background") != null) {
      Log.w(TAG, "Do not set the background; Chip manages its own background drawable.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableLeft") != null) {
      throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableStart") != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableEnd") != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    if (attributeSet.getAttributeValue(NAMESPACE_ANDROID, "drawableRight") != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    if (!attributeSet.getAttributeBooleanValue(NAMESPACE_ANDROID, "singleLine", true)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "lines", 1) != 1)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "minLines", 1) != 1)
        || (attributeSet.getAttributeIntValue(NAMESPACE_ANDROID, "maxLines", 1) != 1)) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    if (attributeSet.getAttributeIntValue(
            NAMESPACE_ANDROID, "gravity", (Gravity.CENTER_VERTICAL | Gravity.START))
        != (Gravity.CENTER_VERTICAL | Gravity.START)) {
      Log.w(TAG, "Chip text must be vertically center and start aligned");
    }
  }

  private void initOutlineProvider() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void getOutline(View view, @NonNull Outline outline) {
              if (chipDrawable != null) {
                chipDrawable.getOutline(outline);
              } else {
                outline.setAlpha(0.0f);
              }
            }
          });
    }
  }


  public Drawable getChipDrawable() {
    return chipDrawable;
  }


  public void setChipDrawable(@NonNull ChipDrawable drawable) {
    if (chipDrawable != drawable) {
      unapplyChipDrawable(chipDrawable);
      chipDrawable = drawable;


      chipDrawable.setShouldDrawText(false);
      applyChipDrawable(chipDrawable);
      ensureAccessibleTouchTarget(minTouchTargetSize);
    }
  }

  private void updateBackgroundDrawable() {
    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      updateFrameworkRippleBackground();
    } else {
      chipDrawable.setUseCompatRipple(true);
      ViewCompat.setBackground(this, getBackgroundDrawable());
      updatePaddingInternal();
      ensureChipDrawableHasCallback();
    }
  }

  private void ensureChipDrawableHasCallback() {
    if (getBackgroundDrawable() == insetBackgroundDrawable && chipDrawable.getCallback() == null) {


      chipDrawable.setCallback(insetBackgroundDrawable);
    }
  }

  @Nullable
  public Drawable getBackgroundDrawable() {
    if (insetBackgroundDrawable == null) {
      return chipDrawable;
    }
    return insetBackgroundDrawable;
  }

  private void updateFrameworkRippleBackground() {

    ripple =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(chipDrawable.getRippleColor()),
            getBackgroundDrawable(),
            null);
    chipDrawable.setUseCompatRipple(false);

    ViewCompat.setBackground(this, ripple);
    updatePaddingInternal();
  }

  private void unapplyChipDrawable(@Nullable ChipDrawable chipDrawable) {
    if (chipDrawable != null) {
      chipDrawable.setDelegate(null);
    }
  }

  private void applyChipDrawable(@NonNull ChipDrawable chipDrawable) {
    chipDrawable.setDelegate(this);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] state = super.onCreateDrawableState(extraSpace + 2);
    if (isChecked()) {
      mergeDrawableStates(state, SELECTED_STATE);
    }
    if (isCheckable()) {
      mergeDrawableStates(state, CHECKABLE_STATE_SET);
    }
    return state;
  }

  @Override
  public void setGravity(int gravity) {
    if (gravity != (Gravity.CENTER_VERTICAL | Gravity.START)) {
      Log.w(TAG, "Chip text must be vertically center and start aligned");
    } else {
      super.setGravity(gravity);
    }
  }

  public void setBackgroundTintList(@Nullable ColorStateList tint) {
    Log.w(TAG, "Do not set the background tint list; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundTintMode(@Nullable Mode tintMode) {
    Log.w(TAG, "Do not set the background tint mode; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundColor(int color) {
    Log.w(TAG, "Do not set the background color; Chip manages its own background drawable.");
  }

  @Override
  public void setBackgroundResource(int resid) {
    Log.w(TAG, "Do not set the background resource; Chip manages its own background drawable.");
  }

  @Override
  public void setBackground(Drawable background) {
    if (background != getBackgroundDrawable() && background != ripple) {
      Log.w(TAG, "Do not set the background; Chip manages its own background drawable.");
    } else {
      super.setBackground(background);
    }
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    if (background != getBackgroundDrawable() && background != ripple) {
      Log.w(TAG, "Do not set the background drawable; Chip manages its own background drawable.");
    } else {
      super.setBackgroundDrawable(background);
    }
  }

  @Override
  public void setCompoundDrawables(
      @Nullable Drawable left,
      @Nullable Drawable top,
      @Nullable Drawable right,
      @Nullable Drawable bottom) {
    if (left != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (right != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawables(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
    if (left != 0) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (right != 0) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesWithIntrinsicBounds(
      @Nullable Drawable left,
      @Nullable Drawable top,
      @Nullable Drawable right,
      @Nullable Drawable bottom) {
    if (left != null) {
      throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
    }
    if (right != null) {
      throw new UnsupportedOperationException("Please set right drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
  }

  @Override
  public void setCompoundDrawablesRelative(
      @Nullable Drawable start,
      @Nullable Drawable top,
      @Nullable Drawable end,
      @Nullable Drawable bottom) {
    if (start != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesRelative(start, top, end, bottom);
  }

  @Override
  public void setCompoundDrawablesRelativeWithIntrinsicBounds(
      int start, int top, int end, int bottom) {
    if (start != 0) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != 0) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }

    super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
  }

  @Override
  public void setCompoundDrawablesRelativeWithIntrinsicBounds(
      @Nullable Drawable start,
      @Nullable Drawable top,
      @Nullable Drawable end,
      @Nullable Drawable bottom) {
    if (start != null) {
      throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
    }
    if (end != null) {
      throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
    }
    super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
  }

  @Nullable
  @Override
  public TruncateAt getEllipsize() {
    return chipDrawable != null ? chipDrawable.getEllipsize() : null;
  }

  @Override
  public void setEllipsize(TruncateAt where) {
    if (chipDrawable == null) {
      return;
    }
    if (where == TruncateAt.MARQUEE) {
      throw new UnsupportedOperationException("Text within a chip are not allowed to scroll.");
    }
    super.setEllipsize(where);
    if (chipDrawable != null) {
      chipDrawable.setEllipsize(where);
    }
  }

  @Override
  public void setSingleLine(boolean singleLine) {
    if (!singleLine) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setSingleLine(singleLine);
  }

  @Override
  public void setLines(int lines) {
    if (lines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setLines(lines);
  }

  @Override
  public void setMinLines(int minLines) {
    if (minLines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setMinLines(minLines);
  }

  @Override
  public void setMaxLines(int maxLines) {
    if (maxLines > 1) {
      throw new UnsupportedOperationException("Chip does not support multi-line text");
    }
    super.setMaxLines(maxLines);
  }

  @Override
  public void setMaxWidth(@Px int maxWidth) {
    super.setMaxWidth(maxWidth);
    if (chipDrawable != null) {
      chipDrawable.setMaxWidth(maxWidth);
    }
  }

  @Override
  public void onChipDrawableSizeChange() {
    ensureAccessibleTouchTarget(minTouchTargetSize);
    requestLayout();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      invalidateOutline();
    }
  }

  @Override
  public void setChecked(boolean checked) {
    if (chipDrawable == null) {

      deferredCheckedValue = checked;
    } else if (chipDrawable.isCheckable()) {
      boolean wasChecked = isChecked();
      super.setChecked(checked);

      if (wasChecked != checked) {
        if (onCheckedChangeListenerInternal != null) {
          onCheckedChangeListenerInternal.onCheckedChanged(this, checked);
        }
      }
    }
  }


  void setOnCheckedChangeListenerInternal(OnCheckedChangeListener listener) {
    onCheckedChangeListenerInternal = listener;
  }


  public void setOnCloseIconClickListener(OnClickListener listener) {
    this.onCloseIconClickListener = listener;
    updateAccessibilityDelegate();
  }


  @CallSuper
  public boolean performCloseIconClick() {
    playSoundEffect(SoundEffectConstants.CLICK);

    boolean result;
    if (onCloseIconClickListener != null) {
      onCloseIconClickListener.onClick(this);
      result = true;
    } else {
      result = false;
    }

    touchHelper.sendEventForVirtualView(
        CLOSE_ICON_VIRTUAL_ID, AccessibilityEvent.TYPE_VIEW_CLICKED);
    return result;
  }

  @SuppressLint("ClickableViewAccessibility")

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    boolean handled = false;

    int action = event.getActionMasked();
    boolean eventInCloseIcon = getCloseIconTouchBounds().contains(event.getX(), event.getY());
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (eventInCloseIcon) {
          setCloseIconPressed(true);
          handled = true;
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (closeIconPressed) {
          if (!eventInCloseIcon) {
            setCloseIconPressed(false);
          }
          handled = true;
        }
        break;
      case MotionEvent.ACTION_UP:
        if (closeIconPressed) {
          performCloseIconClick();
          handled = true;
        }

      case MotionEvent.ACTION_CANCEL:
        setCloseIconPressed(false);
        break;
      default:
        break;
    }
    return handled || super.onTouchEvent(event);
  }

  @Override
  public boolean onHoverEvent(@NonNull MotionEvent event) {
    int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_HOVER_MOVE:
        setCloseIconHovered(getCloseIconTouchBounds().contains(event.getX(), event.getY()));
        break;
      case MotionEvent.ACTION_HOVER_EXIT:
        setCloseIconHovered(false);
        break;
      default:
        break;
    }
    return super.onHoverEvent(event);
  }





  @SuppressLint("PrivateApi")
  private boolean handleAccessibilityExit(@NonNull MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
      try {
        Field f = ExploreByTouchHelper.class.getDeclaredField("mHoveredVirtualViewId");
        f.setAccessible(true);
        int mHoveredVirtualViewId = (int) f.get(touchHelper);

        if (mHoveredVirtualViewId != ExploreByTouchHelper.INVALID_ID) {
          Method m =
              ExploreByTouchHelper.class.getDeclaredMethod("updateHoveredVirtualView", int.class);
          m.setAccessible(true);
          m.invoke(touchHelper, ExploreByTouchHelper.INVALID_ID);
          return true;
        }
      } catch (NoSuchMethodException e) {

        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (IllegalAccessException e) {

        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (InvocationTargetException e) {

        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      } catch (NoSuchFieldException e) {

        Log.e(TAG, "Unable to send Accessibility Exit event", e);
      }
    }
    return false;
  }

  @Override
  protected boolean dispatchHoverEvent(@NonNull MotionEvent event) {
    return handleAccessibilityExit(event)
        || touchHelper.dispatchHoverEvent(event)
        || super.dispatchHoverEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    boolean handled = touchHelper.dispatchKeyEvent(event);






    if (handled
        && touchHelper.getKeyboardFocusedVirtualViewId() != ExploreByTouchHelper.INVALID_ID) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect);
    touchHelper.onFocusChanged(focused, direction, previouslyFocusedRect);
  }

  @Override
  public void getFocusedRect(@NonNull Rect r) {
    if (touchHelper.getKeyboardFocusedVirtualViewId() == CLOSE_ICON_VIRTUAL_ID
        || touchHelper.getAccessibilityFocusedVirtualViewId() == CLOSE_ICON_VIRTUAL_ID) {
      r.set(getCloseIconTouchBoundsInt());
    } else {
      super.getFocusedRect(r);
    }
  }

  private void setCloseIconPressed(boolean pressed) {
    if (closeIconPressed != pressed) {
      closeIconPressed = pressed;
      refreshDrawableState();
    }
  }

  private void setCloseIconHovered(boolean hovered) {
    if (closeIconHovered != hovered) {
      closeIconHovered = hovered;
      refreshDrawableState();
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    boolean changed = false;

    if (chipDrawable != null && chipDrawable.isCloseIconStateful()) {
      changed = chipDrawable.setCloseIconState(createCloseIconDrawableState());
    }

    if (changed) {
      invalidate();
    }
  }

  @NonNull
  private int[] createCloseIconDrawableState() {
    int count = 0;
    if (isEnabled()) {
      count++;
    }
    if (closeIconFocused) {
      count++;
    }
    if (closeIconHovered) {
      count++;
    }
    if (closeIconPressed) {
      count++;
    }
    if (isChecked()) {
      count++;
    }

    int[] stateSet = new int[count];
    int i = 0;

    if (isEnabled()) {
      stateSet[i] = android.R.attr.state_enabled;
      i++;
    }
    if (closeIconFocused) {
      stateSet[i] = android.R.attr.state_focused;
      i++;
    }
    if (closeIconHovered) {
      stateSet[i] = android.R.attr.state_hovered;
      i++;
    }
    if (closeIconPressed) {
      stateSet[i] = android.R.attr.state_pressed;
      i++;
    }
    if (isChecked()) {
      stateSet[i] = android.R.attr.state_selected;
      i++;
    }
    return stateSet;
  }

  private boolean hasCloseIcon() {
    return chipDrawable != null && chipDrawable.getCloseIcon() != null;
  }

  @NonNull
  private RectF getCloseIconTouchBounds() {
    rectF.setEmpty();

    if (hasCloseIcon()) {

      chipDrawable.getCloseIconTouchBounds(rectF);
    }

    return rectF;
  }

  @NonNull
  private Rect getCloseIconTouchBoundsInt() {
    RectF bounds = getCloseIconTouchBounds();
    rect.set((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom);
    return rect;
  }

  @Nullable
  @Override
  @TargetApi(VERSION_CODES.N)
  public PointerIcon onResolvePointerIcon(@NonNull MotionEvent event, int pointerIndex) {
    if (getCloseIconTouchBounds().contains(event.getX(), event.getY()) && isEnabled()) {
      return PointerIcon.getSystemIcon(getContext(), PointerIcon.TYPE_HAND);
    }
    return null;
  }


  private class ChipTouchHelper extends ExploreByTouchHelper {

    ChipTouchHelper(Chip view) {
      super(view);
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
      return (hasCloseIcon() && getCloseIconTouchBounds().contains(x, y))
          ? CLOSE_ICON_VIRTUAL_ID
          : CHIP_BODY_VIRTUAL_ID;
    }

    @Override
    protected void getVisibleVirtualViews(@NonNull List<Integer> virtualViewIds) {
      virtualViewIds.add(CHIP_BODY_VIRTUAL_ID);
      if (hasCloseIcon() && isCloseIconVisible() && onCloseIconClickListener != null) {
        virtualViewIds.add(CLOSE_ICON_VIRTUAL_ID);
      }
    }

    @Override
    protected void onVirtualViewKeyboardFocusChanged(int virtualViewId, boolean hasFocus) {
      if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
        closeIconFocused = hasFocus;
        refreshDrawableState();
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
      if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
        CharSequence closeIconContentDescription = getCloseIconContentDescription();
        if (closeIconContentDescription != null) {
          node.setContentDescription(closeIconContentDescription);
        } else {
          CharSequence chipText = getText();
          node.setContentDescription(
              getContext()
                  .getString(
                      R.string.mtrl_chip_close_icon_content_description,
                      !TextUtils.isEmpty(chipText) ? chipText : "")
                  .trim());
        }
        node.setBoundsInParent(getCloseIconTouchBoundsInt());
        node.addAction(AccessibilityActionCompat.ACTION_CLICK);
        node.setEnabled(isEnabled());
      } else {
        node.setContentDescription("");
        node.setBoundsInParent(EMPTY_BOUNDS);
      }
    }

    @Override
    protected void onPopulateNodeForHost(@NonNull AccessibilityNodeInfoCompat node) {
      node.setCheckable(isCheckable());
      node.setClickable(isClickable());
      if (isCheckable() || isClickable()) {
        node.setClassName(
            isCheckable()
                ? COMPOUND_BUTTON_ACCESSIBILITY_CLASS_NAME
                : BUTTON_ACCESSIBILITY_CLASS_NAME);
      } else {
        node.setClassName(GENERIC_VIEW_ACCESSIBILITY_CLASS_NAME);
      }
      CharSequence chipText = getText();
      if (VERSION.SDK_INT >= VERSION_CODES.M) {
        node.setText(chipText);
      } else {


        node.setContentDescription(chipText);
      }
    }

    @Override
    protected boolean onPerformActionForVirtualView(
        int virtualViewId, int action, Bundle arguments) {
      if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
        if (virtualViewId == CHIP_BODY_VIRTUAL_ID) {
          return performClick();
        } else if (virtualViewId == CLOSE_ICON_VIRTUAL_ID) {
          return performCloseIconClick();
        }
      }
      return false;
    }
  }




  @Nullable
  public ColorStateList getChipBackgroundColor() {
    return chipDrawable != null ? chipDrawable.getChipBackgroundColor() : null;
  }


  public void setChipBackgroundColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColorResource(id);
    }
  }


  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipBackgroundColor(chipBackgroundColor);
    }
  }


  public float getChipMinHeight() {
    return chipDrawable != null ? chipDrawable.getChipMinHeight() : 0;
  }


  public void setChipMinHeightResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeightResource(id);
    }
  }


  public void setChipMinHeight(float minHeight) {
    if (chipDrawable != null) {
      chipDrawable.setChipMinHeight(minHeight);
    }
  }


  public float getChipCornerRadius() {
    return chipDrawable != null ? Math.max(0, chipDrawable.getChipCornerRadius()) : 0;
  }


  @Deprecated
  public void setChipCornerRadiusResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadiusResource(id);
    }
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    chipDrawable.setShapeAppearanceModel(shapeAppearanceModel);
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return chipDrawable.getShapeAppearanceModel();
  }


  @Deprecated
  public void setChipCornerRadius(float chipCornerRadius) {
    if (chipDrawable != null) {
      chipDrawable.setChipCornerRadius(chipCornerRadius);
    }
  }


  @Nullable
  public ColorStateList getChipStrokeColor() {
    return chipDrawable != null ? chipDrawable.getChipStrokeColor() : null;
  }


  public void setChipStrokeColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColorResource(id);
    }
  }


  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeColor(chipStrokeColor);
    }
  }


  public float getChipStrokeWidth() {
    return chipDrawable != null ? chipDrawable.getChipStrokeWidth() : 0;
  }


  public void setChipStrokeWidthResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidthResource(id);
    }
  }


  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (chipDrawable != null) {
      chipDrawable.setChipStrokeWidth(chipStrokeWidth);
    }
  }


  @Nullable
  public ColorStateList getRippleColor() {
    return chipDrawable != null ? chipDrawable.getRippleColor() : null;
  }


  public void setRippleColorResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColorResource(id);
      if (!chipDrawable.getUseCompatRipple()) {
        updateFrameworkRippleBackground();
      }
    }
  }


  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (chipDrawable != null) {
      chipDrawable.setRippleColor(rippleColor);
    }
    if (!chipDrawable.getUseCompatRipple()) {
      updateFrameworkRippleBackground();
    }
  }


  @Deprecated
  public CharSequence getChipText() {
    return getText();
  }

  @Override
  public void setLayoutDirection(int layoutDirection) {
    if (chipDrawable == null) {
      return;
    }
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      super.setLayoutDirection(layoutDirection);
    }
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (chipDrawable == null) {
      return;
    }
    if (text == null) {
      text = "";
    }
    super.setText(chipDrawable.shouldDrawText() ? null : text, type);
    if (chipDrawable != null) {
      chipDrawable.setText(text);
    }
  }


  @Deprecated
  public void setChipTextResource(@StringRes int id) {
    setText(getResources().getString(id));
  }


  @Deprecated
  public void setChipText(@Nullable CharSequence chipText) {
    setText(chipText);
  }


  public void setTextAppearanceResource(@StyleRes int id) {
    this.setTextAppearance(getContext(), id);
  }


  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (chipDrawable != null) {
      chipDrawable.setTextAppearance(textAppearance);
    }
    updateTextPaintDrawState();
  }

  @Override
  public void setTextAppearance(Context context, int resId) {
    super.setTextAppearance(context, resId);
    if (chipDrawable != null) {
      chipDrawable.setTextAppearanceResource(resId);
    }
    updateTextPaintDrawState();
  }

  @Override
  public void setTextAppearance(int resId) {
    super.setTextAppearance(resId);
    if (chipDrawable != null) {
      chipDrawable.setTextAppearanceResource(resId);
    }
    updateTextPaintDrawState();
  }

  private void updateTextPaintDrawState() {
    TextPaint textPaint = getPaint();
    if (chipDrawable != null) {
      textPaint.drawableState = chipDrawable.getState();
    }
    TextAppearance textAppearance = getTextAppearance();
    if (textAppearance != null) {
      textAppearance.updateDrawState(getContext(), textPaint, fontCallback);
    }
  }

  @Nullable
  private TextAppearance getTextAppearance() {
    return chipDrawable != null ? chipDrawable.getTextAppearance() : null;
  }


  public boolean isChipIconVisible() {
    return chipDrawable != null && chipDrawable.isChipIconVisible();
  }


  @Deprecated
  public boolean isChipIconEnabled() {
    return isChipIconVisible();
  }


  public void setChipIconVisible(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconVisible(id);
    }
  }


  public void setChipIconVisible(boolean chipIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconVisible(chipIconVisible);
    }
  }


  @Deprecated
  public void setChipIconEnabledResource(@BoolRes int id) {
    setChipIconVisible(id);
  }


  @Deprecated
  public void setChipIconEnabled(boolean chipIconEnabled) {
    setChipIconVisible(chipIconEnabled);
  }


  @Nullable
  public Drawable getChipIcon() {
    return chipDrawable != null ? chipDrawable.getChipIcon() : null;
  }


  public void setChipIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconResource(id);
    }
  }


  public void setChipIcon(@Nullable Drawable chipIcon) {
    if (chipDrawable != null) {
      chipDrawable.setChipIcon(chipIcon);
    }
  }


  @Nullable
  public ColorStateList getChipIconTint() {
    return chipDrawable != null ? chipDrawable.getChipIconTint() : null;
  }


  public void setChipIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconTintResource(id);
    }
  }


  public void setChipIconTint(@Nullable ColorStateList chipIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconTint(chipIconTint);
    }
  }


  public float getChipIconSize() {
    return chipDrawable != null ? chipDrawable.getChipIconSize() : 0;
  }


  public void setChipIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSizeResource(id);
    }
  }


  public void setChipIconSize(float chipIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setChipIconSize(chipIconSize);
    }
  }


  public boolean isCloseIconVisible() {
    return chipDrawable != null && chipDrawable.isCloseIconVisible();
  }


  @Deprecated
  public boolean isCloseIconEnabled() {
    return isCloseIconVisible();
  }


  public void setCloseIconVisible(@BoolRes int id) {
    setCloseIconVisible(getResources().getBoolean(id));
  }


  public void setCloseIconVisible(boolean closeIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconVisible(closeIconVisible);
    }
    updateAccessibilityDelegate();
  }


  @Deprecated
  public void setCloseIconEnabledResource(@BoolRes int id) {
    setCloseIconVisible(id);
  }


  @Deprecated
  public void setCloseIconEnabled(boolean closeIconEnabled) {
    setCloseIconVisible(closeIconEnabled);
  }


  @Nullable
  public Drawable getCloseIcon() {
    return chipDrawable != null ? chipDrawable.getCloseIcon() : null;
  }


  public void setCloseIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconResource(id);
    }
    updateAccessibilityDelegate();
  }


  public void setCloseIcon(@Nullable Drawable closeIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIcon(closeIcon);
    }
    updateAccessibilityDelegate();
  }


  @Nullable
  public ColorStateList getCloseIconTint() {
    return chipDrawable != null ? chipDrawable.getCloseIconTint() : null;
  }


  public void setCloseIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTintResource(id);
    }
  }


  public void setCloseIconTint(@Nullable ColorStateList closeIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconTint(closeIconTint);
    }
  }


  public float getCloseIconSize() {
    return chipDrawable != null ? chipDrawable.getCloseIconSize() : 0;
  }


  public void setCloseIconSizeResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSizeResource(id);
    }
  }


  public void setCloseIconSize(float closeIconSize) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconSize(closeIconSize);
    }
  }


  public void setCloseIconContentDescription(@Nullable CharSequence closeIconContentDescription) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconContentDescription(closeIconContentDescription);
    }
  }


  @Nullable
  public CharSequence getCloseIconContentDescription() {
    return chipDrawable != null ? chipDrawable.getCloseIconContentDescription() : null;
  }


  public boolean isCheckable() {
    return chipDrawable != null && chipDrawable.isCheckable();
  }


  public void setCheckableResource(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckableResource(id);
    }
  }


  public void setCheckable(boolean checkable) {
    if (chipDrawable != null) {
      chipDrawable.setCheckable(checkable);
    }
  }


  public boolean isCheckedIconVisible() {
    return chipDrawable != null && chipDrawable.isCheckedIconVisible();
  }


  @Deprecated
  public boolean isCheckedIconEnabled() {
    return isCheckedIconVisible();
  }


  public void setCheckedIconVisible(@BoolRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconVisible(id);
    }
  }


  public void setCheckedIconVisible(boolean checkedIconVisible) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconVisible(checkedIconVisible);
    }
  }


  @Deprecated
  public void setCheckedIconEnabledResource(@BoolRes int id) {
    setCheckedIconVisible(id);
  }


  @Deprecated
  public void setCheckedIconEnabled(boolean checkedIconEnabled) {
    setCheckedIconVisible(checkedIconEnabled);
  }


  @Nullable
  public Drawable getCheckedIcon() {
    return chipDrawable != null ? chipDrawable.getCheckedIcon() : null;
  }


  public void setCheckedIconResource(@DrawableRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconResource(id);
    }
  }


  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIcon(checkedIcon);
    }
  }


  @Nullable
  public ColorStateList getCheckedIconTint() {
    return chipDrawable != null ? chipDrawable.getCheckedIconTint() : null;
  }


  public void setCheckedIconTintResource(@ColorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconTintResource(id);
    }
  }


  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    if (chipDrawable != null) {
      chipDrawable.setCheckedIconTint(checkedIconTint);
    }
  }


  @Nullable
  public MotionSpec getShowMotionSpec() {
    return chipDrawable != null ? chipDrawable.getShowMotionSpec() : null;
  }


  public void setShowMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpecResource(id);
    }
  }


  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setShowMotionSpec(showMotionSpec);
    }
  }


  @Nullable
  public MotionSpec getHideMotionSpec() {
    return chipDrawable != null ? chipDrawable.getHideMotionSpec() : null;
  }


  public void setHideMotionSpecResource(@AnimatorRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpecResource(id);
    }
  }


  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
    if (chipDrawable != null) {
      chipDrawable.setHideMotionSpec(hideMotionSpec);
    }
  }


  public float getChipStartPadding() {
    return chipDrawable != null ? chipDrawable.getChipStartPadding() : 0;
  }


  public void setChipStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPaddingResource(id);
    }
  }


  public void setChipStartPadding(float chipStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipStartPadding(chipStartPadding);
    }
  }


  public float getIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getIconStartPadding() : 0;
  }


  public void setIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPaddingResource(id);
    }
  }


  public void setIconStartPadding(float iconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconStartPadding(iconStartPadding);
    }
  }


  public float getIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getIconEndPadding() : 0;
  }


  public void setIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPaddingResource(id);
    }
  }


  public void setIconEndPadding(float iconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setIconEndPadding(iconEndPadding);
    }
  }


  public float getTextStartPadding() {
    return chipDrawable != null ? chipDrawable.getTextStartPadding() : 0;
  }


  public void setTextStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPaddingResource(id);
    }
  }


  public void setTextStartPadding(float textStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextStartPadding(textStartPadding);
    }
  }


  public float getTextEndPadding() {
    return chipDrawable != null ? chipDrawable.getTextEndPadding() : 0;
  }


  public void setTextEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPaddingResource(id);
    }
  }


  public void setTextEndPadding(float textEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setTextEndPadding(textEndPadding);
    }
  }


  public float getCloseIconStartPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconStartPadding() : 0;
  }


  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPaddingResource(id);
    }
  }


  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconStartPadding(closeIconStartPadding);
    }
  }


  public float getCloseIconEndPadding() {
    return chipDrawable != null ? chipDrawable.getCloseIconEndPadding() : 0;
  }


  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPaddingResource(id);
    }
  }


  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setCloseIconEndPadding(closeIconEndPadding);
    }
  }


  public float getChipEndPadding() {
    return chipDrawable != null ? chipDrawable.getChipEndPadding() : 0;
  }


  public void setChipEndPaddingResource(@DimenRes int id) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPaddingResource(id);
    }
  }


  public void setChipEndPadding(float chipEndPadding) {
    if (chipDrawable != null) {
      chipDrawable.setChipEndPadding(chipEndPadding);
    }
  }


  public boolean shouldEnsureMinTouchTargetSize() {
    return ensureMinTouchTargetSize;
  }


  public void setEnsureMinTouchTargetSize(boolean flag) {
    ensureMinTouchTargetSize = flag;
    ensureAccessibleTouchTarget(minTouchTargetSize);
  }


  public boolean ensureAccessibleTouchTarget(@Dimension int minTargetPx) {
    minTouchTargetSize = minTargetPx;
    if (!shouldEnsureMinTouchTargetSize()) {
      if (insetBackgroundDrawable != null) {
        removeBackgroundInset();
      } else {
        updateBackgroundDrawable();
      }
      return false;
    }

    int deltaHeight = Math.max(0, minTargetPx - chipDrawable.getIntrinsicHeight());
    int deltaWidth = Math.max(0, minTargetPx - chipDrawable.getIntrinsicWidth());

    if (deltaWidth <= 0 && deltaHeight <= 0) {
      if (insetBackgroundDrawable != null) {
        removeBackgroundInset();
      } else {
        updateBackgroundDrawable();
      }
      return false;
    }

    int deltaX = deltaWidth > 0 ? deltaWidth / 2 : 0;
    int deltaY = deltaHeight > 0 ? deltaHeight / 2 : 0;

    if (insetBackgroundDrawable != null) {
      Rect padding = new Rect();
      insetBackgroundDrawable.getPadding(padding);
      if (padding.top == deltaY
          && padding.bottom == deltaY
          && padding.left == deltaX
          && padding.right == deltaX) {
        updateBackgroundDrawable();
        return true;
      }
    }
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
      if (getMinHeight() != minTargetPx) {
        setMinHeight(minTargetPx);
      }
      if (getMinWidth() != minTargetPx) {
        setMinWidth(minTargetPx);
      }
    } else {
      setMinHeight(minTargetPx);
      setMinWidth(minTargetPx);
    }
    insetChipBackgroundDrawable(deltaX, deltaY, deltaX, deltaY);
    updateBackgroundDrawable();
    return true;
  }

  private void removeBackgroundInset() {
    if (insetBackgroundDrawable != null) {
      insetBackgroundDrawable = null;
      setMinWidth(0);
      setMinHeight((int) getChipMinHeight());
      updateBackgroundDrawable();
    }
  }

  private void insetChipBackgroundDrawable(
      int insetLeft, int insetTop, int insetRight, int insetBottom) {
    insetBackgroundDrawable =
        new InsetDrawable(chipDrawable, insetLeft, insetTop, insetRight, insetBottom);
  }
}
