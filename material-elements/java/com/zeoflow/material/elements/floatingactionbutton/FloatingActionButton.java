

package com.zeoflow.material.elements.floatingactionbutton;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.util.Preconditions.checkNotNull;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.AnimatorRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.TintableBackgroundView;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TintableImageSourceView;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.AppCompatImageHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.zeoflow.material.elements.animation.MotionSpec;
import com.zeoflow.material.elements.animation.TransformationCallback;
import com.zeoflow.material.elements.appbar.AppBarLayout;
import com.zeoflow.material.elements.bottomsheet.BottomSheetBehavior;
import com.zeoflow.material.elements.expandable.ExpandableTransformationWidget;
import com.zeoflow.material.elements.expandable.ExpandableWidgetHelper;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButtonImpl.InternalTransformationCallback;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButtonImpl.InternalVisibilityChangedListener;
import com.zeoflow.material.elements.internal.DescendantOffsetUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.internal.VisibilityAwareImageButton;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shadow.ShadowViewDelegate;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;
import com.zeoflow.material.elements.snackbar.Snackbar;
import com.zeoflow.material.elements.stateful.ExtendableSavedState;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;


public class FloatingActionButton extends VisibilityAwareImageButton
    implements TintableBackgroundView,
        TintableImageSourceView,
        ExpandableTransformationWidget,
        Shapeable,
        CoordinatorLayout.AttachedBehavior {

  private static final String LOG_TAG = "FloatingActionButton";
  private static final String EXPANDABLE_WIDGET_HELPER_KEY = "expandableWidgetHelper";
  private static final int DEF_STYLE_RES = R.style.Widget_Design_FloatingActionButton;

  
  public abstract static class OnVisibilityChangedListener {
    
    public void onShown(FloatingActionButton fab) {}

    
    public void onHidden(FloatingActionButton fab) {}
  }

  

  
  public static final int SIZE_MINI = 1;

  
  public static final int SIZE_NORMAL = 0;

  
  public static final int SIZE_AUTO = -1;

  
  public static final int NO_CUSTOM_SIZE = 0;

  
  private static final int AUTO_MINI_LARGEST_SCREEN_WIDTH = 470;

  
  @RestrictTo(LIBRARY_GROUP)
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({SIZE_MINI, SIZE_NORMAL, SIZE_AUTO})
  public @interface Size {}

  @Nullable private ColorStateList backgroundTint;
  @Nullable private PorterDuff.Mode backgroundTintMode;
  @Nullable private ColorStateList imageTint;
  @Nullable private PorterDuff.Mode imageMode;
  @Nullable private ColorStateList rippleColor;

  private int borderWidth;
  private int size;
  private int customSize;
  private int imagePadding;
  private int maxImageSize;

  boolean compatPadding;
  final Rect shadowPadding = new Rect();
  private final Rect touchArea = new Rect();

  @NonNull private final AppCompatImageHelper imageHelper;
  @NonNull private final ExpandableWidgetHelper expandableWidgetHelper;

  private FloatingActionButtonImpl impl;

  public FloatingActionButton(@NonNull Context context) {
    this(context, null);
  }

  public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.floatingActionButtonStyle);
  }

  @SuppressWarnings("nullness")
  public FloatingActionButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.FloatingActionButton, defStyleAttr, DEF_STYLE_RES);

    backgroundTint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.FloatingActionButton_backgroundTint);
    backgroundTintMode =
        ViewUtils.parseTintMode(
            a.getInt(R.styleable.FloatingActionButton_backgroundTintMode, -1), null);
    rippleColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.FloatingActionButton_rippleColor);
    size = a.getInt(R.styleable.FloatingActionButton_fabSize, SIZE_AUTO);
    customSize =
        a.getDimensionPixelSize(R.styleable.FloatingActionButton_fabCustomSize, NO_CUSTOM_SIZE);
    borderWidth = a.getDimensionPixelSize(R.styleable.FloatingActionButton_borderWidth, 0);
    final float elevation = a.getDimension(R.styleable.FloatingActionButton_elevation, 0f);
    final float hoveredFocusedTranslationZ =
        a.getDimension(R.styleable.FloatingActionButton_hoveredFocusedTranslationZ, 0f);
    final float pressedTranslationZ =
        a.getDimension(R.styleable.FloatingActionButton_pressedTranslationZ, 0f);
    compatPadding = a.getBoolean(R.styleable.FloatingActionButton_useCompatPadding, false);
    int minTouchTargetSize =
        getResources().getDimensionPixelSize(R.dimen.mtrl_fab_min_touch_target);
    maxImageSize = a.getDimensionPixelSize(R.styleable.FloatingActionButton_maxImageSize, 0);

    MotionSpec showMotionSpec =
        MotionSpec.createFromAttribute(context, a, R.styleable.FloatingActionButton_showMotionSpec);
    MotionSpec hideMotionSpec =
        MotionSpec.createFromAttribute(context, a, R.styleable.FloatingActionButton_hideMotionSpec);
    ShapeAppearanceModel shapeAppearance =
        ShapeAppearanceModel.builder(
                context, attrs, defStyleAttr, DEF_STYLE_RES, ShapeAppearanceModel.PILL)
            .build();

    boolean ensureMinTouchTargetSize =
        a.getBoolean(R.styleable.FloatingActionButton_ensureMinTouchTargetSize, false);

    setEnabled(a.getBoolean(R.styleable.FloatingActionButton_android_enabled, true));

    a.recycle();

    imageHelper = new AppCompatImageHelper(this);
    imageHelper.loadFromAttributes(attrs, defStyleAttr);

    expandableWidgetHelper = new ExpandableWidgetHelper(this);

    getImpl().setShapeAppearance(shapeAppearance);
    getImpl()
        .initializeBackgroundDrawable(backgroundTint, backgroundTintMode, rippleColor, borderWidth);
    getImpl().setMinTouchTargetSize(minTouchTargetSize);
    getImpl().setElevation(elevation);
    getImpl().setHoveredFocusedTranslationZ(hoveredFocusedTranslationZ);
    getImpl().setPressedTranslationZ(pressedTranslationZ);
    getImpl().setMaxImageSize(maxImageSize);
    getImpl().setShowMotionSpec(showMotionSpec);
    getImpl().setHideMotionSpec(hideMotionSpec);
    getImpl().setEnsureMinTouchTargetSize(ensureMinTouchTargetSize);

    setScaleType(ScaleType.MATRIX);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int preferredSize = getSizeDimension();

    imagePadding = (preferredSize - maxImageSize) / 2;
    getImpl().updatePadding();

    final int w = resolveAdjustedSize(preferredSize, widthMeasureSpec);
    final int h = resolveAdjustedSize(preferredSize, heightMeasureSpec);

    
    
    final int d = Math.min(w, h);

    
    setMeasuredDimension(
        d + shadowPadding.left + shadowPadding.right, d + shadowPadding.top + shadowPadding.bottom);
  }

  
  @ColorInt
  @Deprecated
  public int getRippleColor() {
    return rippleColor != null ? rippleColor.getDefaultColor() : 0;
  }

  
  @Nullable
  public ColorStateList getRippleColorStateList() {
    return rippleColor;
  }

  
  public void setRippleColor(@ColorInt int color) {
    setRippleColor(ColorStateList.valueOf(color));
  }

  
  public void setRippleColor(@Nullable ColorStateList color) {
    if (rippleColor != color) {
      rippleColor = color;
      getImpl().setRippleColor(rippleColor);
    }
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<FloatingActionButton> getBehavior() {
    return new FloatingActionButton.Behavior();
  }

  
  @Nullable
  @Override
  public ColorStateList getBackgroundTintList() {
    return backgroundTint;
  }

  
  @Override
  public void setBackgroundTintList(@Nullable ColorStateList tint) {
    if (backgroundTint != tint) {
      backgroundTint = tint;
      getImpl().setBackgroundTintList(tint);
    }
  }

  
  @Nullable
  @Override
  public PorterDuff.Mode getBackgroundTintMode() {
    return backgroundTintMode;
  }

  
  @Override
  public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (backgroundTintMode != tintMode) {
      backgroundTintMode = tintMode;
      getImpl().setBackgroundTintMode(tintMode);
    }
  }

  
  @Override
  public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
    setBackgroundTintList(tint);
  }

  
  @Nullable
  @Override
  public ColorStateList getSupportBackgroundTintList() {
    return getBackgroundTintList();
  }

  
  @Override
  public void setSupportBackgroundTintMode(@Nullable Mode tintMode) {
    setBackgroundTintMode(tintMode);
  }

  
  @Nullable
  @Override
  public Mode getSupportBackgroundTintMode() {
    return getBackgroundTintMode();
  }

  @Override
  public void setSupportImageTintList(@Nullable ColorStateList tint) {
    if (imageTint != tint) {
      imageTint = tint;
      onApplySupportImageTint();
    }
  }

  @Nullable
  @Override
  public ColorStateList getSupportImageTintList() {
    return imageTint;
  }

  @Override
  public void setSupportImageTintMode(@Nullable Mode tintMode) {
    if (imageMode != tintMode) {
      imageMode = tintMode;
      onApplySupportImageTint();
    }
  }

  @Nullable
  @Override
  public Mode getSupportImageTintMode() {
    return imageMode;
  }

  private void onApplySupportImageTint() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }

    if (imageTint == null) {
      DrawableCompat.clearColorFilter(drawable);
      return;
    }

    int color = imageTint.getColorForState(getDrawableState(), Color.TRANSPARENT);
    Mode mode = imageMode;
    if (mode == null) {
      mode = Mode.SRC_IN;
    }

    drawable
        .mutate()
        .setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(color, mode));
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setBackgroundResource(int resid) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setBackgroundColor(int color) {
    Log.i(LOG_TAG, "Setting a custom background is not supported.");
  }

  @Override
  public void setImageResource(@DrawableRes int resId) {
    
    imageHelper.setImageResource(resId);
    onApplySupportImageTint();
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    if (getDrawable() != drawable) {
      super.setImageDrawable(drawable);
      getImpl().updateImageMatrixScale();
      if (imageTint != null) {
        onApplySupportImageTint();
      }
    }
  }

  
  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearance) {
    getImpl().setShapeAppearance(shapeAppearance);
  }

  
  @Override
  @NonNull
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return checkNotNull(getImpl().getShapeAppearance());
  }

  
  public boolean shouldEnsureMinTouchTargetSize() {
    return getImpl().getEnsureMinTouchTargetSize();
  }

  
  public void setEnsureMinTouchTargetSize(boolean flag) {
    if (flag != getImpl().getEnsureMinTouchTargetSize()) {
      getImpl().setEnsureMinTouchTargetSize(flag);
      requestLayout();
    }
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
  }

  
  public void show() {
    show(null);
  }

  
  public void show(@Nullable final OnVisibilityChangedListener listener) {
    show(listener, true);
  }

  void show(@Nullable OnVisibilityChangedListener listener, boolean fromUser) {
    getImpl().show(wrapOnVisibilityChangedListener(listener), fromUser);
  }

  public void addOnShowAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().addOnShowAnimationListener(listener);
  }

  public void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().removeOnShowAnimationListener(listener);
  }

  
  public void hide() {
    hide(null);
  }

  
  public void hide(@Nullable OnVisibilityChangedListener listener) {
    hide(listener, true);
  }

  void hide(@Nullable OnVisibilityChangedListener listener, boolean fromUser) {
    getImpl().hide(wrapOnVisibilityChangedListener(listener), fromUser);
  }

  public void addOnHideAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().addOnHideAnimationListener(listener);
  }

  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    getImpl().removeOnHideAnimationListener(listener);
  }

  @Override
  public boolean setExpanded(boolean expanded) {
    return expandableWidgetHelper.setExpanded(expanded);
  }

  @Override
  public boolean isExpanded() {
    return expandableWidgetHelper.isExpanded();
  }

  @Override
  public void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint) {
    expandableWidgetHelper.setExpandedComponentIdHint(expandedComponentIdHint);
  }

  @Override
  public int getExpandedComponentIdHint() {
    return expandableWidgetHelper.getExpandedComponentIdHint();
  }

  
  public void setUseCompatPadding(boolean useCompatPadding) {
    if (compatPadding != useCompatPadding) {
      compatPadding = useCompatPadding;
      getImpl().onCompatShadowChanged();
    }
  }

  
  public boolean getUseCompatPadding() {
    return compatPadding;
  }

  
  public void setSize(@Size int size) {
    customSize = NO_CUSTOM_SIZE;
    if (size != this.size) {
      this.size = size;
      requestLayout();
    }
  }

  
  @Size
  public int getSize() {
    return size;
  }

  @Nullable
  private InternalVisibilityChangedListener wrapOnVisibilityChangedListener(
      @Nullable final OnVisibilityChangedListener listener) {
    if (listener == null) {
      return null;
    }

    return new InternalVisibilityChangedListener() {
      @Override
      public void onShown() {
        listener.onShown(FloatingActionButton.this);
      }

      @Override
      public void onHidden() {
        listener.onHidden(FloatingActionButton.this);
      }
    };
  }

  public boolean isOrWillBeHidden() {
    return getImpl().isOrWillBeHidden();
  }

  public boolean isOrWillBeShown() {
    return getImpl().isOrWillBeShown();
  }

  
  public void setCustomSize(@Px int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Custom size must be non-negative");
    }

    if (size != customSize) {
      customSize = size;
      requestLayout();
    }
  }

  
  @Px
  public int getCustomSize() {
    return customSize;
  }

  
  public void clearCustomSize() {
    setCustomSize(NO_CUSTOM_SIZE);
  }

  int getSizeDimension() {
    return getSizeDimension(size);
  }

  private int getSizeDimension(@Size final int size) {
    if (customSize != NO_CUSTOM_SIZE) {
      return customSize;
    }

    final Resources res = getResources();
    switch (size) {
      case SIZE_AUTO:
        
        final int width = res.getConfiguration().screenWidthDp;
        final int height = res.getConfiguration().screenHeightDp;
        return Math.max(width, height) < AUTO_MINI_LARGEST_SCREEN_WIDTH
            ? getSizeDimension(SIZE_MINI)
            : getSizeDimension(SIZE_NORMAL);
      case SIZE_MINI:
        return res.getDimensionPixelSize(R.dimen.design_fab_size_mini);
      case SIZE_NORMAL:
      default:
        return res.getDimensionPixelSize(R.dimen.design_fab_size_normal);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getImpl().onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getImpl().onDetachedFromWindow();
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    getImpl().onDrawableStateChanged(getDrawableState());
  }

  @Override
  public void jumpDrawablesToCurrentState() {
    super.jumpDrawablesToCurrentState();
    getImpl().jumpDrawableToCurrentState();
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    if (superState == null) {
      superState = new Bundle();
    }

    ExtendableSavedState state = new ExtendableSavedState(superState);
    state.extendableStates.put(
        EXPANDABLE_WIDGET_HELPER_KEY, expandableWidgetHelper.onSaveInstanceState());

    return state;
  }

  @Override
  @SuppressWarnings("argument.type.incompatible")
  
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof ExtendableSavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    ExtendableSavedState ess = (ExtendableSavedState) state;
    super.onRestoreInstanceState(ess.getSuperState());

    expandableWidgetHelper.onRestoreInstanceState(
        checkNotNull(ess.extendableStates.get(EXPANDABLE_WIDGET_HELPER_KEY)));
  }

  
  @Deprecated
  public boolean getContentRect(@NonNull Rect rect) {
    if (ViewCompat.isLaidOut(this)) {
      rect.set(0, 0, getWidth(), getHeight());
      offsetRectWithShadow(rect);
      return true;
    } else {
      return false;
    }
  }

  
  public void getMeasuredContentRect(@NonNull Rect rect) {
    rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    offsetRectWithShadow(rect);
  }

  private void offsetRectWithShadow(@NonNull Rect rect) {
    rect.left += shadowPadding.left;
    rect.top += shadowPadding.top;
    rect.right -= shadowPadding.right;
    rect.bottom -= shadowPadding.bottom;
  }

  
  @Nullable
  public Drawable getContentBackground() {
    return getImpl().getContentBackground();
  }

  private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
    int result = desiredSize;
    int specMode = MeasureSpec.getMode(measureSpec);
    int specSize = MeasureSpec.getSize(measureSpec);
    switch (specMode) {
      case MeasureSpec.UNSPECIFIED:
        
        
        result = desiredSize;
        break;
      case MeasureSpec.AT_MOST:
        
        
        
        result = Math.min(desiredSize, specSize);
        break;
      case MeasureSpec.EXACTLY:
        
        result = specSize;
        break;
      default:
        throw new IllegalArgumentException();
    }
    return result;
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      
      if (getContentRect(touchArea) && !touchArea.contains((int) ev.getX(), (int) ev.getY())) {
        return false;
      }
    }
    return super.onTouchEvent(ev);
  }

  
  
  public static class Behavior extends BaseBehavior<FloatingActionButton> {

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  
  
  protected static class BaseBehavior<T extends FloatingActionButton>
      extends CoordinatorLayout.Behavior<T> {
    private static final boolean AUTO_HIDE_DEFAULT = true;

    private Rect tmpRect;
    private OnVisibilityChangedListener internalAutoHideListener;
    private boolean autoHideEnabled;

    public BaseBehavior() {
      super();
      autoHideEnabled = AUTO_HIDE_DEFAULT;
    }

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton_Behavior_Layout);
      autoHideEnabled =
          a.getBoolean(
              R.styleable.FloatingActionButton_Behavior_Layout_behavior_autoHide,
              AUTO_HIDE_DEFAULT);
      a.recycle();
    }

    
    public void setAutoHideEnabled(boolean autoHide) {
      autoHideEnabled = autoHide;
    }

    
    public boolean isAutoHideEnabled() {
      return autoHideEnabled;
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
      if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
        
        
        lp.dodgeInsetEdges = Gravity.BOTTOM;
      }
    }

    @Override
    public boolean onDependentViewChanged(
        CoordinatorLayout parent, @NonNull FloatingActionButton child, View dependency) {
      if (dependency instanceof AppBarLayout) {
        
        
        updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
      } else if (isBottomSheet(dependency)) {
        updateFabVisibilityForBottomSheet(dependency, child);
      }
      return false;
    }

    private static boolean isBottomSheet(@NonNull View view) {
      final ViewGroup.LayoutParams lp = view.getLayoutParams();
      if (lp instanceof CoordinatorLayout.LayoutParams) {
        return ((CoordinatorLayout.LayoutParams) lp).getBehavior() instanceof BottomSheetBehavior;
      }
      return false;
    }

    @VisibleForTesting
    public void setInternalAutoHideListener(OnVisibilityChangedListener listener) {
      internalAutoHideListener = listener;
    }

    private boolean shouldUpdateVisibility(
        @NonNull View dependency, @NonNull FloatingActionButton child) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (!autoHideEnabled) {
        return false;
      }

      if (lp.getAnchorId() != dependency.getId()) {
        
        
        return false;
      }

      
      if (child.getUserSetVisibility() != VISIBLE) {
        
        return false;
      }

      return true;
    }

    private boolean updateFabVisibilityForAppBarLayout(
        CoordinatorLayout parent,
        @NonNull AppBarLayout appBarLayout,
        @NonNull FloatingActionButton child) {
      if (!shouldUpdateVisibility(appBarLayout, child)) {
        return false;
      }

      if (tmpRect == null) {
        tmpRect = new Rect();
      }

      
      final Rect rect = tmpRect;
      DescendantOffsetUtils.getDescendantRect(parent, appBarLayout, rect);

      if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
        
        child.hide(internalAutoHideListener, false);
      } else {
        
        child.show(internalAutoHideListener, false);
      }
      return true;
    }

    private boolean updateFabVisibilityForBottomSheet(
        @NonNull View bottomSheet, @NonNull FloatingActionButton child) {
      if (!shouldUpdateVisibility(bottomSheet, child)) {
        return false;
      }
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
        child.hide(internalAutoHideListener, false);
      } else {
        child.show(internalAutoHideListener, false);
      }
      return true;
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent,
        @NonNull FloatingActionButton child,
        int layoutDirection) {
      
      final List<View> dependencies = parent.getDependencies(child);
      for (int i = 0, count = dependencies.size(); i < count; i++) {
        final View dependency = dependencies.get(i);
        if (dependency instanceof AppBarLayout) {
          if (updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
            break;
          }
        } else if (isBottomSheet(dependency)) {
          if (updateFabVisibilityForBottomSheet(dependency, child)) {
            break;
          }
        }
      }
      
      parent.onLayoutChild(child, layoutDirection);
      
      offsetIfNeeded(parent, child);
      return true;
    }

    @Override
    public boolean getInsetDodgeRect(
        @NonNull CoordinatorLayout parent,
        @NonNull FloatingActionButton child,
        @NonNull Rect rect) {
      
      
      final Rect shadowPadding = child.shadowPadding;
      rect.set(
          child.getLeft() + shadowPadding.left,
          child.getTop() + shadowPadding.top,
          child.getRight() - shadowPadding.right,
          child.getBottom() - shadowPadding.bottom);
      return true;
    }

    
    private void offsetIfNeeded(
        @NonNull CoordinatorLayout parent, @NonNull FloatingActionButton fab) {
      final Rect padding = fab.shadowPadding;

      if (padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
        final CoordinatorLayout.LayoutParams lp =
            (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

        int offsetTB = 0;
        int offsetLR = 0;

        if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
          
          offsetLR = padding.right;
        } else if (fab.getLeft() <= lp.leftMargin) {
          
          offsetLR = -padding.left;
        }
        if (fab.getBottom() >= parent.getHeight() - lp.bottomMargin) {
          
          offsetTB = padding.bottom;
        } else if (fab.getTop() <= lp.topMargin) {
          
          offsetTB = -padding.top;
        }

        if (offsetTB != 0) {
          ViewCompat.offsetTopAndBottom(fab, offsetTB);
        }
        if (offsetLR != 0) {
          ViewCompat.offsetLeftAndRight(fab, offsetLR);
        }
      }
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    getImpl().updateShapeElevation(elevation);
  }

  
  public float getCompatElevation() {
    return getImpl().getElevation();
  }

  
  public void setCompatElevation(float elevation) {
    getImpl().setElevation(elevation);
  }

  
  public void setCompatElevationResource(@DimenRes int id) {
    setCompatElevation(getResources().getDimension(id));
  }

  
  public float getCompatHoveredFocusedTranslationZ() {
    return getImpl().getHoveredFocusedTranslationZ();
  }

  
  public void setCompatHoveredFocusedTranslationZ(float translationZ) {
    getImpl().setHoveredFocusedTranslationZ(translationZ);
  }

  
  public void setCompatHoveredFocusedTranslationZResource(@DimenRes int id) {
    setCompatHoveredFocusedTranslationZ(getResources().getDimension(id));
  }

  
  public float getCompatPressedTranslationZ() {
    return getImpl().getPressedTranslationZ();
  }

  
  public void setCompatPressedTranslationZ(float translationZ) {
    getImpl().setPressedTranslationZ(translationZ);
  }

  
  public void setCompatPressedTranslationZResource(@DimenRes int id) {
    setCompatPressedTranslationZ(getResources().getDimension(id));
  }

  
  @Nullable
  public MotionSpec getShowMotionSpec() {
    return getImpl().getShowMotionSpec();
  }

  
  public void setShowMotionSpec(@Nullable MotionSpec spec) {
    getImpl().setShowMotionSpec(spec);
  }

  
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  
  @Nullable
  public MotionSpec getHideMotionSpec() {
    return getImpl().getHideMotionSpec();
  }

  
  public void setHideMotionSpec(@Nullable MotionSpec spec) {
    getImpl().setHideMotionSpec(spec);
  }

  
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  
  public void addTransformationCallback(
      @NonNull TransformationCallback<? extends FloatingActionButton> listener) {
    getImpl().addTransformationCallback(new TransformationCallbackWrapper(listener));
  }

  
  public void removeTransformationCallback(
      @NonNull TransformationCallback<? extends FloatingActionButton> listener) {
    getImpl().removeTransformationCallback(new TransformationCallbackWrapper(listener));
  }

  class TransformationCallbackWrapper<T extends FloatingActionButton>
      implements InternalTransformationCallback {

    @NonNull private final TransformationCallback<T> listener;

    TransformationCallbackWrapper(@NonNull TransformationCallback<T> listener) {
      this.listener = listener;
    }

    @Override
    public void onTranslationChanged() {
      listener.onTranslationChanged((T) FloatingActionButton.this);
    }

    @Override
    public void onScaleChanged() {
      listener.onScaleChanged((T) FloatingActionButton.this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      return obj instanceof TransformationCallbackWrapper
          && ((TransformationCallbackWrapper) obj).listener.equals(listener);
    }

    @Override
    public int hashCode() {
      return listener.hashCode();
    }
  }

  @Override
  public void setTranslationX(float translationX) {
    super.setTranslationX(translationX);
    getImpl().onTranslationChanged();
  }

  @Override
  public void setTranslationY(float translationY) {
    super.setTranslationY(translationY);
    getImpl().onTranslationChanged();
  }

  @Override
  public void setTranslationZ(float translationZ) {
    super.setTranslationZ(translationZ);
    getImpl().onTranslationChanged();
  }

  @Override
  public void setScaleX(float scaleX) {
    super.setScaleX(scaleX);
    getImpl().onScaleChanged();
  }

  @Override
  public void setScaleY(float scaleY) {
    super.setScaleY(scaleY);
    getImpl().onScaleChanged();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @VisibleForTesting
  public void setShadowPaddingEnabled(boolean shadowPaddingEnabled) {
    getImpl().setShadowPaddingEnabled(shadowPaddingEnabled);
  }

  private FloatingActionButtonImpl getImpl() {
    if (impl == null) {
      impl = createImpl();
    }
    return impl;
  }

  @NonNull
  private FloatingActionButtonImpl createImpl() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return new FloatingActionButtonImplLollipop(this, new ShadowDelegateImpl());
    } else {
      return new FloatingActionButtonImpl(this, new ShadowDelegateImpl());
    }
  }

  private class ShadowDelegateImpl implements ShadowViewDelegate {
    ShadowDelegateImpl() {}

    @Override
    public float getRadius() {
      return getSizeDimension() / 2f;
    }

    @Override
    public void setShadowPadding(int left, int top, int right, int bottom) {
      shadowPadding.set(left, top, right, bottom);
      setPadding(
          left + imagePadding, top + imagePadding, right + imagePadding, bottom + imagePadding);
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
      if (background != null) {
        FloatingActionButton.super.setBackgroundDrawable(background);
      }
    }

    @Override
    public boolean isCompatPaddingEnabled() {
      return compatPadding;
    }
  }
}
