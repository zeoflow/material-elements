

package com.zeoflow.material.elements.floatingactionbutton;

import com.google.android.material.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import com.zeoflow.material.elements.animation.MotionSpec;
import com.zeoflow.material.elements.appbar.AppBarLayout;
import com.zeoflow.material.elements.bottomsheet.BottomSheetBehavior;
import com.zeoflow.material.elements.button.MaterialButton;
import com.zeoflow.material.elements.internal.DescendantOffsetUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.snackbar.Snackbar;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.util.List;


public class ExtendedFloatingActionButton extends MaterialButton implements AttachedBehavior {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_ExtendedFloatingActionButton_Icon;

  private static final int ANIM_STATE_NONE = 0;
  private static final int ANIM_STATE_HIDING = 1;
  private static final int ANIM_STATE_SHOWING = 2;

  private int animState = ANIM_STATE_NONE;

  private final AnimatorTracker changeVisibilityTracker = new AnimatorTracker();
  @NonNull private final MotionStrategy shrinkStrategy;
  @NonNull private final MotionStrategy extendStrategy;
  private final MotionStrategy showStrategy = new ShowStrategy(changeVisibilityTracker);
  private final MotionStrategy hideStrategy = new HideStrategy(changeVisibilityTracker);

  @NonNull private final Behavior<ExtendedFloatingActionButton> behavior;

  private boolean isExtended = true;

  
  public abstract static class OnChangedCallback {

    
    public void onShown(ExtendedFloatingActionButton extendedFab) {}

    
    public void onHidden(ExtendedFloatingActionButton extendedFab) {}

    
    public void onExtended(ExtendedFloatingActionButton extendedFab) {}

    
    public void onShrunken(ExtendedFloatingActionButton extendedFab) {}
  }

  public ExtendedFloatingActionButton(@NonNull Context context) {
    this(context, null);
  }

