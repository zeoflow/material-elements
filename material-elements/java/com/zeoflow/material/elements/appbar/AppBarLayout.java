

package com.zeoflow.material.elements.appbar;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class AppBarLayout extends LinearLayout implements CoordinatorLayout.AttachedBehavior {

  static final int PENDING_ACTION_NONE = 0x0;
  static final int PENDING_ACTION_EXPANDED = 0x1;
  static final int PENDING_ACTION_COLLAPSED = 1 << 1;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 1 << 2;
  static final int PENDING_ACTION_FORCE = 1 << 3;

  
  
  public interface BaseOnOffsetChangedListener<T extends AppBarLayout> {

    
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  
  
  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
    void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
  }

  private static final int DEF_STYLE_RES = R.style.Widget_Design_AppBarLayout;
  private static final int INVALID_SCROLL_RANGE = -1;

  private int currentOffset;
  private int totalScrollRange = INVALID_SCROLL_RANGE;
  private int downPreScrollRange = INVALID_SCROLL_RANGE;
  private int downScrollRange = INVALID_SCROLL_RANGE;

  private boolean haveChildWithInterpolator;

  private int pendingAction = PENDING_ACTION_NONE;

  @Nullable private WindowInsetsCompat lastInsets;

  private List<BaseOnOffsetChangedListener> listeners;

  private boolean liftableOverride;
  private boolean liftable;
  private boolean lifted;

  private boolean liftOnScroll;
  @IdRes private int liftOnScrollTargetViewId;
  @Nullable private WeakReference<View> liftOnScrollTargetView;
  @Nullable private ValueAnimator elevationOverlayAnimator;

  private int[] tmpStatesArray;

  @Nullable private Drawable statusBarForeground;

  public AppBarLayout(@NonNull Context context) {
    this(context, null);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.appBarLayoutStyle);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();
    setOrientation(VERTICAL);

    if (Build.VERSION.SDK_INT >= 21) {
      
      
      ViewUtilsLollipop.setBoundsViewOutlineProvider(this);

      
      
      ViewUtilsLollipop.setStateListAnimatorFromAttrs(
          this, attrs, defStyleAttr, DEF_STYLE_RES);
    }

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.AppBarLayout,
            defStyleAttr,
            DEF_STYLE_RES);

    ViewCompat.setBackground(this, a.getDrawable(R.styleable.AppBarLayout_android_background));

    if (getBackground() instanceof ColorDrawable) {
      ColorDrawable background = (ColorDrawable) getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

    if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
      setExpanded(
          a.getBoolean(R.styleable.AppBarLayout_expanded, false),
          false, 
          false );
    }

    if (Build.VERSION.SDK_INT >= 21 && a.hasValue(R.styleable.AppBarLayout_elevation)) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(
          this, a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0));
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      
      
      if (a.hasValue(R.styleable.AppBarLayout_android_keyboardNavigationCluster)) {
        this.setKeyboardNavigationCluster(
            a.getBoolean(R.styleable.AppBarLayout_android_keyboardNavigationCluster, false));
      }
      if (a.hasValue(R.styleable.AppBarLayout_android_touchscreenBlocksFocus)) {
        this.setTouchscreenBlocksFocus(
            a.getBoolean(R.styleable.AppBarLayout_android_touchscreenBlocksFocus, false));
      }
    }

    liftOnScroll = a.getBoolean(R.styleable.AppBarLayout_liftOnScroll, false);
    liftOnScrollTargetViewId =
        a.getResourceId(R.styleable.AppBarLayout_liftOnScrollTargetViewId, View.NO_ID);

    setStatusBarForeground(a.getDrawable(R.styleable.AppBarLayout_statusBarForeground));
    a.recycle();

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });
  }

  
  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    addOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  
  
  
  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners != null && listener != null) {
      listeners.remove(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    removeOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  
  public void setStatusBarForeground(@Nullable Drawable drawable) {
    if (statusBarForeground != drawable) {
      if (statusBarForeground != null) {
        statusBarForeground.setCallback(null);
      }
      statusBarForeground = drawable != null ? drawable.mutate() : null;
      if (statusBarForeground != null) {
        if (statusBarForeground.isStateful()) {
          statusBarForeground.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarForeground, ViewCompat.getLayoutDirection(this));
        statusBarForeground.setVisible(getVisibility() == VISIBLE, false);
        statusBarForeground.setCallback(this);
      }
      updateWillNotDraw();
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  
  public void setStatusBarForegroundColor(@ColorInt int color) {
    setStatusBarForeground(new ColorDrawable(color));
  }

  
  public void setStatusBarForegroundResource(@DrawableRes int resId) {
    setStatusBarForeground(AppCompatResources.getDrawable(getContext(), resId));
  }

  
  @Nullable
  public Drawable getStatusBarForeground() {
    return statusBarForeground;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    
    if (shouldDrawStatusBarForeground()) {
      int saveCount = canvas.save();
      canvas.translate(0f, -currentOffset);
      statusBarForeground.draw(canvas);
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();

    Drawable d = statusBarForeground;
    if (d != null && d.isStateful() && d.setState(state)) {
      invalidateDrawable(d);
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == statusBarForeground;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarForeground != null) {
      statusBarForeground.setVisible(visible, false);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    
    
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode != MeasureSpec.EXACTLY
        && ViewCompat.getFitsSystemWindows(this)
        && shouldOffsetFirstChild()) {
      int newHeight = getMeasuredHeight();
      switch (heightMode) {
        case MeasureSpec.AT_MOST:
          
          newHeight =
              MathUtils.clamp(
                  getMeasuredHeight() + getTopInset(), 0, MeasureSpec.getSize(heightMeasureSpec));
          break;
        case MeasureSpec.UNSPECIFIED:
          
          newHeight += getTopInset();
          break;
        default: 
      }
      setMeasuredDimension(getMeasuredWidth(), newHeight);
    }

    invalidateScrollRanges();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
      
      final int topInset = getTopInset();
      for (int z = getChildCount() - 1; z >= 0; z--) {
        ViewCompat.offsetTopAndBottom(getChildAt(z), topInset);
      }
    }

    invalidateScrollRanges();

    haveChildWithInterpolator = false;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
      final Interpolator interpolator = childLp.getScrollInterpolator();

      if (interpolator != null) {
        haveChildWithInterpolator = true;
        break;
      }
    }

    if (statusBarForeground != null) {
      statusBarForeground.setBounds(0, 0, getWidth(), getTopInset());
    }

    
    if (!liftableOverride) {
      setLiftableState(liftOnScroll || hasCollapsibleChild());
    }
  }

  private void updateWillNotDraw() {
    setWillNotDraw(!shouldDrawStatusBarForeground());
  }

  private boolean shouldDrawStatusBarForeground() {
    return statusBarForeground != null && getTopInset() > 0;
  }

  private boolean hasCollapsibleChild() {
    for (int i = 0, z = getChildCount(); i < z; i++) {
      if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
        return true;
      }
    }
    return false;
  }

  private void invalidateScrollRanges() {
    
    totalScrollRange = INVALID_SCROLL_RANGE;
    downPreScrollRange = INVALID_SCROLL_RANGE;
    downScrollRange = INVALID_SCROLL_RANGE;
  }

  @Override
  public void setOrientation(int orientation) {
    if (orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "AppBarLayout is always vertical and does not support horizontal orientation");
    }
    super.setOrientation(orientation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<AppBarLayout> getBehavior() {
    return new AppBarLayout.Behavior();
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  
  public void setExpanded(boolean expanded) {
    setExpanded(expanded, ViewCompat.isLaidOut(this));
  }

  
  public void setExpanded(boolean expanded, boolean animate) {
    setExpanded(expanded, animate, true);
  }

  private void setExpanded(boolean expanded, boolean animate, boolean force) {
    pendingAction =
        (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
            | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0)
            | (force ? PENDING_ACTION_FORCE : 0);
    requestLayout();
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
      return new LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) p);
    }
    return new LayoutParams(p);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    clearLiftOnScrollTargetView();
  }

  boolean hasChildWithInterpolator() {
    return haveChildWithInterpolator;
  }

  
  public final int getTotalScrollRange() {
    if (totalScrollRange != INVALID_SCROLL_RANGE) {
      return totalScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        
        range += childHeight + lp.topMargin + lp.bottomMargin;

        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          
          
          range -= getTopInset();
        }
        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          
          
          
          range -= ViewCompat.getMinimumHeight(child);
          break;
        }
      } else {
        
        
        break;
      }
    }
    return totalScrollRange = Math.max(0, range);
  }

  boolean hasScrollableChildren() {
    return getTotalScrollRange() != 0;
  }

  
  int getUpNestedPreScrollRange() {
    return getTotalScrollRange();
  }

  
  int getDownNestedPreScrollRange() {
    if (downPreScrollRange != INVALID_SCROLL_RANGE) {
      
      return downPreScrollRange;
    }

    int range = 0;
    for (int i = getChildCount() - 1; i >= 0; i--) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
        
        int childRange = lp.topMargin + lp.bottomMargin;
        
        if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
          
          childRange += ViewCompat.getMinimumHeight(child);
        } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          
          childRange += childHeight - ViewCompat.getMinimumHeight(child);
        } else {
          
          childRange += childHeight;
        }
        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          
          
          childRange = Math.min(childRange, childHeight - getTopInset());
        }
        range += childRange;
      } else if (range > 0) {
        
        
        break;
      }
    }
    return downPreScrollRange = Math.max(0, range);
  }

  
  int getDownNestedScrollRange() {
    if (downScrollRange != INVALID_SCROLL_RANGE) {
      
      return downScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      int childHeight = child.getMeasuredHeight();
      childHeight += lp.topMargin + lp.bottomMargin;

      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        
        range += childHeight;

        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          
          
          
          range -= ViewCompat.getMinimumHeight(child);
          break;
        }
      } else {
        
        
        break;
      }
    }
    return downScrollRange = Math.max(0, range);
  }

  void onOffsetChanged(int offset) {
    currentOffset = offset;

    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

    
    
    if (listeners != null) {
      for (int i = 0, z = listeners.size(); i < z; i++) {
        final BaseOnOffsetChangedListener listener = listeners.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }

  public final int getMinimumHeightForVisibleOverlappingContent() {
    final int topInset = getTopInset();
    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight != 0) {
      
      return (minHeight * 2) + topInset;
    }

    
    final int childCount = getChildCount();
    final int lastChildMinHeight =
        childCount >= 1 ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) : 0;
    if (lastChildMinHeight != 0) {
      return (lastChildMinHeight * 2) + topInset;
    }

    
    
    return getHeight() / 3;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (tmpStatesArray == null) {
      
      
      tmpStatesArray = new int[4];
    }
    final int[] extraStates = tmpStatesArray;
    final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

    extraStates[0] = liftable ? R.attr.state_liftable : -R.attr.state_liftable;
    extraStates[1] = liftable && lifted ? R.attr.state_lifted : -R.attr.state_lifted;

    
    
    extraStates[2] = liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
    extraStates[3] = liftable && lifted ? R.attr.state_collapsed : -R.attr.state_collapsed;

    return mergeDrawableStates(states, extraStates);
  }

  
  public boolean setLiftable(boolean liftable) {
    this.liftableOverride = true;
    return setLiftableState(liftable);
  }

  
  private boolean setLiftableState(boolean liftable) {
    if (this.liftable != liftable) {
      this.liftable = liftable;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  
  public boolean setLifted(boolean lifted) {
    return setLiftedState(lifted);
  }

  
  boolean setLiftedState(boolean lifted) {
    if (this.lifted != lifted) {
      this.lifted = lifted;
      refreshDrawableState();
      if (liftOnScroll && getBackground() instanceof MaterialShapeDrawable) {
        startLiftOnScrollElevationOverlayAnimation((MaterialShapeDrawable) getBackground(), lifted);
      }
      return true;
    }
    return false;
  }

  private void startLiftOnScrollElevationOverlayAnimation(
      @NonNull final MaterialShapeDrawable background, boolean lifted) {
    float appBarElevation = getResources().getDimension(R.dimen.design_appbar_elevation);
    float fromElevation = lifted ? 0 : appBarElevation;
    float toElevation = lifted ? appBarElevation : 0;

    if (elevationOverlayAnimator != null) {
      elevationOverlayAnimator.cancel();
    }

    elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation);
    elevationOverlayAnimator.setDuration(
        getResources().getInteger(R.integer.app_bar_elevation_anim_duration));
    elevationOverlayAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    elevationOverlayAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            background.setElevation((float) valueAnimator.getAnimatedValue());
          }
        });
    elevationOverlayAnimator.start();
  }

  
  public void setLiftOnScroll(boolean liftOnScroll) {
    this.liftOnScroll = liftOnScroll;
  }

  
  public boolean isLiftOnScroll() {
    return liftOnScroll;
  }

  
  public void setLiftOnScrollTargetViewId(@IdRes int liftOnScrollTargetViewId) {
    this.liftOnScrollTargetViewId = liftOnScrollTargetViewId;
    
    clearLiftOnScrollTargetView();
  }

  
  @IdRes
  public int getLiftOnScrollTargetViewId() {
    return liftOnScrollTargetViewId;
  }

  boolean shouldLift(@Nullable View defaultScrollingView) {
    View scrollingView = findLiftOnScrollTargetView(defaultScrollingView);
    if (scrollingView == null) {
      scrollingView = defaultScrollingView;
    }
    return scrollingView != null
        && (scrollingView.canScrollVertically(-1) || scrollingView.getScrollY() > 0);
  }

  @Nullable
  private View findLiftOnScrollTargetView(@Nullable View defaultScrollingView) {
    if (liftOnScrollTargetView == null && liftOnScrollTargetViewId != View.NO_ID) {
      View targetView = null;
      if (defaultScrollingView != null) {
        targetView = defaultScrollingView.findViewById(liftOnScrollTargetViewId);
      }
      if (targetView == null && getParent() instanceof ViewGroup) {
        
        
        targetView = ((ViewGroup) getParent()).findViewById(liftOnScrollTargetViewId);
      }
      if (targetView != null) {
        liftOnScrollTargetView = new WeakReference<>(targetView);
      }
    }
    return liftOnScrollTargetView != null ? liftOnScrollTargetView.get() : null;
  }

  private void clearLiftOnScrollTargetView() {
    if (liftOnScrollTargetView != null) {
      liftOnScrollTargetView.clear();
    }
    liftOnScrollTargetView = null;
  }

  
  @Deprecated
  public void setTargetElevation(float elevation) {
    if (Build.VERSION.SDK_INT >= 21) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, elevation);
    }
  }

  
  @Deprecated
  public float getTargetElevation() {
    return 0;
  }

  int getPendingAction() {
    return pendingAction;
  }

  void resetPendingAction() {
    pendingAction = PENDING_ACTION_NONE;
  }

  @VisibleForTesting
  final int getTopInset() {
    return lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
  }

  
  private boolean shouldOffsetFirstChild() {
    if (getChildCount() > 0) {
      final View firstChild = getChildAt(0);
      return firstChild.getVisibility() != GONE && !ViewCompat.getFitsSystemWindows(firstChild);
    }
    return false;
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      
      newInsets = insets;
    }

    
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      updateWillNotDraw();
      requestLayout();
    }

    return insets;
  }

  
  public static class LayoutParams extends LinearLayout.LayoutParams {

    
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
        flag = true,
        value = {
          SCROLL_FLAG_NO_SCROLL,
          SCROLL_FLAG_SCROLL,
          SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
          SCROLL_FLAG_ENTER_ALWAYS,
          SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
          SCROLL_FLAG_SNAP,
          SCROLL_FLAG_SNAP_MARGINS,
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {}
    
    public static final int SCROLL_FLAG_NO_SCROLL = 0x0;

    
    public static final int SCROLL_FLAG_SCROLL = 0x1;

    
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 1 << 1;

    
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 1 << 2;

    
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 1 << 3;

    
    public static final int SCROLL_FLAG_SNAP = 1 << 4;

    
    public static final int SCROLL_FLAG_SNAP_MARGINS = 1 << 5;

    
    static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;

    static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
    static final int COLLAPSIBLE_FLAGS =
        SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;

    int scrollFlags = SCROLL_FLAG_SCROLL;
    Interpolator scrollInterpolator;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_Layout);
      scrollFlags = a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0);
      if (a.hasValue(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator)) {
        int resId = a.getResourceId(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator, 0);
        scrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
      }
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LinearLayout.LayoutParams source) {
      
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull LayoutParams source) {
      
      super(source);
      scrollFlags = source.scrollFlags;
      scrollInterpolator = source.scrollInterpolator;
    }

    
    public void setScrollFlags(@ScrollFlags int flags) {
      scrollFlags = flags;
    }

    
    @ScrollFlags
    public int getScrollFlags() {
      return scrollFlags;
    }

    
    public void setScrollInterpolator(Interpolator interpolator) {
      scrollInterpolator = interpolator;
    }

    
    public Interpolator getScrollInterpolator() {
      return scrollInterpolator;
    }

    
    boolean isCollapsible() {
      return (scrollFlags & SCROLL_FLAG_SCROLL) == SCROLL_FLAG_SCROLL
          && (scrollFlags & COLLAPSIBLE_FLAGS) != 0;
    }
  }

  
  
  public static class Behavior extends BaseBehavior<AppBarLayout> {

    
    public abstract static class DragCallback extends BaseBehavior.BaseDragCallback<AppBarLayout> {}

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  
  
  protected static class BaseBehavior<T extends AppBarLayout> extends HeaderBehavior<T> {
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600; 
    private static final int INVALID_POSITION = -1;

    
    
    public abstract static class BaseDragCallback<T extends AppBarLayout> {
      
      public abstract boolean canDrag(@NonNull T appBarLayout);
    }

    private int offsetDelta;

    @NestedScrollType private int lastStartedType;

    private ValueAnimator offsetAnimator;

    private int offsetToChildIndexOnLayout = INVALID_POSITION;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;

    @Nullable private WeakReference<View> lastNestedScrollingChildRef;
    private BaseDragCallback onDragCallback;

    public BaseBehavior() {}

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        @NonNull View directTargetChild,
        View target,
        int nestedScrollAxes,
        int type) {
      
      
      final boolean started =
          (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
              && (child.isLiftOnScroll() || canScrollChildren(parent, child, directTargetChild));

      if (started && offsetAnimator != null) {
        
        offsetAnimator.cancel();
      }

      
      lastNestedScrollingChildRef = null;

      
      lastStartedType = type;

      return started;
    }

    
    private boolean canScrollChildren(
        @NonNull CoordinatorLayout parent, @NonNull T child, @NonNull View directTargetChild) {
      return child.hasScrollableChildren()
          && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
    }

    @Override
    public void onNestedPreScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
        View target,
        int dx,
        int dy,
        int[] consumed,
        int type) {
      if (dy != 0) {
        int min;
        int max;
        if (dy < 0) {
          
          min = -child.getTotalScrollRange();
          max = min + child.getDownNestedPreScrollRange();
        } else {
          
          min = -child.getUpNestedPreScrollRange();
          max = 0;
        }
        if (min != max) {
          consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
        }
      }
      if (child.isLiftOnScroll()) {
        child.setLiftedState(child.shouldLift(target));
      }
    }

    @Override
    public void onNestedScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
        View target,
        int dxConsumed,
        int dyConsumed,
        int dxUnconsumed,
        int dyUnconsumed,
        int type,
        int[] consumed) {
      if (dyUnconsumed < 0) {
        
        
        consumed[1] =
            scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
      }

      if (dyUnconsumed == 0) {
        
        
        updateAccessibilityActions(coordinatorLayout, child);
      }
    }

    @Override
    public void onStopNestedScroll(
        CoordinatorLayout coordinatorLayout, @NonNull T abl, View target, int type) {
      
      
      
      
      if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        
        snapToChildIfNeeded(coordinatorLayout, abl);
        if (abl.isLiftOnScroll()) {
          abl.setLiftedState(abl.shouldLift(target));
        }
      }

      
      lastNestedScrollingChildRef = new WeakReference<>(target);
    }

    
    public void setDragCallback(@Nullable BaseDragCallback callback) {
      onDragCallback = callback;
    }

    private void animateOffsetTo(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T child,
        final int offset,
        float velocity) {
      final int distance = Math.abs(getTopBottomOffsetForScrollingSibling() - offset);

      final int duration;
      velocity = Math.abs(velocity);
      if (velocity > 0) {
        duration = 3 * Math.round(1000 * (distance / velocity));
      } else {
        final float distanceRatio = (float) distance / child.getHeight();
        duration = (int) ((distanceRatio + 1) * 150);
      }

      animateOffsetWithDuration(coordinatorLayout, child, offset, duration);
    }

    private void animateOffsetWithDuration(
        final CoordinatorLayout coordinatorLayout,
        final T child,
        final int offset,
        final int duration) {
      final int currentOffset = getTopBottomOffsetForScrollingSibling();
      if (currentOffset == offset) {
        if (offsetAnimator != null && offsetAnimator.isRunning()) {
          offsetAnimator.cancel();
        }
        return;
      }

      if (offsetAnimator == null) {
        offsetAnimator = new ValueAnimator();
        offsetAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
        offsetAnimator.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
              @Override
              public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                setHeaderTopBottomOffset(
                    coordinatorLayout, child, (int) animator.getAnimatedValue());
              }
            });
      } else {
        offsetAnimator.cancel();
      }

      offsetAnimator.setDuration(Math.min(duration, MAX_OFFSET_ANIMATION_DURATION));
      offsetAnimator.setIntValues(currentOffset, offset);
      offsetAnimator.start();
    }

    private int getChildIndexOnOffset(@NonNull T abl, final int offset) {
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        int top = child.getTop();
        int bottom = child.getBottom();

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (checkFlag(lp.getScrollFlags(), LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
          
          top -= lp.topMargin;
          bottom += lp.bottomMargin;
        }

        if (top <= -offset && bottom >= -offset) {
          return i;
        }
      }
      return -1;
    }

    private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, @NonNull T abl) {
      final int offset = getTopBottomOffsetForScrollingSibling();
      final int offsetChildIndex = getChildIndexOnOffset(abl, offset);
      if (offsetChildIndex >= 0) {
        final View offsetChild = abl.getChildAt(offsetChildIndex);
        final LayoutParams lp = (LayoutParams) offsetChild.getLayoutParams();
        final int flags = lp.getScrollFlags();

        if ((flags & LayoutParams.FLAG_SNAP) == LayoutParams.FLAG_SNAP) {
          
          int snapTop = -offsetChild.getTop();
          int snapBottom = -offsetChild.getBottom();

          if (offsetChildIndex == abl.getChildCount() - 1) {
            
            snapBottom += abl.getTopInset();
          }

          if (checkFlag(flags, LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)) {
            
            snapBottom += ViewCompat.getMinimumHeight(offsetChild);
          } else if (checkFlag(
              flags, LayoutParams.FLAG_QUICK_RETURN | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)) {
            
            
            final int seam = snapBottom + ViewCompat.getMinimumHeight(offsetChild);
            if (offset < seam) {
              snapTop = seam;
            } else {
              snapBottom = seam;
            }
          }

          if (checkFlag(flags, LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
            
            snapTop += lp.topMargin;
            snapBottom -= lp.bottomMargin;
          }

          final int newOffset = offset < (snapBottom + snapTop) / 2 ? snapBottom : snapTop;
          animateOffsetTo(
              coordinatorLayout, abl, MathUtils.clamp(newOffset, -abl.getTotalScrollRange(), 0), 0);
        }
      }
    }

    private static boolean checkFlag(final int flags, final int check) {
      return (flags & check) == check;
    }

    @Override
    public boolean onMeasureChild(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        int parentWidthMeasureSpec,
        int widthUsed,
        int parentHeightMeasureSpec,
        int heightUsed) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (lp.height == CoordinatorLayout.LayoutParams.WRAP_CONTENT) {
        
        
        
        
        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            heightUsed);
        return true;
      }

      
      return super.onMeasureChild(
          parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent, @NonNull T abl, int layoutDirection) {
      boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

      
      
      
      
      final int pendingAction = abl.getPendingAction();
      if (offsetToChildIndexOnLayout >= 0 && (pendingAction & PENDING_ACTION_FORCE) == 0) {
        View child = abl.getChildAt(offsetToChildIndexOnLayout);
        int offset = -child.getBottom();
        if (offsetToChildIndexOnLayoutIsMinHeight) {
          offset += ViewCompat.getMinimumHeight(child) + abl.getTopInset();
        } else {
          offset += Math.round(child.getHeight() * offsetToChildIndexOnLayoutPerc);
        }
        setHeaderTopBottomOffset(parent, abl, offset);
      } else if (pendingAction != PENDING_ACTION_NONE) {
        final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
        if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
          final int offset = -abl.getUpNestedPreScrollRange();
          if (animate) {
            animateOffsetTo(parent, abl, offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, offset);
          }
        } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
          if (animate) {
            animateOffsetTo(parent, abl, 0, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, 0);
          }
        }
      }

      
      abl.resetPendingAction();
      offsetToChildIndexOnLayout = INVALID_POSITION;

      
      
      setTopAndBottomOffset(
          MathUtils.clamp(getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));

      
      
      
      updateAppBarLayoutDrawableState(
          parent, abl, getTopAndBottomOffset(), 0 , true );

      
      abl.onOffsetChanged(getTopAndBottomOffset());

      updateAccessibilityActions(parent, abl);
      return handled;
    }

    private void updateAccessibilityActions(
        CoordinatorLayout coordinatorLayout, @NonNull T appBarLayout) {
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_FORWARD.getId());
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_BACKWARD.getId());
      View scrollingView = findFirstScrollingChild(coordinatorLayout);
      
      
      if (scrollingView == null || appBarLayout.getTotalScrollRange() == 0) {
        return;
      }
      
      
      CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) scrollingView.getLayoutParams();
      if (!(lp.getBehavior() instanceof ScrollingViewBehavior)) {
        return;
      }
      addAccessibilityScrollActions(coordinatorLayout, appBarLayout, scrollingView);
    }

    private void addAccessibilityScrollActions(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T appBarLayout,
        @NonNull final View scrollingView) {
      if (getTopBottomOffsetForScrollingSibling() != -appBarLayout.getTotalScrollRange()
          && scrollingView.canScrollVertically(1)) {
        
        
        addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_FORWARD, false);
      }
      
      
      if (getTopBottomOffsetForScrollingSibling() != 0) {
        if (scrollingView.canScrollVertically(-1)) {
          
          
          final int dy = -appBarLayout.getDownNestedPreScrollRange();
          
          if (dy != 0) {
            ViewCompat.replaceAccessibilityAction(
                coordinatorLayout,
                ACTION_SCROLL_BACKWARD,
                null,
                new AccessibilityViewCommand() {
                  @Override
                  public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
                    onNestedPreScroll(
                        coordinatorLayout,
                        appBarLayout,
                        scrollingView,
                        0,
                        dy,
                        new int[] {0, 0},
                        ViewCompat.TYPE_NON_TOUCH);
                    return true;
                  }
                });
          }
        } else {
          
          
          addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_BACKWARD, true);
        }
      }
    }

    private void addActionToExpand(
        CoordinatorLayout parent,
        @NonNull final T appBarLayout,
        @NonNull AccessibilityActionCompat action,
        final boolean expand) {
      ViewCompat.replaceAccessibilityAction(
          parent,
          action,
          null,
          new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
              appBarLayout.setExpanded(expand);
              return true;
            }
          });
    }

    @Override
    boolean canDragView(T view) {
      if (onDragCallback != null) {
        
        return onDragCallback.canDrag(view);
      }

      
      if (lastNestedScrollingChildRef != null) {
        
        final View scrollingView = lastNestedScrollingChildRef.get();
        return scrollingView != null
            && scrollingView.isShown()
            && !scrollingView.canScrollVertically(-1);
      } else {
        
        return true;
      }
    }

    @Override
    void onFlingFinished(@NonNull CoordinatorLayout parent, @NonNull T layout) {
      
      snapToChildIfNeeded(parent, layout);
      if (layout.isLiftOnScroll()) {
        layout.setLiftedState(layout.shouldLift(findFirstScrollingChild(parent)));
      }
    }

    @Override
    int getMaxDragOffset(@NonNull T view) {
      return -view.getDownNestedScrollRange();
    }

    @Override
    int getScrollRangeForDragFling(@NonNull T view) {
      return view.getTotalScrollRange();
    }

    @Override
    int setHeaderTopBottomOffset(
        @NonNull CoordinatorLayout coordinatorLayout,
        @NonNull T appBarLayout,
        int newOffset,
        int minOffset,
        int maxOffset) {
      final int curOffset = getTopBottomOffsetForScrollingSibling();
      int consumed = 0;

      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        
        
        newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
        if (curOffset != newOffset) {
          final int interpolatedOffset =
              appBarLayout.hasChildWithInterpolator()
                  ? interpolateOffset(appBarLayout, newOffset)
                  : newOffset;

          final boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

          
          consumed = curOffset - newOffset;
          
          offsetDelta = newOffset - interpolatedOffset;

          if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
            
            
            
            
            coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
          }

          
          appBarLayout.onOffsetChanged(getTopAndBottomOffset());

          
          updateAppBarLayoutDrawableState(
              coordinatorLayout,
              appBarLayout,
              newOffset,
              newOffset < curOffset ? -1 : 1,
              false );
        }
      } else {
        
        offsetDelta = 0;
      }

      updateAccessibilityActions(coordinatorLayout, appBarLayout);
      return consumed;
    }

    @VisibleForTesting
    boolean isOffsetAnimatorRunning() {
      return offsetAnimator != null && offsetAnimator.isRunning();
    }

    private int interpolateOffset(@NonNull T layout, final int offset) {
      final int absOffset = Math.abs(offset);

      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final Interpolator interpolator = childLp.getScrollInterpolator();

        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          if (interpolator != null) {
            int childScrollableHeight = 0;
            final int flags = childLp.getScrollFlags();
            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
              
              childScrollableHeight += child.getHeight() + childLp.topMargin + childLp.bottomMargin;

              if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                
                
                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
              }
            }

            if (ViewCompat.getFitsSystemWindows(child)) {
              childScrollableHeight -= layout.getTopInset();
            }

            if (childScrollableHeight > 0) {
              final int offsetForView = absOffset - child.getTop();
              final int interpolatedDiff =
                  Math.round(
                      childScrollableHeight
                          * interpolator.getInterpolation(
                              offsetForView / (float) childScrollableHeight));

              return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
            }
          }

          
          
          break;
        }
      }

      return offset;
    }

    private void updateAppBarLayoutDrawableState(
        @NonNull final CoordinatorLayout parent,
        @NonNull final T layout,
        final int offset,
        final int direction,
        final boolean forceJump) {
      final View child = getAppBarChildOnOffset(layout, offset);
      if (child != null) {
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final int flags = childLp.getScrollFlags();
        boolean lifted = false;

        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
          final int minHeight = ViewCompat.getMinimumHeight(child);

          if (direction > 0
              && (flags
                      & (LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                          | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED))
                  != 0) {
            
            
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
            
            
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          }
        }

        if (layout.isLiftOnScroll()) {
          
          
          lifted = layout.shouldLift(findFirstScrollingChild(parent));
        }

        final boolean changed = layout.setLiftedState(lifted);

        if (forceJump || (changed && shouldJumpElevationState(parent, layout))) {
          
          
          layout.jumpDrawablesToCurrentState();
        }
      }
    }

    private boolean shouldJumpElevationState(@NonNull CoordinatorLayout parent, @NonNull T layout) {
      
      
      final List<View> dependencies = parent.getDependents(layout);
      for (int i = 0, size = dependencies.size(); i < size; i++) {
        final View dependency = dependencies.get(i);
        final CoordinatorLayout.LayoutParams lp =
            (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        final CoordinatorLayout.Behavior behavior = lp.getBehavior();

        if (behavior instanceof ScrollingViewBehavior) {
          return ((ScrollingViewBehavior) behavior).getOverlayTop() != 0;
        }
      }
      return false;
    }

    @Nullable
    private static View getAppBarChildOnOffset(
        @NonNull final AppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);
      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          return child;
        }
      }
      return null;
    }

    @Nullable
    private View findFirstScrollingChild(@NonNull CoordinatorLayout parent) {
      for (int i = 0, z = parent.getChildCount(); i < z; i++) {
        final View child = parent.getChildAt(i);
        if (child instanceof NestedScrollingChild
            || child instanceof ListView
            || child instanceof ScrollView) {
          return child;
        }
      }
      return null;
    }

    @Override
    int getTopBottomOffsetForScrollingSibling() {
      return getTopAndBottomOffset() + offsetDelta;
    }

    @Override
    public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull T abl) {
      final Parcelable superState = super.onSaveInstanceState(parent, abl);
      final int offset = getTopAndBottomOffset();

      
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        final int visBottom = child.getBottom() + offset;

        if (child.getTop() + offset <= 0 && visBottom >= 0) {
          final SavedState ss = new SavedState(superState);
          ss.firstVisibleChildIndex = i;
          ss.firstVisibleChildAtMinimumHeight =
              visBottom == (ViewCompat.getMinimumHeight(child) + abl.getTopInset());
          ss.firstVisibleChildPercentageShown = visBottom / (float) child.getHeight();
          return ss;
        }
      }

      
      return superState;
    }

    @Override
    public void onRestoreInstanceState(
        @NonNull CoordinatorLayout parent, @NonNull T appBarLayout, Parcelable state) {
      if (state instanceof SavedState) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
        offsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
        offsetToChildIndexOnLayoutPerc = ss.firstVisibleChildPercentageShown;
        offsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibleChildAtMinimumHeight;
      } else {
        super.onRestoreInstanceState(parent, appBarLayout, state);
        offsetToChildIndexOnLayout = INVALID_POSITION;
      }
    }

    
    protected static class SavedState extends AbsSavedState {
      int firstVisibleChildIndex;
      float firstVisibleChildPercentageShown;
      boolean firstVisibleChildAtMinimumHeight;

      public SavedState(@NonNull Parcel source, ClassLoader loader) {
        super(source, loader);
        firstVisibleChildIndex = source.readInt();
        firstVisibleChildPercentageShown = source.readFloat();
        firstVisibleChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      @Override
      public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(firstVisibleChildIndex);
        dest.writeFloat(firstVisibleChildPercentageShown);
        dest.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
      }

      public static final Creator<SavedState> CREATOR =
          new ClassLoaderCreator<SavedState>() {
            @NonNull
            @Override
            public SavedState createFromParcel(@NonNull Parcel source, ClassLoader loader) {
              return new SavedState(source, loader);
            }

            @Nullable
            @Override
            public SavedState createFromParcel(@NonNull Parcel source) {
              return new SavedState(source, null);
            }

            @NonNull
            @Override
            public SavedState[] newArray(int size) {
              return new SavedState[size];
            }
          };
    }
  }

  
  public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {

    public ScrollingViewBehavior() {}

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);

      final TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
      setOverlayTop(
          a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
      a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
      
      return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      offsetChildAsNeeded(child, dependency);
      updateLiftedStateIfNeeded(child, dependency);
      return false;
    }

    @Override
    public void onDependentViewRemoved(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      if (dependency instanceof AppBarLayout) {
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_FORWARD.getId());
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_BACKWARD.getId());
      }
    }

    @Override
    public boolean onRequestChildRectangleOnScreen(
        @NonNull CoordinatorLayout parent,
        @NonNull View child,
        @NonNull Rect rectangle,
        boolean immediate) {
      final AppBarLayout header = findFirstDependency(parent.getDependencies(child));
      if (header != null) {
        
        rectangle.offset(child.getLeft(), child.getTop());

        final Rect parentRect = tempRect1;
        parentRect.set(0, 0, parent.getWidth(), parent.getHeight());

        if (!parentRect.contains(rectangle)) {
          
          
          header.setExpanded(false, !immediate);
          return true;
        }
      }
      return false;
    }

    private void offsetChildAsNeeded(@NonNull View child, @NonNull View dependency) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        
        
        final BaseBehavior ablBehavior = (BaseBehavior) behavior;
        ViewCompat.offsetTopAndBottom(
            child,
            (dependency.getBottom() - child.getTop())
                + ablBehavior.offsetDelta
                + getVerticalLayoutGap()
                - getOverlapPixelsForOffset(dependency));
      }
    }

    @Override
    float getOverlapRatioForOffset(final View header) {
      if (header instanceof AppBarLayout) {
        final AppBarLayout abl = (AppBarLayout) header;
        final int totalScrollRange = abl.getTotalScrollRange();
        final int preScrollDown = abl.getDownNestedPreScrollRange();
        final int offset = getAppBarLayoutOffset(abl);

        if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
          
          return 0;
        } else {
          final int availScrollRange = totalScrollRange - preScrollDown;
          if (availScrollRange != 0) {
            
            return 1f + (offset / (float) availScrollRange);
          }
        }
      }
      return 0f;
    }

    private static int getAppBarLayoutOffset(@NonNull AppBarLayout abl) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        return ((BaseBehavior) behavior).getTopBottomOffsetForScrollingSibling();
      }
      return 0;
    }

    @Nullable
    @Override
    AppBarLayout findFirstDependency(@NonNull List<View> views) {
      for (int i = 0, z = views.size(); i < z; i++) {
        View view = views.get(i);
        if (view instanceof AppBarLayout) {
          return (AppBarLayout) view;
        }
      }
      return null;
    }

    @Override
    int getScrollRange(View v) {
      if (v instanceof AppBarLayout) {
        return ((AppBarLayout) v).getTotalScrollRange();
      } else {
        return super.getScrollRange(v);
      }
    }

    private void updateLiftedStateIfNeeded(View child, View dependency) {
      if (dependency instanceof AppBarLayout) {
        AppBarLayout appBarLayout = (AppBarLayout) dependency;
        if (appBarLayout.isLiftOnScroll()) {
          appBarLayout.setLiftedState(appBarLayout.shouldLift(child));
        }
      }
    }
  }
}
