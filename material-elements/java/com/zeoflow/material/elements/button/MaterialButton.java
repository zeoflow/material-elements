

package com.zeoflow.material.elements.button;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
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
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;


public class MaterialButton extends AppCompatButton implements Checkable, Shapeable {

  
  public interface OnCheckedChangeListener {
    
    void onCheckedChanged(MaterialButton button, boolean isChecked);
  }

  
  interface OnPressedChangeListener {
    void onPressedChanged(MaterialButton button, boolean isPressed);
  }

  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

  
  public static final int ICON_GRAVITY_START = 0x1;

  
  public static final int ICON_GRAVITY_TEXT_START = 0x2;

  
  public static final int ICON_GRAVITY_END = 0x3;

  
  public static final int ICON_GRAVITY_TEXT_END = 0x4;

  
  @IntDef({ICON_GRAVITY_START, ICON_GRAVITY_TEXT_START, ICON_GRAVITY_END, ICON_GRAVITY_TEXT_END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IconGravity {}

  private static final String LOG_TAG = "MaterialButton";

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Button;

  @NonNull private final MaterialButtonHelper materialButtonHelper;
  @NonNull private final LinkedHashSet<OnCheckedChangeListener> onCheckedChangeListeners =
      new LinkedHashSet<>();

  @Nullable private OnPressedChangeListener onPressedChangeListenerInternal;
  @Nullable private Mode iconTintMode;
  @Nullable private ColorStateList iconTint;
  @Nullable private Drawable icon;

  @Px private int iconSize;
  @Px private int iconLeft;
  @Px private int iconPadding;

  private boolean checked = false;
  private boolean broadcasting = false;
  @IconGravity private int iconGravity;

  public MaterialButton(@NonNull Context context) {
    this(context, null );
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialButton, defStyleAttr, DEF_STYLE_RES);

    iconPadding = attributes.getDimensionPixelSize(R.styleable.MaterialButton_iconPadding, 0);
    iconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialButton_iconTintMode, -1), Mode.SRC_IN);

    iconTint =
        MaterialResources.getColorStateList(
            getContext(), attributes, R.styleable.MaterialButton_iconTint);
    icon = MaterialResources.getDrawable(getContext(), attributes, R.styleable.MaterialButton_icon);
    iconGravity = attributes.getInteger(R.styleable.MaterialButton_iconGravity, ICON_GRAVITY_START);

    iconSize = attributes.getDimensionPixelSize(R.styleable.MaterialButton_iconSize, 0);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();

    
    materialButtonHelper = new MaterialButtonHelper(this, shapeAppearanceModel);
    materialButtonHelper.loadFromAttributes(attributes);

    attributes.recycle();