  public ExtendedFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.extendedFloatingActionButtonStyle);
  }

  @SuppressWarnings("nullness")
  public ExtendedFloatingActionButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();
    behavior = new ExtendedFloatingActionButtonBehavior<>(context, attrs);
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.ExtendedFloatingActionButton, defStyleAttr, DEF_STYLE_RES);

    MotionSpec showMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_showMotionSpec);
    MotionSpec hideMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_hideMotionSpec);
    MotionSpec extendMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_extendMotionSpec);
    MotionSpec shrinkMotionSpec =
        MotionSpec.createFromAttribute(
            context, a, R.styleable.ExtendedFloatingActionButton_shrinkMotionSpec);

    AnimatorTracker changeSizeTracker = new AnimatorTracker();
    extendStrategy = new ChangeSizeStrategy(
        changeSizeTracker,
        new Size() {
          @Override
          public int getWidth() {
            return getMeasuredWidth();
          }

          @Override
          public int getHeight() {
            return getMeasuredHeight();
          }

          @Override
          public LayoutParams getLayoutParams() {
            return new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
          }
        },
         true);

    shrinkStrategy = new ChangeSizeStrategy(
        changeSizeTracker,
        new Size() {
          @Override
          public int getWidth() {
            return getCollapsedSize();
          }

          @Override
          public int getHeight() {
            return getCollapsedSize();
          }

          @Override
          public LayoutParams getLayoutParams() {
            return new LayoutParams(getWidth(), getHeight());
          }
        },
         false);

    showStrategy.setMotionSpec(showMotionSpec);
    hideStrategy.setMotionSpec(hideMotionSpec);
    extendStrategy.setMotionSpec(extendMotionSpec);
    shrinkStrategy.setMotionSpec(shrinkMotionSpec);
    a.recycle();

    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder(
            context, attrs, defStyleAttr, DEF_STYLE_RES, ShapeAppearanceModel.PILL
        ).build();
    setShapeAppearanceModel(shapeAppearanceModel);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    
    if (isExtended && TextUtils.isEmpty(getText()) && getIcon() != null) {
      isExtended = false;
      shrinkStrategy.performNow();
    }
  }

  @NonNull
  @Override
  public Behavior<ExtendedFloatingActionButton> getBehavior() {
    return behavior;
  }


  
  public void setExtended(boolean extended) {
    if (this.isExtended == extended) {
      return;
    }

    MotionStrategy motionStrategy = extended ? extendStrategy : shrinkStrategy;
    if (motionStrategy.shouldCancel()) {
      return;
    }

    motionStrategy.performNow();
  }

  public final boolean isExtended() {
    return isExtended;
  }

  
  public void addOnShowAnimationListener(@NonNull AnimatorListener listener) {
    showStrategy.addAnimationListener(listener);
  }

  
  public void removeOnShowAnimationListener(@NonNull AnimatorListener listener) {
    showStrategy.removeAnimationListener(listener);
  }

  
  public void addOnHideAnimationListener(@NonNull AnimatorListener listener) {
    hideStrategy.addAnimationListener(listener);
  }

  
  public void removeOnHideAnimationListener(@NonNull AnimatorListener listener) {
    hideStrategy.removeAnimationListener(listener);
  }

  
  public void addOnShrinkAnimationListener(@NonNull AnimatorListener listener) {
    shrinkStrategy.addAnimationListener(listener);
  }

  
  public void removeOnShrinkAnimationListener(@NonNull AnimatorListener listener) {
    shrinkStrategy.removeAnimationListener(listener);
  }

  
  public void addOnExtendAnimationListener(@NonNull AnimatorListener listener) {
    extendStrategy.addAnimationListener(listener);
  }

  
  public void removeOnExtendAnimationListener(@NonNull AnimatorListener listener) {
    extendStrategy.removeAnimationListener(listener);
  }

  
  public void hide() {
    performMotion(hideStrategy, null);
  }

  
  public void hide(@NonNull OnChangedCallback callback) {
    performMotion(hideStrategy, callback);
  }

  
  public void show() {
    performMotion(showStrategy, null);
  }

  
  public void show(@NonNull OnChangedCallback callback) {
    performMotion(showStrategy, callback);
  }

  
  public void extend() {
    performMotion(extendStrategy, null);
  }

  
  public void extend(@NonNull final OnChangedCallback callback) {
    performMotion(extendStrategy, callback);
  }


  
  public void shrink() {
    performMotion(shrinkStrategy, null);
  }

  
  public void shrink(@NonNull final OnChangedCallback callback) {
    performMotion(shrinkStrategy, callback);
  }

  
  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showStrategy.getMotionSpec();
  }

  
  public void setShowMotionSpec(@Nullable MotionSpec spec) {
    showStrategy.setMotionSpec(spec);
  }

  
  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  
  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideStrategy.getMotionSpec();
  }

  
  public void setHideMotionSpec(@Nullable MotionSpec spec) {
    hideStrategy.setMotionSpec(spec);
  }

  
  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  
  @Nullable
  public MotionSpec getExtendMotionSpec() {
    return extendStrategy.getMotionSpec();
  }

  
  public void setExtendMotionSpec(@Nullable MotionSpec spec) {
    extendStrategy.setMotionSpec(spec);
  }

  
  public void setExtendMotionSpecResource(@AnimatorRes int id) {
    setExtendMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  
  @Nullable
  public MotionSpec getShrinkMotionSpec() {
    return shrinkStrategy.getMotionSpec();
  }

  
  public void setShrinkMotionSpec(@Nullable MotionSpec spec) {
    shrinkStrategy.setMotionSpec(spec);
  }

  
  public void setShrinkMotionSpecResource(@AnimatorRes int id) {
    setShrinkMotionSpec(MotionSpec.createFromResource(getContext(), id));
  }

  private void performMotion(
      @NonNull final MotionStrategy strategy, @Nullable final OnChangedCallback callback) {
    if (strategy.shouldCancel()) {
      return;
    }

    boolean shouldAnimate = shouldAnimateVisibilityChange();
    if (!shouldAnimate) {
      strategy.performNow();
      strategy.onChange(callback);
      return;
    }

    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    Animator animator = strategy.createAnimator();
    animator.addListener(
        new AnimatorListenerAdapter() {
          private boolean cancelled;

          @Override
          public void onAnimationStart(Animator animation) {
            strategy.onAnimationStart(animation);
            cancelled = false;
          }

          @Override
          public void onAnimationCancel(Animator animation) {
            cancelled = true;
            strategy.onAnimationCancel();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            strategy.onAnimationEnd();
            if (!cancelled) {
              strategy.onChange(callback);
            }
          }
        });

    for (AnimatorListener l : strategy.getListeners()) {
      animator.addListener(l);
    }

    animator.start();
  }

  private boolean isOrWillBeShown() {
    if (getVisibility() != View.VISIBLE) {
      
      return animState == ANIM_STATE_SHOWING;
    } else {
      
      return animState != ANIM_STATE_HIDING;
    }
  }

  private boolean isOrWillBeHidden() {
    if (getVisibility() == View.VISIBLE) {
      
      return animState == ANIM_STATE_HIDING;
    } else {
      
      return animState != ANIM_STATE_SHOWING;
    }
  }

  private boolean shouldAnimateVisibilityChange() {
    return ViewCompat.isLaidOut(this) && !isInEditMode();
  }

  
  static final Property<View, Float> WIDTH =
      new Property<View, Float>(Float.class, "width") {
        @Override
        public void set(@NonNull View object, @NonNull Float value) {
          object.getLayoutParams().width = value.intValue();
          object.requestLayout();
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
          return (float) object.getLayoutParams().width;
        }
      };

  
  static final Property<View, Float> HEIGHT =
      new Property<View, Float>(Float.class, "height") {
        @Override
        public void set(@NonNull View object, @NonNull Float value) {
          object.getLayoutParams().height = value.intValue();
          object.requestLayout();
        }

        @NonNull
        @Override
        public Float get(@NonNull View object) {
          return (float) object.getLayoutParams().height;
        }
      };

  
  @VisibleForTesting
  int getCollapsedSize() {
    return Math.min(ViewCompat.getPaddingStart(this), ViewCompat.getPaddingEnd(this)) * 2
        + getIconSize();
  }

  
  protected static class ExtendedFloatingActionButtonBehavior<
      T extends ExtendedFloatingActionButton>
      extends CoordinatorLayout.Behavior<T> {
    private static final boolean AUTO_HIDE_DEFAULT = false;
    private static final boolean AUTO_SHRINK_DEFAULT = true;

    private Rect tmpRect;
    @Nullable private OnChangedCallback internalAutoHideCallback;
    @Nullable private OnChangedCallback internalAutoShrinkCallback;
    private boolean autoHideEnabled;
    private boolean autoShrinkEnabled;

    public ExtendedFloatingActionButtonBehavior() {
      super();
      autoHideEnabled = AUTO_HIDE_DEFAULT;
      autoShrinkEnabled = AUTO_SHRINK_DEFAULT;
    }

    
    @SuppressWarnings("argument.type.incompatible")
    public ExtendedFloatingActionButtonBehavior(
        @NonNull Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      TypedArray a =
          context.obtainStyledAttributes(
              attrs, R.styleable.ExtendedFloatingActionButton_Behavior_Layout);
      autoHideEnabled =
          a.getBoolean(
              R.styleable.ExtendedFloatingActionButton_Behavior_Layout_behavior_autoHide,
              AUTO_HIDE_DEFAULT);
      autoShrinkEnabled =
          a.getBoolean(
              R.styleable.ExtendedFloatingActionButton_Behavior_Layout_behavior_autoShrink,
              AUTO_SHRINK_DEFAULT);
      a.recycle();
    }

    
    public void setAutoHideEnabled(boolean autoHide) {
      autoHideEnabled = autoHide;
    }

    
    public boolean isAutoHideEnabled() {
      return autoHideEnabled;
    }

    
    public void setAutoShrinkEnabled(boolean autoShrink) {
      autoShrinkEnabled = autoShrink;
    }

    
    public boolean isAutoShrinkEnabled() {
      return autoShrinkEnabled;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    
    
    public boolean getInsetDodgeRect(
        @NonNull CoordinatorLayout parent,
        @NonNull ExtendedFloatingActionButton child,
        @NonNull Rect rect) {
      return super.getInsetDodgeRect(parent, (T) child, rect);
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
      if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
        
        
        lp.dodgeInsetEdges = Gravity.BOTTOM;
      }
    }

    @Override
    public boolean onDependentViewChanged(
        CoordinatorLayout parent, @NonNull ExtendedFloatingActionButton child, View dependency) {
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
    void setInternalAutoHideCallback(@Nullable OnChangedCallback callback) {
      internalAutoHideCallback = callback;
    }

    @VisibleForTesting
    void setInternalAutoShrinkCallback(@Nullable OnChangedCallback callback) {
      internalAutoShrinkCallback = callback;
    }

    private boolean shouldUpdateVisibility(
        @NonNull View dependency, @NonNull ExtendedFloatingActionButton child) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (!autoHideEnabled && !autoShrinkEnabled) {
        return false;
      }

      if (lp.getAnchorId() != dependency.getId()) {
        
        
        return false;
      }

      return true;
    }

    private boolean updateFabVisibilityForAppBarLayout(
        CoordinatorLayout parent,
        @NonNull AppBarLayout appBarLayout,
        @NonNull ExtendedFloatingActionButton child) {
      if (!shouldUpdateVisibility(appBarLayout, child)) {
        return false;
      }

      if (tmpRect == null) {
        tmpRect = new Rect();
      }

      
      final Rect rect = tmpRect;
      DescendantOffsetUtils.getDescendantRect(parent, appBarLayout, rect);

      if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
        
        shrinkOrHide(child);
      } else {
        
        extendOrShow(child);
      }
      return true;
    }

    private boolean updateFabVisibilityForBottomSheet(
        @NonNull View bottomSheet, @NonNull ExtendedFloatingActionButton child) {
      if (!shouldUpdateVisibility(bottomSheet, child)) {
        return false;
      }
      CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
        shrinkOrHide(child);
      } else {
        extendOrShow(child);
      }
      return true;
    }

    
    protected void shrinkOrHide(@NonNull ExtendedFloatingActionButton fab) {
      OnChangedCallback callback = autoShrinkEnabled
          ? internalAutoShrinkCallback
          : internalAutoHideCallback;
      MotionStrategy strategy = autoShrinkEnabled
          ? fab.shrinkStrategy
          : fab.hideStrategy;

      fab.performMotion(strategy, callback);
    }

    
    protected void extendOrShow(@NonNull ExtendedFloatingActionButton fab) {
      OnChangedCallback callback = autoShrinkEnabled
          ? internalAutoShrinkCallback
          : internalAutoHideCallback;
      MotionStrategy strategy = autoShrinkEnabled
          ? fab.extendStrategy
          : fab.showStrategy;

      fab.performMotion(strategy, callback);
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent,
        @NonNull ExtendedFloatingActionButton child,
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
      return true;
    }
  }

  interface Size {
    int getWidth();
    int getHeight();
    LayoutParams getLayoutParams();
  }

  class ChangeSizeStrategy extends BaseMotionStrategy {

    private final Size size;
    private final boolean extending;

    ChangeSizeStrategy(AnimatorTracker animatorTracker, Size size, boolean extending) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
      this.size = size;
      this.extending = extending;
    }

    @Override
    public void performNow() {
      isExtended = extending;
      LayoutParams layoutParams = getLayoutParams();
      if (layoutParams == null) {
        return;
      }

      layoutParams.width = size.getLayoutParams().width;
      layoutParams.height = size.getLayoutParams().height;
      requestLayout();
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback == null) {
        return;
      }

      if (extending) {
        callback.onExtended(ExtendedFloatingActionButton.this);
      } else {
        callback.onShrunken(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_change_size_motion_spec;
    }

    @NonNull
    @Override
    public AnimatorSet createAnimator() {
      MotionSpec spec = getCurrentMotionSpec();
      if (spec.hasPropertyValues("width")) {
        PropertyValuesHolder[] widthValues = spec.getPropertyValues("width");
        widthValues[0].setFloatValues(getWidth(), size.getWidth());
        spec.setPropertyValues("width", widthValues);
      }

      if (spec.hasPropertyValues("height")) {
        PropertyValuesHolder[] heightValues = spec.getPropertyValues("height");
        heightValues[0].setFloatValues(getHeight(), size.getHeight());
        spec.setPropertyValues("height", heightValues);
      }

      return super.createAnimator(spec);
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      isExtended = extending;
      setHorizontallyScrolling(true);
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      setHorizontallyScrolling(false);

      LayoutParams layoutParams = getLayoutParams();
      if (layoutParams == null) {
        return;
      }
      layoutParams.width = size.getLayoutParams().width;
      layoutParams.height = size.getLayoutParams().height;
    }

    @Override
    public boolean shouldCancel() {
      return extending == isExtended || getIcon() == null || TextUtils.isEmpty(getText());
    }
  }

  class ShowStrategy extends BaseMotionStrategy {

    public ShowStrategy(AnimatorTracker animatorTracker) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
    }

    @Override
    public void performNow() {
      setVisibility(VISIBLE);
      setAlpha(1f);
      setScaleY(1f);
      setScaleX(1f);
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback != null) {
        callback.onShown(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_show_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animation) {
      super.onAnimationStart(animation);
      setVisibility(VISIBLE);
      animState = ANIM_STATE_SHOWING;
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      animState = ANIM_STATE_NONE;
    }

    @Override
    public boolean shouldCancel() {
      return isOrWillBeShown();
    }
  }

  class HideStrategy extends BaseMotionStrategy {

    private boolean isCancelled;

    public HideStrategy(AnimatorTracker animatorTracker) {
      super(ExtendedFloatingActionButton.this, animatorTracker);
    }

    @Override
    public void performNow() {
      setVisibility(GONE);
    }

    @Override
    public void onChange(@Nullable final OnChangedCallback callback) {
      if (callback != null) {
        callback.onHidden(ExtendedFloatingActionButton.this);
      }
    }

    @Override
    public boolean shouldCancel() {
      return isOrWillBeHidden();
    }

    @Override
    public int getDefaultMotionSpecResource() {
      return R.animator.mtrl_extended_fab_hide_motion_spec;
    }

    @Override
    public void onAnimationStart(Animator animator) {
      super.onAnimationStart(animator);
      isCancelled = false;
      setVisibility(VISIBLE);
      animState = ANIM_STATE_HIDING;
    }

    @Override
    public void onAnimationCancel() {
      super.onAnimationCancel();
      isCancelled = true;
    }

    @Override
    public void onAnimationEnd() {
      super.onAnimationEnd();
      animState = ANIM_STATE_NONE;
      if (!isCancelled) {
        setVisibility(GONE);
      }
    }
  }
}
