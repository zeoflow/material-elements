

package com.zeoflow.material.elements.bottomsheet;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.R;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.internal.ViewUtils.RelativePadding;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  
  public abstract static class BottomSheetCallback {

    
    public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

    
    public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
  }

  
  public static final int STATE_DRAGGING = 1;

  
  public static final int STATE_SETTLING = 2;

  
  public static final int STATE_EXPANDED = 3;

  
  public static final int STATE_COLLAPSED = 4;

  
  public static final int STATE_HIDDEN = 5;

  
  public static final int STATE_HALF_EXPANDED = 6;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    STATE_EXPANDED,
    STATE_COLLAPSED,
    STATE_DRAGGING,
    STATE_SETTLING,
    STATE_HIDDEN,
    STATE_HALF_EXPANDED
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {}

  
  public static final int PEEK_HEIGHT_AUTO = -1;

  
  public static final int SAVE_PEEK_HEIGHT = 0x1;

  
  public static final int SAVE_FIT_TO_CONTENTS = 1 << 1;

  
  public static final int SAVE_HIDEABLE = 1 << 2;

  
  public static final int SAVE_SKIP_COLLAPSED = 1 << 3;

  
  public static final int SAVE_ALL = -1;

  
  public static final int SAVE_NONE = 0;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      flag = true,
      value = {
        SAVE_PEEK_HEIGHT,
        SAVE_FIT_TO_CONTENTS,
        SAVE_HIDEABLE,
        SAVE_SKIP_COLLAPSED,
        SAVE_ALL,
        SAVE_NONE,
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface SaveFlags {}

  private static final String TAG = "BottomSheetBehavior";

  @SaveFlags private int saveFlags = SAVE_NONE;

  private static final int SIGNIFICANT_VEL_THRESHOLD = 500;

  private static final float HIDE_THRESHOLD = 0.5f;

  private static final float HIDE_FRICTION = 0.1f;

  private static final int CORNER_ANIMATION_DURATION = 500;

  private boolean fitToContents = true;

  private boolean updateImportantForAccessibilityOnSiblings = false;

  private float maximumVelocity;

  
  private int peekHeight;

  
  private boolean peekHeightAuto;

  
  private int peekHeightMin;

  
  private boolean shapeThemingEnabled;

  private MaterialShapeDrawable materialShapeDrawable;

  private int gestureInsetBottom;
  private boolean gestureInsetBottomIgnored;

  
  private ShapeAppearanceModel shapeAppearanceModelDefault;

  private boolean isShapeExpanded;

  private SettleRunnable settleRunnable = null;

  @Nullable private ValueAnimator interpolatorAnimator;

  private static final int DEF_STYLE_RES = R.style.Widget_Design_BottomSheet_Modal;

  int expandedOffset;

  int fitToContentsOffset;

  int halfExpandedOffset;

  float halfExpandedRatio = 0.5f;

  int collapsedOffset;

  float elevation = -1;

  boolean hideable;

  private boolean skipCollapsed;

  private boolean draggable = true;

  @State int state = STATE_COLLAPSED;

  @Nullable ViewDragHelper viewDragHelper;

  private boolean ignoreEvents;

  private int lastNestedScrollDy;

  private boolean nestedScrolled;

  int parentWidth;
  int parentHeight;

  @Nullable WeakReference<V> viewRef;

  @Nullable WeakReference<View> nestedScrollingChildRef;

  @NonNull private final ArrayList<BottomSheetCallback> callbacks = new ArrayList<>();

  @Nullable private VelocityTracker velocityTracker;

  int activePointerId;

  private int initialY;

  boolean touchingScrollingChild;

  @Nullable private Map<View, Integer> importantForAccessibilityMap;

  public BottomSheetBehavior() {}

  public BottomSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout);
    this.shapeThemingEnabled = a.hasValue(R.styleable.BottomSheetBehavior_Layout_shapeAppearance);
    boolean hasBackgroundTint = a.hasValue(R.styleable.BottomSheetBehavior_Layout_backgroundTint);
    if (hasBackgroundTint) {
      ColorStateList bottomSheetColor =
          MaterialResources.getColorStateList(
              context, a, R.styleable.BottomSheetBehavior_Layout_backgroundTint);
      createMaterialShapeDrawable(context, attrs, hasBackgroundTint, bottomSheetColor);
    } else {
      createMaterialShapeDrawable(context, attrs, hasBackgroundTint);
    }
    createShapeValueAnimator();

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      this.elevation = a.getDimension(R.styleable.BottomSheetBehavior_Layout_android_elevation, -1);
    }

    TypedValue value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight);
    if (value != null && value.data == PEEK_HEIGHT_AUTO) {
      setPeekHeight(value.data);
    } else {
      setPeekHeight(
          a.getDimensionPixelSize(
              R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO));
    }
    setHideable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
    setGestureInsetBottomIgnored(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_gestureInsetBottomIgnored, false));
    setFitToContents(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true));
    setSkipCollapsed(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false));
    setDraggable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_draggable, true));
    setSaveFlags(a.getInt(R.styleable.BottomSheetBehavior_Layout_behavior_saveFlags, SAVE_NONE));
    setHalfExpandedRatio(
        a.getFloat(R.styleable.BottomSheetBehavior_Layout_behavior_halfExpandedRatio, 0.5f));

    value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset);
    if (value != null && value.type == TypedValue.TYPE_FIRST_INT) {
      setExpandedOffset(value.data);
    } else {
      setExpandedOffset(
          a.getDimensionPixelOffset(
              R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset, 0));
    }
    a.recycle();
    ViewConfiguration configuration = ViewConfiguration.get(context);
    maximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull V child) {
    return new SavedState(super.onSaveInstanceState(parent, child), this);
  }

  @Override
  public void onRestoreInstanceState(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(parent, child, ss.getSuperState());
    
    restoreOptionalState(ss);
    
    if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
      this.state = STATE_COLLAPSED;
    } else {
      this.state = ss.state;
    }
  }

  @Override
  public void onAttachedToLayoutParams(@NonNull LayoutParams layoutParams) {
    super.onAttachedToLayoutParams(layoutParams);
    
    
    viewRef = null;
    viewDragHelper = null;
  }

  @Override
  public void onDetachedFromLayoutParams() {
    super.onDetachedFromLayoutParams();
    
    viewRef = null;
    viewDragHelper = null;
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
      child.setFitsSystemWindows(true);
    }

    if (viewRef == null) {
      
      peekHeightMin =
          parent.getResources().getDimensionPixelSize(R.dimen.design_bottom_sheet_peek_height_min);
      setSystemGestureInsets(child);
      viewRef = new WeakReference<>(child);
      
      
      if (shapeThemingEnabled && materialShapeDrawable != null) {
        ViewCompat.setBackground(child, materialShapeDrawable);
      }
      
      if (materialShapeDrawable != null) {
        
        materialShapeDrawable.setElevation(
            elevation == -1 ? ViewCompat.getElevation(child) : elevation);
        
        isShapeExpanded = state == STATE_EXPANDED;
        materialShapeDrawable.setInterpolation(isShapeExpanded ? 0f : 1f);
      }
      updateAccessibilityActions();
      if (ViewCompat.getImportantForAccessibility(child)
          == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
      }
    }
    if (viewDragHelper == null) {
      viewDragHelper = ViewDragHelper.create(parent, dragCallback);
    }

    int savedTop = child.getTop();
    
    parent.onLayoutChild(child, layoutDirection);
    
    parentWidth = parent.getWidth();
    parentHeight = parent.getHeight();
    fitToContentsOffset = Math.max(0, parentHeight - child.getHeight());
    calculateHalfExpandedOffset();
    calculateCollapsedOffset();

    if (state == STATE_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, getExpandedOffset());
    } else if (state == STATE_HALF_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, halfExpandedOffset);
    } else if (hideable && state == STATE_HIDDEN) {
      ViewCompat.offsetTopAndBottom(child, parentHeight);
    } else if (state == STATE_COLLAPSED) {
      ViewCompat.offsetTopAndBottom(child, collapsedOffset);
    } else if (state == STATE_DRAGGING || state == STATE_SETTLING) {
      ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
    }

    nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown() || !draggable) {
      ignoreEvents = true;
      return false;
    }
    int action = event.getActionMasked();
    
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    switch (action) {
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        touchingScrollingChild = false;
        activePointerId = MotionEvent.INVALID_POINTER_ID;
        
        if (ignoreEvents) {
          ignoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        int initialX = (int) event.getX();
        initialY = (int) event.getY();
        
        
        if (state != STATE_SETTLING) {
          View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
          if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
            activePointerId = event.getPointerId(event.getActionIndex());
            touchingScrollingChild = true;
          }
        }
        ignoreEvents =
            activePointerId == MotionEvent.INVALID_POINTER_ID
                && !parent.isPointInChildBounds(child, initialX, initialY);
        break;
      default: 
    }
    if (!ignoreEvents
        && viewDragHelper != null
        && viewDragHelper.shouldInterceptTouchEvent(event)) {
      return true;
    }
    
    
    
    View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
    return action == MotionEvent.ACTION_MOVE
        && scroll != null
        && !ignoreEvents
        && state != STATE_DRAGGING
        && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY())
        && viewDragHelper != null
        && Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop();
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }
    int action = event.getActionMasked();
    if (state == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
      return true;
    }
    if (viewDragHelper != null) {
      viewDragHelper.processTouchEvent(event);
    }
    
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    
    
    if (action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
      if (Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop()) {
        viewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !ignoreEvents;
  }

  @Override
  public boolean onStartNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View directTargetChild,
      @NonNull View target,
      int axes,
      int type) {
    lastNestedScrollDy = 0;
    nestedScrolled = false;
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dx,
      int dy,
      @NonNull int[] consumed,
      int type) {
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      
      return;
    }
    View scrollingChild = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
    if (target != scrollingChild) {
      return;
    }
    int currentTop = child.getTop();
    int newTop = currentTop - dy;
    if (dy > 0) { 
      if (newTop < getExpandedOffset()) {
        consumed[1] = currentTop - getExpandedOffset();
        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
        setStateInternal(STATE_EXPANDED);
      } else {
        if (!draggable) {
          
          return;
        }

        consumed[1] = dy;
        ViewCompat.offsetTopAndBottom(child, -dy);
        setStateInternal(STATE_DRAGGING);
      }
    } else if (dy < 0) { 
      if (!target.canScrollVertically(-1)) {
        if (newTop <= collapsedOffset || hideable) {
          if (!draggable) {
            
            return;
          }

          consumed[1] = dy;
          ViewCompat.offsetTopAndBottom(child, -dy);
          setStateInternal(STATE_DRAGGING);
        } else {
          consumed[1] = currentTop - collapsedOffset;
          ViewCompat.offsetTopAndBottom(child, -consumed[1]);
          setStateInternal(STATE_COLLAPSED);
        }
      }
    }
    dispatchOnSlide(child.getTop());
    lastNestedScrollDy = dy;
    nestedScrolled = true;
  }

  @Override
  public void onStopNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int type) {
    if (child.getTop() == getExpandedOffset()) {
      setStateInternal(STATE_EXPANDED);
      return;
    }
    if (nestedScrollingChildRef == null
        || target != nestedScrollingChildRef.get()
        || !nestedScrolled) {
      return;
    }
    int top;
    int targetState;
    if (lastNestedScrollDy > 0) {
      if (fitToContents) {
        top = fitToContentsOffset;
        targetState = STATE_EXPANDED;
      } else {
        int currentTop = child.getTop();
        if (currentTop > halfExpandedOffset) {
          top = halfExpandedOffset;
          targetState = STATE_HALF_EXPANDED;
        } else {
          top = expandedOffset;
          targetState = STATE_EXPANDED;
        }
      }
    } else if (hideable && shouldHide(child, getYVelocity())) {
      top = parentHeight;
      targetState = STATE_HIDDEN;
    } else if (lastNestedScrollDy == 0) {
      int currentTop = child.getTop();
      if (fitToContents) {
        if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
          top = fitToContentsOffset;
          targetState = STATE_EXPANDED;
        } else {
          top = collapsedOffset;
          targetState = STATE_COLLAPSED;
        }
      } else {
        if (currentTop < halfExpandedOffset) {
          if (currentTop < Math.abs(currentTop - collapsedOffset)) {
            top = expandedOffset;
            targetState = STATE_EXPANDED;
          } else {
            top = halfExpandedOffset;
            targetState = STATE_HALF_EXPANDED;
          }
        } else {
          if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
            top = halfExpandedOffset;
            targetState = STATE_HALF_EXPANDED;
          } else {
            top = collapsedOffset;
            targetState = STATE_COLLAPSED;
          }
        }
      }
    } else {
      if (fitToContents) {
        top = collapsedOffset;
        targetState = STATE_COLLAPSED;
      } else {
        
        int currentTop = child.getTop();
        if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
          top = halfExpandedOffset;
          targetState = STATE_HALF_EXPANDED;
        } else {
          top = collapsedOffset;
          targetState = STATE_COLLAPSED;
        }
      }
    }
    startSettlingAnimation(child, targetState, top, false);
    nestedScrolled = false;
  }

  @Override
  public void onNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed,
      int type,
      @NonNull int[] consumed) {
    
  }

  @Override
  public boolean onNestedPreFling(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      float velocityX,
      float velocityY) {
    if (nestedScrollingChildRef != null) {
      return target == nestedScrollingChildRef.get()
          && (state != STATE_EXPANDED
              || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    } else {
      return false;
    }
  }

  
  public boolean isFitToContents() {
    return fitToContents;
  }

  
  public void setFitToContents(boolean fitToContents) {
    if (this.fitToContents == fitToContents) {
      return;
    }
    this.fitToContents = fitToContents;

    
    
    if (viewRef != null) {
      calculateCollapsedOffset();
    }
    
    setStateInternal((this.fitToContents && state == STATE_HALF_EXPANDED) ? STATE_EXPANDED : state);

    updateAccessibilityActions();
  }

  
  public void setPeekHeight(int peekHeight) {
    setPeekHeight(peekHeight, false);
  }

  
  public final void setPeekHeight(int peekHeight, boolean animate) {
    boolean layout = false;
    if (peekHeight == PEEK_HEIGHT_AUTO) {
      if (!peekHeightAuto) {
        peekHeightAuto = true;
        layout = true;
      }
    } else if (peekHeightAuto || this.peekHeight != peekHeight) {
      peekHeightAuto = false;
      this.peekHeight = Math.max(0, peekHeight);
      layout = true;
    }
    
    
    if (layout) {
      updatePeekHeight(animate);
    }
  }

  private void updatePeekHeight(boolean animate) {
    if (viewRef != null) {
      calculateCollapsedOffset();
      if (state == STATE_COLLAPSED) {
        V view = viewRef.get();
        if (view != null) {
          if (animate) {
            settleToStatePendingLayout(state);
          } else {
            view.requestLayout();
          }
        }
      }
    }
  }

  
  public int getPeekHeight() {
    return peekHeightAuto ? PEEK_HEIGHT_AUTO : peekHeight;
  }

  
  public void setHalfExpandedRatio(@FloatRange(from = 0.0f, to = 1.0f) float ratio) {

    if ((ratio <= 0) || (ratio >= 1)) {
      throw new IllegalArgumentException("ratio must be a float value between 0 and 1");
    }
    this.halfExpandedRatio = ratio;
    
    
    if (viewRef != null) {
      calculateHalfExpandedOffset();
    }
  }

  
  @FloatRange(from = 0.0f, to = 1.0f)
  public float getHalfExpandedRatio() {
    return halfExpandedRatio;
  }

  
  public void setExpandedOffset(int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be greater than or equal to 0");
    }
    this.expandedOffset = offset;
  }

  
  public int getExpandedOffset() {
    return fitToContents ? fitToContentsOffset : expandedOffset;
  }

  
  public void setHideable(boolean hideable) {
    if (this.hideable != hideable) {
      this.hideable = hideable;
      if (!hideable && state == STATE_HIDDEN) {
        
        setState(STATE_COLLAPSED);
      }
      updateAccessibilityActions();
    }
  }

  
  public boolean isHideable() {
    return hideable;
  }

  
  public void setSkipCollapsed(boolean skipCollapsed) {
    this.skipCollapsed = skipCollapsed;
  }

  
  public boolean getSkipCollapsed() {
    return skipCollapsed;
  }

  
  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  public boolean isDraggable() {
    return draggable;
  }

  
  public void setSaveFlags(@SaveFlags int flags) {
    this.saveFlags = flags;
  }
  
  @SaveFlags
  public int getSaveFlags() {
    return this.saveFlags;
  }

  
  @Deprecated
  public void setBottomSheetCallback(BottomSheetCallback callback) {
    Log.w(
        TAG,
        "BottomSheetBehavior now supports multiple callbacks. `setBottomSheetCallback()` removes"
            + " all existing callbacks, including ones set internally by library authors, which"
            + " may result in unintended behavior. This may change in the future. Please use"
            + " `addBottomSheetCallback()` and `removeBottomSheetCallback()` instead to set your"
            + " own callbacks.");
    callbacks.clear();
    if (callback != null) {
      callbacks.add(callback);
    }
  }

  
  public void addBottomSheetCallback(@NonNull BottomSheetCallback callback) {
    if (!callbacks.contains(callback)) {
      callbacks.add(callback);
    }
  }

  
  public void removeBottomSheetCallback(@NonNull BottomSheetCallback callback) {
    callbacks.remove(callback);
  }

  
  public void setState(@State int state) {
    if (state == this.state) {
      return;
    }
    if (viewRef == null) {
      
      if (state == STATE_COLLAPSED
          || state == STATE_EXPANDED
          || state == STATE_HALF_EXPANDED
          || (hideable && state == STATE_HIDDEN)) {
        this.state = state;
      }
      return;
    }
    settleToStatePendingLayout(state);
  }

  
  public void setGestureInsetBottomIgnored(boolean gestureInsetBottomIgnored) {
    this.gestureInsetBottomIgnored = gestureInsetBottomIgnored;
  }

  
  public boolean isGestureInsetBottomIgnored() {
    return gestureInsetBottomIgnored;
  }

  private void settleToStatePendingLayout(@State int state) {
    final V child = viewRef.get();
    if (child == null) {
      return;
    }
    
    ViewParent parent = child.getParent();
    if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child)) {
      final int finalState = state;
      child.post(
          new Runnable() {
            @Override
            public void run() {
              settleToState(child, finalState);
            }
          });
    } else {
      settleToState(child, state);
    }
  }

  
  @State
  public int getState() {
    return state;
  }

  void setStateInternal(@State int state) {
    if (this.state == state) {
      return;
    }
    this.state = state;

    if (viewRef == null) {
      return;
    }

    View bottomSheet = viewRef.get();
    if (bottomSheet == null) {
      return;
    }

    if (state == STATE_EXPANDED) {
      updateImportantForAccessibility(true);
    } else if (state == STATE_HALF_EXPANDED || state == STATE_HIDDEN || state == STATE_COLLAPSED) {
      updateImportantForAccessibility(false);
    }

    updateDrawableForTargetState(state);
    for (int i = 0; i < callbacks.size(); i++) {
      callbacks.get(i).onStateChanged(bottomSheet, state);
    }
    updateAccessibilityActions();
  }

  private void updateDrawableForTargetState(@State int state) {
    if (state == STATE_SETTLING) {
      
      return;
    }

    boolean expand = state == STATE_EXPANDED;
    if (isShapeExpanded != expand) {
      isShapeExpanded = expand;
      if (materialShapeDrawable != null && interpolatorAnimator != null) {
        if (interpolatorAnimator.isRunning()) {
          interpolatorAnimator.reverse();
        } else {
          float to = expand ? 0f : 1f;
          float from = 1f - to;
          interpolatorAnimator.setFloatValues(from, to);
          interpolatorAnimator.start();
        }
      }
    }
  }

  private int calculatePeekHeight() {
    if (peekHeightAuto) {
      return Math.max(peekHeightMin, parentHeight - parentWidth * 9 / 16);
    }
    return peekHeight + (gestureInsetBottomIgnored ? 0 : gestureInsetBottom);
  }

  private void calculateCollapsedOffset() {
    int peek = calculatePeekHeight();

    if (fitToContents) {
      collapsedOffset = Math.max(parentHeight - peek, fitToContentsOffset);
    } else {
      collapsedOffset = parentHeight - peek;
    }
  }

  private void calculateHalfExpandedOffset() {
    this.halfExpandedOffset = (int) (parentHeight * (1 - halfExpandedRatio));
  }

  private void reset() {
    activePointerId = ViewDragHelper.INVALID_POINTER;
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  private void restoreOptionalState(@NonNull SavedState ss) {
    if (this.saveFlags == SAVE_NONE) {
      return;
    }
    if (this.saveFlags == SAVE_ALL || (this.saveFlags & SAVE_PEEK_HEIGHT) == SAVE_PEEK_HEIGHT) {
      this.peekHeight = ss.peekHeight;
    }
    if (this.saveFlags == SAVE_ALL
        || (this.saveFlags & SAVE_FIT_TO_CONTENTS) == SAVE_FIT_TO_CONTENTS) {
      this.fitToContents = ss.fitToContents;
    }
    if (this.saveFlags == SAVE_ALL || (this.saveFlags & SAVE_HIDEABLE) == SAVE_HIDEABLE) {
      this.hideable = ss.hideable;
    }
    if (this.saveFlags == SAVE_ALL
        || (this.saveFlags & SAVE_SKIP_COLLAPSED) == SAVE_SKIP_COLLAPSED) {
      this.skipCollapsed = ss.skipCollapsed;
    }
  }

  boolean shouldHide(@NonNull View child, float yvel) {
    if (skipCollapsed) {
      return true;
    }
    if (child.getTop() < collapsedOffset) {
      
      return false;
    }
    int peek = calculatePeekHeight();
    final float newTop = child.getTop() + yvel * HIDE_FRICTION;
    return Math.abs(newTop - collapsedOffset) / (float) peek > HIDE_THRESHOLD;
  }

  @Nullable
  @VisibleForTesting
  View findScrollingChild(View view) {
    if (ViewCompat.isNestedScrollingEnabled(view)) {
      return view;
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0, count = group.getChildCount(); i < count; i++) {
        View scrollingChild = findScrollingChild(group.getChildAt(i));
        if (scrollingChild != null) {
          return scrollingChild;
        }
      }
    }
    return null;
  }

  private void createMaterialShapeDrawable(
      @NonNull Context context, AttributeSet attrs, boolean hasBackgroundTint) {
    this.createMaterialShapeDrawable(context, attrs, hasBackgroundTint, null);
  }

  private void createMaterialShapeDrawable(
      @NonNull Context context,
      AttributeSet attrs,
      boolean hasBackgroundTint,
      @Nullable ColorStateList bottomSheetColor) {
    if (this.shapeThemingEnabled) {
      this.shapeAppearanceModelDefault =
          ShapeAppearanceModel.builder(context, attrs, R.attr.bottomSheetStyle, DEF_STYLE_RES)
              .build();

      this.materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModelDefault);
      this.materialShapeDrawable.initializeElevationOverlay(context);

      if (hasBackgroundTint && bottomSheetColor != null) {
        materialShapeDrawable.setFillColor(bottomSheetColor);
      } else {
        
        TypedValue defaultColor = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground, defaultColor, true);
        materialShapeDrawable.setTint(defaultColor.data);
      }
    }
  }

  private void createShapeValueAnimator() {
    interpolatorAnimator = ValueAnimator.ofFloat(0f, 1f);
    interpolatorAnimator.setDuration(CORNER_ANIMATION_DURATION);
    interpolatorAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            if (materialShapeDrawable != null) {
              materialShapeDrawable.setInterpolation(value);
            }
          }
        });
  }

  
  private void setSystemGestureInsets(@NonNull View child) {
    if (VERSION.SDK_INT >= VERSION_CODES.Q && !isGestureInsetBottomIgnored() && !peekHeightAuto) {
      ViewUtils.doOnApplyWindowInsets(
          child,
          new ViewUtils.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(
                View view, WindowInsetsCompat insets, RelativePadding initialPadding) {
              gestureInsetBottom = insets.getMandatorySystemGestureInsets().bottom;
              updatePeekHeight( false);
              return insets;
            }
          });
    }
  }

  private float getYVelocity() {
    if (velocityTracker == null) {
      return 0;
    }
    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
    return velocityTracker.getYVelocity(activePointerId);
  }

  void settleToState(@NonNull View child, int state) {
    int top;
    if (state == STATE_COLLAPSED) {
      top = collapsedOffset;
    } else if (state == STATE_HALF_EXPANDED) {
      top = halfExpandedOffset;
      if (fitToContents && top <= fitToContentsOffset) {
        
        state = STATE_EXPANDED;
        top = fitToContentsOffset;
      }
    } else if (state == STATE_EXPANDED) {
      top = getExpandedOffset();
    } else if (hideable && state == STATE_HIDDEN) {
      top = parentHeight;
    } else {
      throw new IllegalArgumentException("Illegal state argument: " + state);
    }
    startSettlingAnimation(child, state, top, false);
  }

  void startSettlingAnimation(View child, int state, int top, boolean settleFromViewDragHelper) {
    boolean startedSettling =
        settleFromViewDragHelper
            ? viewDragHelper.settleCapturedViewAt(child.getLeft(), top)
            : viewDragHelper.smoothSlideViewTo(child, child.getLeft(), top);
    if (startedSettling) {
      setStateInternal(STATE_SETTLING);
      
      updateDrawableForTargetState(state);
      if (settleRunnable == null) {
        
        settleRunnable = new SettleRunnable(child, state);
      }
      
      if (settleRunnable.isPosted == false) {
        settleRunnable.targetState = state;
        ViewCompat.postOnAnimation(child, settleRunnable);
        settleRunnable.isPosted = true;
      } else {
        
        settleRunnable.targetState = state;
      }
    } else {
      setStateInternal(state);
    }
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
          if (state == STATE_DRAGGING) {
            return false;
          }
          if (touchingScrollingChild) {
            return false;
          }
          if (state == STATE_EXPANDED && activePointerId == pointerId) {
            View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
            if (scroll != null && scroll.canScrollVertically(-1)) {
              
              return false;
            }
          }
          return viewRef != null && viewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(
            @NonNull View changedView, int left, int top, int dx, int dy) {
          dispatchOnSlide(top);
        }

        @Override
        public void onViewDragStateChanged(int state) {
          if (state == ViewDragHelper.STATE_DRAGGING && draggable) {
            setStateInternal(STATE_DRAGGING);
          }
        }

        private boolean releasedLow(@NonNull View child) {
          
          return child.getTop() > (parentHeight + getExpandedOffset()) / 2;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
          int top;
          @State int targetState;
          if (yvel < 0) { 
            if (fitToContents) {
              top = fitToContentsOffset;
              targetState = STATE_EXPANDED;
            } else {
              int currentTop = releasedChild.getTop();
              if (currentTop > halfExpandedOffset) {
                top = halfExpandedOffset;
                targetState = STATE_HALF_EXPANDED;
              } else {
                top = expandedOffset;
                targetState = STATE_EXPANDED;
              }
            }
          } else if (hideable && shouldHide(releasedChild, yvel)) {
            
            
            if ((Math.abs(xvel) < Math.abs(yvel) && yvel > SIGNIFICANT_VEL_THRESHOLD)
                || releasedLow(releasedChild)) {
              top = parentHeight;
              targetState = STATE_HIDDEN;
            } else if (fitToContents) {
              top = fitToContentsOffset;
              targetState = STATE_EXPANDED;
            } else if (Math.abs(releasedChild.getTop() - expandedOffset)
                < Math.abs(releasedChild.getTop() - halfExpandedOffset)) {
              top = expandedOffset;
              targetState = STATE_EXPANDED;
            } else {
              top = halfExpandedOffset;
              targetState = STATE_HALF_EXPANDED;
            }
          } else if (yvel == 0.f || Math.abs(xvel) > Math.abs(yvel)) {
            
            
            int currentTop = releasedChild.getTop();
            if (fitToContents) {
              if (Math.abs(currentTop - fitToContentsOffset)
                  < Math.abs(currentTop - collapsedOffset)) {
                top = fitToContentsOffset;
                targetState = STATE_EXPANDED;
              } else {
                top = collapsedOffset;
                targetState = STATE_COLLAPSED;
              }
            } else {
              if (currentTop < halfExpandedOffset) {
                if (currentTop < Math.abs(currentTop - collapsedOffset)) {
                  top = expandedOffset;
                  targetState = STATE_EXPANDED;
                } else {
                  top = halfExpandedOffset;
                  targetState = STATE_HALF_EXPANDED;
                }
              } else {
                if (Math.abs(currentTop - halfExpandedOffset)
                    < Math.abs(currentTop - collapsedOffset)) {
                  top = halfExpandedOffset;
                  targetState = STATE_HALF_EXPANDED;
                } else {
                  top = collapsedOffset;
                  targetState = STATE_COLLAPSED;
                }
              }
            }
          } else { 
            if (fitToContents) {
              top = collapsedOffset;
              targetState = STATE_COLLAPSED;
            } else {
              
              int currentTop = releasedChild.getTop();
              if (Math.abs(currentTop - halfExpandedOffset)
                  < Math.abs(currentTop - collapsedOffset)) {
                top = halfExpandedOffset;
                targetState = STATE_HALF_EXPANDED;
              } else {
                top = collapsedOffset;
                targetState = STATE_COLLAPSED;
              }
            }
          }
          startSettlingAnimation(releasedChild, targetState, top, true);
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return MathUtils.clamp(
              top, getExpandedOffset(), hideable ? parentHeight : collapsedOffset);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
          if (hideable) {
            return parentHeight;
          } else {
            return collapsedOffset;
          }
        }
      };

  void dispatchOnSlide(int top) {
    View bottomSheet = viewRef.get();
    if (bottomSheet != null && !callbacks.isEmpty()) {
      float slideOffset =
          (top > collapsedOffset || collapsedOffset == getExpandedOffset())
              ? (float) (collapsedOffset - top) / (parentHeight - collapsedOffset)
              : (float) (collapsedOffset - top) / (collapsedOffset - getExpandedOffset());
      for (int i = 0; i < callbacks.size(); i++) {
        callbacks.get(i).onSlide(bottomSheet, slideOffset);
      }
    }
  }

  @VisibleForTesting
  int getPeekHeightMin() {
    return peekHeightMin;
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @VisibleForTesting
  public void disableShapeAnimations() {
    
    interpolatorAnimator = null;
  }

  private class SettleRunnable implements Runnable {

    private final View view;

    private boolean isPosted;

    @State int targetState;

    SettleRunnable(View view, @State int targetState) {
      this.view = view;
      this.targetState = targetState;
    }

    @Override
    public void run() {
      if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
        ViewCompat.postOnAnimation(view, this);
      } else {
        setStateInternal(targetState);
      }
      this.isPosted = false;
    }
  }

  
  protected static class SavedState extends AbsSavedState {
    @State final int state;
    int peekHeight;
    boolean fitToContents;
    boolean hideable;
    boolean skipCollapsed;

    public SavedState(@NonNull Parcel source) {
      this(source, null);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      
      state = source.readInt();
      peekHeight = source.readInt();
      fitToContents = source.readInt() == 1;
      hideable = source.readInt() == 1;
      skipCollapsed = source.readInt() == 1;
    }

    public SavedState(Parcelable superState, @NonNull BottomSheetBehavior<?> behavior) {
      super(superState);
      this.state = behavior.state;
      this.peekHeight = behavior.peekHeight;
      this.fitToContents = behavior.fitToContents;
      this.hideable = behavior.hideable;
      this.skipCollapsed = behavior.skipCollapsed;
    }

    
    @Deprecated
    public SavedState(Parcelable superstate, int state) {
      super(superstate);
      this.state = state;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(state);
      out.writeInt(peekHeight);
      out.writeInt(fitToContents ? 1 : 0);
      out.writeInt(hideable ? 1 : 0);
      out.writeInt(skipCollapsed ? 1 : 0);
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

  
  @NonNull
  @SuppressWarnings("unchecked")
  public static <V extends View> BottomSheetBehavior<V> from(@NonNull V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof BottomSheetBehavior)) {
      throw new IllegalArgumentException("The view is not associated with BottomSheetBehavior");
    }
    return (BottomSheetBehavior<V>) behavior;
  }

  
  public void setUpdateImportantForAccessibilityOnSiblings(
      boolean updateImportantForAccessibilityOnSiblings) {
    this.updateImportantForAccessibilityOnSiblings = updateImportantForAccessibilityOnSiblings;
  }

  private void updateImportantForAccessibility(boolean expanded) {
    if (viewRef == null) {
      return;
    }

    ViewParent viewParent = viewRef.get().getParent();
    if (!(viewParent instanceof CoordinatorLayout)) {
      return;
    }

    CoordinatorLayout parent = (CoordinatorLayout) viewParent;
    final int childCount = parent.getChildCount();
    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && expanded) {
      if (importantForAccessibilityMap == null) {
        importantForAccessibilityMap = new HashMap<>(childCount);
      } else {
        
        return;
      }
    }

    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      if (child == viewRef.get()) {
        continue;
      }

      if (expanded) {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          importantForAccessibilityMap.put(child, child.getImportantForAccessibility());
        }
        if (updateImportantForAccessibilityOnSiblings) {
          ViewCompat.setImportantForAccessibility(
              child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
      } else {
        if (updateImportantForAccessibilityOnSiblings
            && importantForAccessibilityMap != null
            && importantForAccessibilityMap.containsKey(child)) {
          
          ViewCompat.setImportantForAccessibility(child, importantForAccessibilityMap.get(child));
        }
      }
    }

    if (!expanded) {
      importantForAccessibilityMap = null;
    }
  }

  private void updateAccessibilityActions() {
    if (viewRef == null) {
      return;
    }
    V child = viewRef.get();
    if (child == null) {
      return;
    }
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_COLLAPSE);
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_EXPAND);
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_DISMISS);

    if (hideable && state != STATE_HIDDEN) {
      addAccessibilityActionForState(child, AccessibilityActionCompat.ACTION_DISMISS, STATE_HIDDEN);
    }

    switch (state) {
      case STATE_EXPANDED:
        {
          int nextState = fitToContents ? STATE_COLLAPSED : STATE_HALF_EXPANDED;
          addAccessibilityActionForState(
              child, AccessibilityActionCompat.ACTION_COLLAPSE, nextState);
          break;
        }
      case STATE_HALF_EXPANDED:
        {
          addAccessibilityActionForState(
              child, AccessibilityActionCompat.ACTION_COLLAPSE, STATE_COLLAPSED);
          addAccessibilityActionForState(
              child, AccessibilityActionCompat.ACTION_EXPAND, STATE_EXPANDED);
          break;
        }
      case STATE_COLLAPSED:
        {
          int nextState = fitToContents ? STATE_EXPANDED : STATE_HALF_EXPANDED;
          addAccessibilityActionForState(child, AccessibilityActionCompat.ACTION_EXPAND, nextState);
          break;
        }
      default: 
    }
  }

  private void addAccessibilityActionForState(
      V child, AccessibilityActionCompat action, final int state) {
    ViewCompat.replaceAccessibilityAction(
        child,
        action,
        null,
        new AccessibilityViewCommand() {
          @Override
          public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
            setState(state);
            return true;
          }
        });
  }
}