    setCompoundDrawablePadding(iconPadding);
    updateIcon(icon != null);
  }

  @NonNull
  private String getA11yClassName() {
    
    return (isCheckable() ? CompoundButton.class : Button.class).getName();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(getA11yClassName());
    info.setCheckable(isCheckable());
    info.setChecked(isChecked());
    info.setClickable(isClickable());
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent accessibilityEvent) {
    super.onInitializeAccessibilityEvent(accessibilityEvent);
    accessibilityEvent.setClassName(getA11yClassName());
    accessibilityEvent.setChecked(isChecked());
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.checked = checked;
    return savedState;
  }

  @Override
  public void onRestoreInstanceState(@Nullable Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    setChecked(savedState.checked);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintList(tint);
    } else {
      
      
      super.setSupportBackgroundTintList(tint);
    }
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  @Nullable
  public ColorStateList getSupportBackgroundTintList() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getSupportBackgroundTintList();
    } else {
      
      
      
      return super.getSupportBackgroundTintList();
    }
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintMode(tintMode);
    } else {
      
      
      super.setSupportBackgroundTintMode(tintMode);
    }
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  @Nullable
  public PorterDuff.Mode getSupportBackgroundTintMode() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getSupportBackgroundTintMode();
    } else {
      
      
      return super.getSupportBackgroundTintMode();
    }
  }

  @Override
  public void setBackgroundTintList(@Nullable ColorStateList tintList) {
    setSupportBackgroundTintList(tintList);
  }

  @Nullable
  @Override
  public ColorStateList getBackgroundTintList() {
    return getSupportBackgroundTintList();
  }

  @Override
  public void setBackgroundTintMode(@Nullable Mode tintMode) {
    setSupportBackgroundTintMode(tintMode);
  }

  @Nullable
  @Override
  public Mode getBackgroundTintMode() {
    return getSupportBackgroundTintMode();
  }

  @Override
  public void setBackgroundColor(@ColorInt int color) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setBackgroundColor(color);
    } else {
      
      
      super.setBackgroundColor(color);
    }
  }

  @Override
  public void setBackground(@NonNull Drawable background) {
    setBackgroundDrawable(background);
  }

  @Override
  public void setBackgroundResource(@DrawableRes int backgroundResourceId) {
    Drawable background = null;
    if (backgroundResourceId != 0) {
      background = AppCompatResources.getDrawable(getContext(), backgroundResourceId);
    }
    setBackgroundDrawable(background);
  }

  @Override
  public void setBackgroundDrawable(@NonNull Drawable background) {
    if (isUsingOriginalBackground()) {
      if (background != this.getBackground()) {
        Log.w(
            LOG_TAG,
            "Do not set the background; MaterialButton manages its own background drawable.");
        materialButtonHelper.setBackgroundOverwritten();
        super.setBackgroundDrawable(background);
      } else {
        
        
        
        getBackground().setState(background.getState());
      }
    } else {
      super.setBackgroundDrawable(background);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    
    if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP && materialButtonHelper != null) {
      materialButtonHelper.updateMaskBounds(bottom - top, right - left);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateIconPosition();
  }

  @Override
  protected void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    super.onTextChanged(charSequence, i, i1, i2);
    updateIconPosition();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isUsingOriginalBackground()) {
      MaterialShapeUtils.setParentAbsoluteElevation(
          this, materialButtonHelper.getMaterialShapeDrawable());
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (isUsingOriginalBackground()) {
      materialButtonHelper.getMaterialShapeDrawable().setElevation(elevation);
    }
  }

  private void updateIconPosition() {
    if (icon == null || getLayout() == null) {
      return;
    }

    if (iconGravity == ICON_GRAVITY_START || iconGravity == ICON_GRAVITY_END) {
      iconLeft = 0;
      updateIcon( false);
      return;
    }

    Paint textPaint = getPaint();
    String buttonText = getText().toString();
    if (getTransformationMethod() != null) {
      
      
      buttonText = getTransformationMethod().getTransformation(buttonText, this).toString();
    }

    int textWidth =
        Math.min((int) textPaint.measureText(buttonText), getLayout().getEllipsizedWidth());

    int localIconSize = iconSize == 0 ? icon.getIntrinsicWidth() : iconSize;
    int newIconLeft =
        (getMeasuredWidth()
                - textWidth
                - ViewCompat.getPaddingEnd(this)
                - localIconSize
                - iconPadding
                - ViewCompat.getPaddingStart(this))
            / 2;

    
    if (isLayoutRTL() != (iconGravity == ICON_GRAVITY_TEXT_END)) {
      newIconLeft = -newIconLeft;
    }

    if (iconLeft != newIconLeft) {
      iconLeft = newIconLeft;
      updateIcon( false);
    }
  }

  private boolean isLayoutRTL() {
    return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  
  void setInternalBackground(Drawable background) {
    super.setBackgroundDrawable(background);
  }

  
  public void setIconPadding(@Px int iconPadding) {
    if (this.iconPadding != iconPadding) {
      this.iconPadding = iconPadding;
      setCompoundDrawablePadding(iconPadding);
    }
  }

  
  @Px
  public int getIconPadding() {
    return iconPadding;
  }

  
  public void setIconSize(@Px int iconSize) {
    if (iconSize < 0) {
      throw new IllegalArgumentException("iconSize cannot be less than 0");
    }

    if (this.iconSize != iconSize) {
      this.iconSize = iconSize;
      updateIcon( true);
    }
  }

  
  @Px
  public int getIconSize() {
    return iconSize;
  }

  
  public void setIcon(@Nullable Drawable icon) {
    if (this.icon != icon) {
      this.icon = icon;
      updateIcon( true);
    }
  }
  
  public void setIconResource(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setIcon(icon);
  }

  
  public Drawable getIcon() {
    return icon;
  }

  
  public void setIconTint(@Nullable ColorStateList iconTint) {
    if (this.iconTint != iconTint) {
      this.iconTint = iconTint;
      updateIcon( false);
    }
  }

  
  public void setIconTintResource(@ColorRes int iconTintResourceId) {
    setIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  
  public ColorStateList getIconTint() {
    return iconTint;
  }

  
  public void setIconTintMode(Mode iconTintMode) {
    if (this.iconTintMode != iconTintMode) {
      this.iconTintMode = iconTintMode;
      updateIcon( false);
    }
  }

  
  public Mode getIconTintMode() {
    return iconTintMode;
  }

  
  private void updateIcon(boolean needsIconUpdate) {
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintList(icon, iconTint);
      if (iconTintMode != null) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }

      int width = iconSize != 0 ? iconSize : icon.getIntrinsicWidth();
      int height = iconSize != 0 ? iconSize : icon.getIntrinsicHeight();
      icon.setBounds(iconLeft, 0, iconLeft + width, height);
    }

    
    boolean isIconStart =
        iconGravity == ICON_GRAVITY_START || iconGravity == ICON_GRAVITY_TEXT_START;
    
    if (needsIconUpdate) {
      resetIconDrawable(isIconStart);
      return;
    }

    
    Drawable[] existingDrawables  = TextViewCompat.getCompoundDrawablesRelative(this);
    Drawable drawableStart = existingDrawables[0];
    Drawable drawableEnd = existingDrawables[2];
    boolean hasIconChanged =
        (isIconStart && drawableStart != icon) || (!isIconStart && drawableEnd != icon);

    if (hasIconChanged) {
      resetIconDrawable(isIconStart);
    }
  }

  private void resetIconDrawable(boolean isIconStart) {
    if (isIconStart) {
      TextViewCompat.setCompoundDrawablesRelative(this, icon, null, null, null);
    } else {
      TextViewCompat.setCompoundDrawablesRelative(this, null, null, icon, null);
    }
  }

  
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setRippleColor(rippleColor);
    }
  }

  
  public void setRippleColorResource(@ColorRes int rippleColorResourceId) {
    if (isUsingOriginalBackground()) {
      setRippleColor(AppCompatResources.getColorStateList(getContext(), rippleColorResourceId));
    }
  }

  
  @Nullable
  public ColorStateList getRippleColor() {
    return isUsingOriginalBackground() ? materialButtonHelper.getRippleColor() : null;
  }

  
  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setStrokeColor(strokeColor);
    }
  }

  
  public void setStrokeColorResource(@ColorRes int strokeColorResourceId) {
    if (isUsingOriginalBackground()) {
      setStrokeColor(AppCompatResources.getColorStateList(getContext(), strokeColorResourceId));
    }
  }

  
  public ColorStateList getStrokeColor() {
    return isUsingOriginalBackground() ? materialButtonHelper.getStrokeColor() : null;
  }

  
  public void setStrokeWidth(@Px int strokeWidth) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setStrokeWidth(strokeWidth);
    }
  }

  
  public void setStrokeWidthResource(@DimenRes int strokeWidthResourceId) {
    if (isUsingOriginalBackground()) {
      setStrokeWidth(getResources().getDimensionPixelSize(strokeWidthResourceId));
    }
  }

  
  @Px
  public int getStrokeWidth() {
    return isUsingOriginalBackground() ? materialButtonHelper.getStrokeWidth() : 0;
  }

  
  public void setCornerRadius(@Px int cornerRadius) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setCornerRadius(cornerRadius);
    }
  }

  
  public void setCornerRadiusResource(@DimenRes int cornerRadiusResourceId) {
    if (isUsingOriginalBackground()) {
      setCornerRadius(getResources().getDimensionPixelSize(cornerRadiusResourceId));
    }
  }

  
  @Px
  public int getCornerRadius() {
    return isUsingOriginalBackground() ? materialButtonHelper.getCornerRadius() : 0;
  }

  
  @IconGravity
  public int getIconGravity() {
    return iconGravity;
  }

  
  public void setIconGravity(@IconGravity int iconGravity) {
    if (this.iconGravity != iconGravity) {
      this.iconGravity = iconGravity;
      updateIconPosition();
    }
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

    if (isCheckable()) {
      mergeDrawableStates(drawableState, CHECKABLE_STATE_SET);
    }

    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }

    return drawableState;
  }

  
  public void addOnCheckedChangeListener(@NonNull OnCheckedChangeListener listener) {
    onCheckedChangeListeners.add(listener);
  }

  
  public void removeOnCheckedChangeListener(@NonNull OnCheckedChangeListener listener) {
    onCheckedChangeListeners.remove(listener);
  }

  
  public void clearOnCheckedChangeListeners() {
    onCheckedChangeListeners.clear();
  }

  @Override
  public void setChecked(boolean checked) {
    if (isCheckable() && isEnabled() && this.checked != checked) {
      this.checked = checked;
      refreshDrawableState();

      
      if (broadcasting) {
        return;
      }

      broadcasting = true;
      for (OnCheckedChangeListener listener : onCheckedChangeListeners) {
        listener.onCheckedChanged(this, this.checked);
      }
      broadcasting = false;
    }
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void toggle() {
    setChecked(!checked);
  }

  @Override
  public boolean performClick() {
    toggle();

    return super.performClick();
  }

  
  public boolean isCheckable() {
    return materialButtonHelper != null && materialButtonHelper.isCheckable();
  }

  
  public void setCheckable(boolean checkable) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setCheckable(checkable);
    }
  }

  
  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShapeAppearanceModel(shapeAppearanceModel);
    } else {
      throw new IllegalStateException(
          "Attempted to set ShapeAppearanceModel on a MaterialButton which has an overwritten"
              + " background.");
    }
  }

  
  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getShapeAppearanceModel();
    } else {
      throw new IllegalStateException(
          "Attempted to get ShapeAppearanceModel from a MaterialButton which has an overwritten"
              + " background.");
    }
  }

  
  void setOnPressedChangeListenerInternal(@Nullable OnPressedChangeListener listener) {
    onPressedChangeListenerInternal = listener;
  }

  @Override
  public void setPressed(boolean pressed) {
    if (onPressedChangeListenerInternal != null) {
      onPressedChangeListenerInternal.onPressedChanged(this, pressed);
    }
    super.setPressed(pressed);
  }

  private boolean isUsingOriginalBackground() {
    return materialButtonHelper != null && !materialButtonHelper.isBackgroundOverwritten();
  }

  void setShouldDrawSurfaceColorStroke(boolean shouldDrawSurfaceColorStroke) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShouldDrawSurfaceColorStroke(shouldDrawSurfaceColorStroke);
    }
  }

  static class SavedState extends AbsSavedState {

    boolean checked;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      if (loader == null) {
        loader = getClass().getClassLoader();
      }
      readFromParcel(source);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(checked ? 1 : 0);
    }

    private void readFromParcel(@NonNull Parcel in) {
      checked = in.readInt() == 1;
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @NonNull
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
}
