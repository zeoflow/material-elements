

package com.zeoflow.material.elements.bottomappbar;

import com.google.android.material.R;

import static com.zeoflow.material.elements.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.ViewCompat.ScrollAxis;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import com.zeoflow.material.elements.animation.TransformationCallback;
import com.zeoflow.material.elements.behavior.HideBottomViewOnScrollBehavior;
import com.zeoflow.material.elements.floatingactionbutton.ExtendedFloatingActionButton;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButton;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.internal.ViewUtils.RelativePadding;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.EdgeTreatment;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class BottomAppBar extends Toolbar implements AttachedBehavior {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_BottomAppBar;

  private static final long ANIMATION_DURATION = 300;

  public static final int FAB_ALIGNMENT_MODE_CENTER = 0;
  public static final int FAB_ALIGNMENT_MODE_END = 1;

  
  @IntDef({FAB_ALIGNMENT_MODE_CENTER, FAB_ALIGNMENT_MODE_END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAlignmentMode {}

  public static final int FAB_ANIMATION_MODE_SCALE = 0;
  public static final int FAB_ANIMATION_MODE_SLIDE = 1;

  
  @IntDef({FAB_ANIMATION_MODE_SCALE, FAB_ANIMATION_MODE_SLIDE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FabAnimationMode {}

  private final int fabOffsetEndMode;
  private final MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();

  @Nullable private Animator modeAnimator;
  @Nullable private Animator menuAnimator;
  @FabAlignmentMode private int fabAlignmentMode;
  @FabAnimationMode private int fabAnimationMode;
  private boolean hideOnScroll;
  private final boolean paddingBottomSystemWindowInsets;
  private final boolean paddingLeftSystemWindowInsets;
  private final boolean paddingRightSystemWindowInsets;

  
  private int animatingModeChangeCounter = 0;
  private ArrayList<AnimationListener> animationListeners;

  
  interface AnimationListener {
    void onAnimationStart(BottomAppBar bar);
    void onAnimationEnd(BottomAppBar bar);
  }

  
  private boolean fabAttached = true;

  private Behavior behavior;

  private int bottomInset;
  private int rightInset;
  private int leftInset;

  
  @NonNull
  AnimatorListenerAdapter fabAnimationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
          maybeAnimateMenuView(fabAlignmentMode, fabAttached);
        }
      };

  
  @NonNull
  TransformationCallback<FloatingActionButton> fabTransformationCallback =
      new TransformationCallback<FloatingActionButton>() {
        @Override
        public void onScaleChanged(@NonNull FloatingActionButton fab) {
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() == View.VISIBLE ? fab.getScaleY() : 0);
        }

        @Override
        public void onTranslationChanged(@NonNull FloatingActionButton fab) {
          float horizontalOffset = fab.getTranslationX();
          if (getTopEdgeTreatment().getHorizontalOffset() != horizontalOffset) {
            getTopEdgeTreatment().setHorizontalOffset(horizontalOffset);
            materialShapeDrawable.invalidateSelf();
          }

          
          float verticalOffset = Math.max(0, -fab.getTranslationY());
          if (getTopEdgeTreatment().getCradleVerticalOffset() != verticalOffset) {
            getTopEdgeTreatment().setCradleVerticalOffset(verticalOffset);
            materialShapeDrawable.invalidateSelf();
          }
          materialShapeDrawable.setInterpolation(
              fab.getVisibility() == View.VISIBLE ? fab.getScaleY() : 0);
        }
      };

  public BottomAppBar(@NonNull Context context) {
    this(context, null, 0);
  }

  public BottomAppBar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomAppBarStyle);
  }

  public BottomAppBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.BottomAppBar, defStyleAttr, DEF_STYLE_RES);

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(context, a, R.styleable.BottomAppBar_backgroundTint);

    int elevation = a.getDimensionPixelSize(R.styleable.BottomAppBar_elevation, 0);
    float fabCradleMargin = a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleMargin, 0);
    float fabCornerRadius =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleRoundedCornerRadius, 0);
    float fabVerticalOffset =
        a.getDimensionPixelOffset(R.styleable.BottomAppBar_fabCradleVerticalOffset, 0);
    fabAlignmentMode =
        a.getInt(R.styleable.BottomAppBar_fabAlignmentMode, FAB_ALIGNMENT_MODE_CENTER);
    fabAnimationMode =
        a.getInt(R.styleable.BottomAppBar_fabAnimationMode, FAB_ANIMATION_MODE_SCALE);
    hideOnScroll = a.getBoolean(R.styleable.BottomAppBar_hideOnScroll, false);
    
    paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingBottomSystemWindowInsets, false);
    paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingLeftSystemWindowInsets, false);
    paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.BottomAppBar_paddingRightSystemWindowInsets, false);

    a.recycle();

    fabOffsetEndMode =
        getResources().getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fabOffsetEndMode);

    EdgeTreatment topEdgeTreatment =
        new BottomAppBarTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset);
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder().setTopEdge(topEdgeTreatment).build();
    materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    materialShapeDrawable.setShadowCompatibilityMode(SHADOW_COMPAT_MODE_ALWAYS);
    materialShapeDrawable.setPaintStyle(Style.FILL);
    materialShapeDrawable.initializeElevationOverlay(context);
    setElevation(elevation);
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint);
    ViewCompat.setBackground(this, materialShapeDrawable);

    ViewUtils.doOnApplyWindowInsets(
        this,
        attrs,
        defStyleAttr,
        DEF_STYLE_RES,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            
            
            boolean leftInsetsChanged = false;
            boolean rightInsetsChanged = false;
            if (paddingBottomSystemWindowInsets) {
              bottomInset = insets.getSystemWindowInsetBottom();
            }
            if (paddingLeftSystemWindowInsets) {
              leftInsetsChanged = leftInset != insets.getSystemWindowInsetLeft();
              leftInset = insets.getSystemWindowInsetLeft();
            }
            if (paddingRightSystemWindowInsets) {
              rightInsetsChanged = rightInset != insets.getSystemWindowInsetRight();
              rightInset = insets.getSystemWindowInsetRight();
            }

            
            
            if (leftInsetsChanged || rightInsetsChanged) {
              cancelAnimations();

              setCutoutState();
              setActionMenuViewPosition();
            }

            return insets;
          }
        });
  }

  
  @FabAlignmentMode
  public int getFabAlignmentMode() {
    return fabAlignmentMode;
  }

  
  public void setFabAlignmentMode(@FabAlignmentMode int fabAlignmentMode) {
    maybeAnimateModeChange(fabAlignmentMode);
    maybeAnimateMenuView(fabAlignmentMode, fabAttached);
    this.fabAlignmentMode = fabAlignmentMode;
  }

  
  @FabAnimationMode
  public int getFabAnimationMode() {
    return fabAnimationMode;
  }

  
  public void setFabAnimationMode(@FabAnimationMode int fabAnimationMode) {
    this.fabAnimationMode = fabAnimationMode;
  }

  public void setBackgroundTint(@Nullable ColorStateList backgroundTint) {
    DrawableCompat.setTintList(materialShapeDrawable, backgroundTint);
  }

  @Nullable
  public ColorStateList getBackgroundTint() {
    return materialShapeDrawable.getTintList();
  }

  
  public float getFabCradleMargin() {
    return getTopEdgeTreatment().getFabCradleMargin();
  }

  
  public void setFabCradleMargin(@Dimension float cradleMargin) {
    if (cradleMargin != getFabCradleMargin()) {
      getTopEdgeTreatment().setFabCradleMargin(cradleMargin);
      materialShapeDrawable.invalidateSelf();
    }
  }

  
  @Dimension
  public float getFabCradleRoundedCornerRadius() {
    return getTopEdgeTreatment().getFabCradleRoundedCornerRadius();
  }

  
  public void setFabCradleRoundedCornerRadius(@Dimension float roundedCornerRadius) {
    if (roundedCornerRadius != getFabCradleRoundedCornerRadius()) {
      getTopEdgeTreatment().setFabCradleRoundedCornerRadius(roundedCornerRadius);
      materialShapeDrawable.invalidateSelf();
    }
  }

  
  @Dimension
  public float getCradleVerticalOffset() {
    return getTopEdgeTreatment().getCradleVerticalOffset();
  }

  
  public void setCradleVerticalOffset(@Dimension float verticalOffset) {
    if (verticalOffset != getCradleVerticalOffset()) {
      getTopEdgeTreatment().setCradleVerticalOffset(verticalOffset);
      materialShapeDrawable.invalidateSelf();
      setCutoutState();
    }
  }

  
  public boolean getHideOnScroll() {
    return hideOnScroll;
  }

  
  public void setHideOnScroll(boolean hide) {
    hideOnScroll = hide;
  }

  
  public void performHide() {
    getBehavior().slideDown(this);
  }

  
  public void performShow() {
    getBehavior().slideUp(this);
  }

  @Override
  public void setElevation(float elevation) {
    materialShapeDrawable.setElevation(elevation);
    
    int topShadowHeight =
        materialShapeDrawable.getShadowRadius() - materialShapeDrawable.getShadowOffsetY();
    getBehavior().setAdditionalHiddenOffsetY(this, topShadowHeight);
  }

  
  public void replaceMenu(@MenuRes int newMenu) {
    getMenu().clear();
    inflateMenu(newMenu);
  }

  
  void addAnimationListener(@NonNull AnimationListener listener) {
    if (animationListeners == null) {
      animationListeners = new ArrayList<>();
    }
    animationListeners.add(listener);
  }

  void removeAnimationListener(@NonNull AnimationListener listener) {
    if (animationListeners == null) {
      return;
    }
    animationListeners.remove(listener);
  }

  private void dispatchAnimationStart() {
    if (animatingModeChangeCounter++ == 0 && animationListeners != null) {
      
      for (AnimationListener listener : animationListeners) {
        listener.onAnimationStart(this);
      }
    }
  }

  private void dispatchAnimationEnd() {
    if (--animatingModeChangeCounter == 0 && animationListeners != null) {
      
      for (AnimationListener listener : animationListeners) {
        listener.onAnimationEnd(this);
      }
    }
  }

  
  boolean setFabDiameter(@Px int diameter) {
    if (diameter != getTopEdgeTreatment().getFabDiameter()) {
      getTopEdgeTreatment().setFabDiameter(diameter);
      materialShapeDrawable.invalidateSelf();
      return true;
    }

    return false;
  }

  private void maybeAnimateModeChange(@FabAlignmentMode int targetMode) {
    if (fabAlignmentMode == targetMode || !ViewCompat.isLaidOut(this)) {
      return;
    }

    if (modeAnimator != null) {
      modeAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    if (fabAnimationMode == FAB_ANIMATION_MODE_SLIDE) {
      createFabTranslationXAnimation(targetMode, animators);
    } else {
      createFabDefaultXAnimation(targetMode, animators);
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    modeAnimator = set;
    modeAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            dispatchAnimationStart();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            dispatchAnimationEnd();
          }
        });
    modeAnimator.start();
  }

  @Nullable
  private FloatingActionButton findDependentFab() {
    View view = findDependentView();
    return view instanceof FloatingActionButton ? (FloatingActionButton) view : null;
  }

  @Nullable
  private View findDependentView() {
    if (!(getParent() instanceof CoordinatorLayout)) {
      
      return null;
    }

    List<View> dependents = ((CoordinatorLayout) getParent()).getDependents(this);
    for (View v : dependents) {
      if (v instanceof FloatingActionButton || v instanceof ExtendedFloatingActionButton) {
        return v;
      }
    }

    return null;
  }

  private boolean isFabVisibleOrWillBeShown() {
    FloatingActionButton fab = findDependentFab();
    return fab != null && fab.isOrWillBeShown();
  }

  
  protected void createFabDefaultXAnimation(
      final @FabAlignmentMode int targetMode, List<Animator> animators) {
    final FloatingActionButton fab = findDependentFab();

    if (fab == null || fab.isOrWillBeHidden()) {
      return;
    }

    dispatchAnimationStart();

    fab.hide(
        new FloatingActionButton.OnVisibilityChangedListener() {
          @Override
          public void onHidden(@NonNull FloatingActionButton fab) {
            fab.setTranslationX(getFabTranslationX(targetMode));
            fab.show(
                new FloatingActionButton.OnVisibilityChangedListener() {
                  @Override
                  public void onShown(FloatingActionButton fab) {
                    dispatchAnimationEnd();
                  }
                });
          }
        });
  }

  private void createFabTranslationXAnimation(
      @FabAlignmentMode int targetMode, @NonNull List<Animator> animators) {
    ObjectAnimator animator =
        ObjectAnimator.ofFloat(findDependentFab(), "translationX", getFabTranslationX(targetMode));
    animator.setDuration(ANIMATION_DURATION);
    animators.add(animator);
  }

  private void maybeAnimateMenuView(@FabAlignmentMode int targetMode, boolean newFabAttached) {
    if (!ViewCompat.isLaidOut(this)) {
      return;
    }

    if (menuAnimator != null) {
      menuAnimator.cancel();
    }

    List<Animator> animators = new ArrayList<>();

    
    if (!isFabVisibleOrWillBeShown()) {
      targetMode = FAB_ALIGNMENT_MODE_CENTER;
      newFabAttached = false;
    }

    createMenuViewTranslationAnimation(targetMode, newFabAttached, animators);

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    menuAnimator = set;
    menuAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            dispatchAnimationStart();
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            dispatchAnimationEnd();
            menuAnimator = null;
          }
        });
    menuAnimator.start();
  }

  private void createMenuViewTranslationAnimation(
      @FabAlignmentMode final int targetMode,
      final boolean targetAttached,
      @NonNull List<Animator> animators) {

    final ActionMenuView actionMenuView = getActionMenuView();

    
    if (actionMenuView == null) {
      return;
    }

    Animator fadeIn = ObjectAnimator.ofFloat(actionMenuView, "alpha", 1);

    float translationXDifference =
        actionMenuView.getTranslationX()
            - getActionMenuViewTranslationX(actionMenuView, targetMode, targetAttached);

    
    if (Math.abs(translationXDifference) > 1) {
      
      Animator fadeOut = ObjectAnimator.ofFloat(actionMenuView, "alpha", 0);

      fadeOut.addListener(
          new AnimatorListenerAdapter() {
            public boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
              cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              if (!cancelled) {
                translateActionMenuView(actionMenuView, targetMode, targetAttached);
              }
            }
          });

      AnimatorSet set = new AnimatorSet();
      set.setDuration(ANIMATION_DURATION / 2);
      set.playSequentially(fadeOut, fadeIn);
      animators.add(set);
    } else if (actionMenuView.getAlpha() < 1) {
      
      
      animators.add(fadeIn);
    }
  }

  private float getFabTranslationY() {
    return -getTopEdgeTreatment().getCradleVerticalOffset();
  }

  private float getFabTranslationX(@FabAlignmentMode int fabAlignmentMode) {
    boolean isRtl = ViewUtils.isLayoutRtl(this);
    if (fabAlignmentMode == FAB_ALIGNMENT_MODE_END) {
      int systemEndInset = isRtl ? leftInset : rightInset;
      int totalEndInset = fabOffsetEndMode + systemEndInset;
      return (getMeasuredWidth() / 2 - totalEndInset) * (isRtl ? -1 : 1);
    } else {
      return 0;
    }
  }

  private float getFabTranslationX() {
    return getFabTranslationX(fabAlignmentMode);
  }

  @Nullable
  private ActionMenuView getActionMenuView() {
    for (int i = 0; i < getChildCount(); i++) {
      View view = getChildAt(i);
      if (view instanceof ActionMenuView) {
        return (ActionMenuView) view;
      }
    }

    return null;
  }

  
  private void translateActionMenuView(
      @NonNull ActionMenuView actionMenuView,
      @FabAlignmentMode int fabAlignmentMode,
      boolean fabAttached) {
    actionMenuView.setTranslationX(
        getActionMenuViewTranslationX(actionMenuView, fabAlignmentMode, fabAttached));
  }

  
  protected int getActionMenuViewTranslationX(
      @NonNull ActionMenuView actionMenuView,
      @FabAlignmentMode int fabAlignmentMode,
      boolean fabAttached) {
    if (fabAlignmentMode != FAB_ALIGNMENT_MODE_END || !fabAttached) {
      return 0;
    }

    boolean isRtl = ViewUtils.isLayoutRtl(this);
    int toolbarLeftContentEnd = isRtl ? getMeasuredWidth() : 0;

    
    for (int i = 0; i < getChildCount(); i++) {
      View view = getChildAt(i);
      boolean isAlignedToStart =
          view.getLayoutParams() instanceof Toolbar.LayoutParams
              && (((Toolbar.LayoutParams) view.getLayoutParams()).gravity
                      & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
                  == Gravity.START;
      if (isAlignedToStart) {
        toolbarLeftContentEnd =
            isRtl
                ? Math.min(toolbarLeftContentEnd, view.getLeft())
                : Math.max(toolbarLeftContentEnd, view.getRight());
      }
    }

    int actionMenuViewStart = isRtl ? actionMenuView.getRight() : actionMenuView.getLeft();
    int systemStartInset = isRtl ? rightInset : -leftInset;
    int end = actionMenuViewStart + systemStartInset;

    return toolbarLeftContentEnd - end;
  }

  private void cancelAnimations() {
    if (menuAnimator != null) {
      menuAnimator.cancel();
    }
    if (modeAnimator != null) {
      modeAnimator.cancel();
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    
    
    
    
    if (changed) {
      cancelAnimations();

      setCutoutState();
    }

    
    setActionMenuViewPosition();
  }

  @NonNull
  private BottomAppBarTopEdgeTreatment getTopEdgeTreatment() {
    return (BottomAppBarTopEdgeTreatment)
        materialShapeDrawable.getShapeAppearanceModel().getTopEdge();
  }

  private void setCutoutState() {
    
    getTopEdgeTreatment().setHorizontalOffset(getFabTranslationX());
    View fab = findDependentView();
    materialShapeDrawable.setInterpolation(fabAttached && isFabVisibleOrWillBeShown() ? 1 : 0);
    if (fab != null) {
      fab.setTranslationY(getFabTranslationY());
      fab.setTranslationX(getFabTranslationX());
    }
  }

  private void setActionMenuViewPosition() {
    ActionMenuView actionMenuView = getActionMenuView();
    if (actionMenuView != null) {
      actionMenuView.setAlpha(1.0f);
      if (!isFabVisibleOrWillBeShown()) {
        translateActionMenuView(actionMenuView, FAB_ALIGNMENT_MODE_CENTER, false);
      } else {
        translateActionMenuView(actionMenuView, fabAlignmentMode, fabAttached);
      }
    }
  }

  
  private void addFabAnimationListeners(@NonNull FloatingActionButton fab) {
    fab.addOnHideAnimationListener(fabAnimationListener);
    fab.addOnShowAnimationListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            fabAnimationListener.onAnimationStart(animation);

            
            FloatingActionButton fab = findDependentFab();
            if (fab != null) {
              fab.setTranslationX(getFabTranslationX());
            }
          }
        });
    fab.addTransformationCallback(fabTransformationCallback);
  }

  private int getBottomInset() {
    return bottomInset;
  }

  private int getRightInset() {
    return rightInset;
  }

  private int getLeftInset() {
    return leftInset;
  }

  @Override
  public void setTitle(CharSequence title) {
    
  }

  @Override
  public void setSubtitle(CharSequence subtitle) {
    
  }

  @NonNull
  @Override
  public Behavior getBehavior() {
    if (behavior == null) {
      behavior = new Behavior();
    }
    return behavior;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, materialShapeDrawable);

    
    
    if (getParent() instanceof ViewGroup) {
      ((ViewGroup) getParent()).setClipChildren(false);
    }
  }

  
  public static class Behavior extends HideBottomViewOnScrollBehavior<BottomAppBar> {

    @NonNull private final Rect fabContentRect;

    private WeakReference<BottomAppBar> viewRef;

    private int originalBottomMargin;

    private final OnLayoutChangeListener fabLayoutListener =
        new OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(
              View v,
              int left,
              int top,
              int right,
              int bottom,
              int oldLeft,
              int oldTop,
              int oldRight,
              int oldBottom) {
            BottomAppBar child = viewRef.get();

            
            if (child == null || !(v instanceof FloatingActionButton)) {
              v.removeOnLayoutChangeListener(this);
              return;
            }

            FloatingActionButton fab = ((FloatingActionButton) v);

            fab.getMeasuredContentRect(fabContentRect);
            int height = fabContentRect.height();

            
            child.setFabDiameter(height);

            CoordinatorLayout.LayoutParams fabLayoutParams =
                (CoordinatorLayout.LayoutParams) v.getLayoutParams();

            
            
            if (originalBottomMargin == 0) {
              
              
              int bottomShadowPadding = (fab.getMeasuredHeight() - height) / 2;
              int bottomMargin =
                  child
                      .getResources()
                      .getDimensionPixelOffset(R.dimen.mtrl_bottomappbar_fab_bottom_margin);
              
              int minBottomMargin = bottomMargin - bottomShadowPadding;
              fabLayoutParams.bottomMargin = child.getBottomInset() + minBottomMargin;
              fabLayoutParams.leftMargin = child.getLeftInset();
              fabLayoutParams.rightMargin = child.getRightInset();
              boolean isRtl =  ViewUtils.isLayoutRtl(fab);
              if (isRtl) {
                fabLayoutParams.leftMargin += child.fabOffsetEndMode;
              } else {
                fabLayoutParams.rightMargin += child.fabOffsetEndMode;
              }
            }
          }
        };

    public Behavior() {
      fabContentRect = new Rect();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      fabContentRect = new Rect();
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent, @NonNull BottomAppBar child, int layoutDirection) {
      viewRef = new WeakReference<>(child);

      View dependentView = child.findDependentView();
      if (dependentView != null && !ViewCompat.isLaidOut(dependentView)) {
        
        
        CoordinatorLayout.LayoutParams fabLayoutParams =
            (CoordinatorLayout.LayoutParams) dependentView.getLayoutParams();
        fabLayoutParams.anchorGravity = Gravity.CENTER | Gravity.TOP;

        
        
        originalBottomMargin = fabLayoutParams.bottomMargin;

        if (dependentView instanceof FloatingActionButton) {
          FloatingActionButton fab = ((FloatingActionButton) dependentView);

          
          fab.addOnLayoutChangeListener(fabLayoutListener);

          
          child.addFabAnimationListeners(fab);
        }

        
        child.setCutoutState();
      }

      
      parent.onLayoutChild(child, layoutDirection);
      return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(
        @NonNull CoordinatorLayout coordinatorLayout,
        @NonNull BottomAppBar child,
        @NonNull View directTargetChild,
        @NonNull View target,
        @ScrollAxis int axes,
        @NestedScrollType int type) {
      
      return child.getHideOnScroll()
          && super.onStartNestedScroll(
              coordinatorLayout, child, directTargetChild, target, axes, type);
    }
  }

  @NonNull
  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.fabAlignmentMode = fabAlignmentMode;
    savedState.fabAttached = fabAttached;
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    fabAlignmentMode = savedState.fabAlignmentMode;
    fabAttached = savedState.fabAttached;
  }

  static class SavedState extends AbsSavedState {
    @FabAlignmentMode int fabAlignmentMode;
    boolean fabAttached;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(@NonNull Parcel in, ClassLoader loader) {
      super(in, loader);
      fabAlignmentMode = in.readInt();
      fabAttached = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(fabAlignmentMode);
      out.writeInt(fabAttached ? 1 : 0);
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
}
