

package com.zeoflow.material.elements.behavior;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.widget.ViewDragHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class SwipeDismissBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  
  public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

  
  public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

  
  public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({SWIPE_DIRECTION_START_TO_END, SWIPE_DIRECTION_END_TO_START, SWIPE_DIRECTION_ANY})
  @Retention(RetentionPolicy.SOURCE)
  private @interface SwipeDirection {}

  
  public static final int SWIPE_DIRECTION_START_TO_END = 0;

  
  public static final int SWIPE_DIRECTION_END_TO_START = 1;

  
  public static final int SWIPE_DIRECTION_ANY = 2;

  private static final float DEFAULT_DRAG_DISMISS_THRESHOLD = 0.5f;
  private static final float DEFAULT_ALPHA_START_DISTANCE = 0f;
  private static final float DEFAULT_ALPHA_END_DISTANCE = DEFAULT_DRAG_DISMISS_THRESHOLD;

  ViewDragHelper viewDragHelper;
  OnDismissListener listener;
  private boolean interceptingEvents;

  private float sensitivity = 0f;
  private boolean sensitivitySet;

  int swipeDirection = SWIPE_DIRECTION_ANY;
  float dragDismissThreshold = DEFAULT_DRAG_DISMISS_THRESHOLD;
  float alphaStartSwipeDistance = DEFAULT_ALPHA_START_DISTANCE;
  float alphaEndSwipeDistance = DEFAULT_ALPHA_END_DISTANCE;

  
  public interface OnDismissListener {
    
    public void onDismiss(View view);

    
    public void onDragStateChanged(int state);
  }

  
  public void setListener(@Nullable OnDismissListener listener) {
    this.listener = listener;
  }

  @VisibleForTesting
  @Nullable
  public OnDismissListener getListener() {
    return listener;
  }

  
  public void setSwipeDirection(@SwipeDirection int direction) {
    swipeDirection = direction;
  }

  
  public void setDragDismissDistance(float distance) {
    dragDismissThreshold = clamp(0f, distance, 1f);
  }

  
  public void setStartAlphaSwipeDistance(float fraction) {
    alphaStartSwipeDistance = clamp(0f, fraction, 1f);
  }

  
  public void setEndAlphaSwipeDistance(float fraction) {
    alphaEndSwipeDistance = clamp(0f, fraction, 1f);
  }

  
  public void setSensitivity(float sensitivity) {
    this.sensitivity = sensitivity;
    sensitivitySet = true;
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    boolean handled = super.onLayoutChild(parent, child, layoutDirection);
    if (ViewCompat.getImportantForAccessibility(child)
        == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
      ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
      updateAccessibilityActions(child);
    }
    return handled;
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    boolean dispatchEventToHelper = interceptingEvents;

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        interceptingEvents =
            parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY());
        dispatchEventToHelper = interceptingEvents;
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        
        interceptingEvents = false;
        break;
    }

    if (dispatchEventToHelper) {
      ensureViewDragHelper(parent);
      return viewDragHelper.shouldInterceptTouchEvent(event);
    }
    return false;
  }

  @Override
  public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
    if (viewDragHelper != null) {
      viewDragHelper.processTouchEvent(event);
      return true;
    }
    return false;
  }

  
  public boolean canSwipeDismissView(@NonNull View view) {
    return true;
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {
        private static final int INVALID_POINTER_ID = -1;

        private int originalCapturedViewLeft;
        private int activePointerId = INVALID_POINTER_ID;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
          
          return (activePointerId == INVALID_POINTER_ID || activePointerId == pointerId)
              && canSwipeDismissView(child);
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
          this.activePointerId = activePointerId;
          originalCapturedViewLeft = capturedChild.getLeft();

          
          
          final ViewParent parent = capturedChild.getParent();
          if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
          }
        }

        @Override
        public void onViewDragStateChanged(int state) {
          if (listener != null) {
            listener.onDragStateChanged(state);
          }
        }

        @Override
        public void onViewReleased(@NonNull View child, float xvel, float yvel) {
          
          activePointerId = INVALID_POINTER_ID;

          final int childWidth = child.getWidth();
          int targetLeft;
          boolean dismiss = false;

          if (shouldDismiss(child, xvel)) {
            targetLeft =
                child.getLeft() < originalCapturedViewLeft
                    ? originalCapturedViewLeft - childWidth
                    : originalCapturedViewLeft + childWidth;
            dismiss = true;
          } else {
            
            targetLeft = originalCapturedViewLeft;
          }

          if (viewDragHelper.settleCapturedViewAt(targetLeft, child.getTop())) {
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, dismiss));
          } else if (dismiss && listener != null) {
            listener.onDismiss(child);
          }
        }

        private boolean shouldDismiss(@NonNull View child, float xvel) {
          if (xvel != 0f) {
            final boolean isRtl =
                ViewCompat.getLayoutDirection(child) == ViewCompat.LAYOUT_DIRECTION_RTL;

            if (swipeDirection == SWIPE_DIRECTION_ANY) {
              
              return true;
            } else if (swipeDirection == SWIPE_DIRECTION_START_TO_END) {
              
              
              return isRtl ? xvel < 0f : xvel > 0f;
            } else if (swipeDirection == SWIPE_DIRECTION_END_TO_START) {
              
              
              return isRtl ? xvel > 0f : xvel < 0f;
            }
          } else {
            final int distance = child.getLeft() - originalCapturedViewLeft;
            final int thresholdDistance = Math.round(child.getWidth() * dragDismissThreshold);
            return Math.abs(distance) >= thresholdDistance;
          }

          return false;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
          return child.getWidth();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          final boolean isRtl =
              ViewCompat.getLayoutDirection(child) == ViewCompat.LAYOUT_DIRECTION_RTL;
          int min;
          int max;

          if (swipeDirection == SWIPE_DIRECTION_START_TO_END) {
            if (isRtl) {
              min = originalCapturedViewLeft - child.getWidth();
              max = originalCapturedViewLeft;
            } else {
              min = originalCapturedViewLeft;
              max = originalCapturedViewLeft + child.getWidth();
            }
          } else if (swipeDirection == SWIPE_DIRECTION_END_TO_START) {
            if (isRtl) {
              min = originalCapturedViewLeft;
              max = originalCapturedViewLeft + child.getWidth();
            } else {
              min = originalCapturedViewLeft - child.getWidth();
              max = originalCapturedViewLeft;
            }
          } else {
            min = originalCapturedViewLeft - child.getWidth();
            max = originalCapturedViewLeft + child.getWidth();
          }

          return clamp(min, left, max);
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return child.getTop();
        }

        @Override
        public void onViewPositionChanged(@NonNull View child, int left, int top, int dx, int dy) {
          final float startAlphaDistance =
              originalCapturedViewLeft + child.getWidth() * alphaStartSwipeDistance;
          final float endAlphaDistance =
              originalCapturedViewLeft + child.getWidth() * alphaEndSwipeDistance;

          if (left <= startAlphaDistance) {
            child.setAlpha(1f);
          } else if (left >= endAlphaDistance) {
            child.setAlpha(0f);
          } else {
            
            final float distance = fraction(startAlphaDistance, endAlphaDistance, left);
            child.setAlpha(clamp(0f, 1f - distance, 1f));
          }
        }
      };

  private void ensureViewDragHelper(ViewGroup parent) {
    if (viewDragHelper == null) {
      viewDragHelper =
          sensitivitySet
              ? ViewDragHelper.create(parent, sensitivity, dragCallback)
              : ViewDragHelper.create(parent, dragCallback);
    }
  }

  private class SettleRunnable implements Runnable {
    private final View view;
    private final boolean dismiss;

    SettleRunnable(View view, boolean dismiss) {
      this.view = view;
      this.dismiss = dismiss;
    }

    @Override
    public void run() {
      if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
        ViewCompat.postOnAnimation(view, this);
      } else {
        if (dismiss && listener != null) {
          listener.onDismiss(view);
        }
      }
    }
  }

  private void updateAccessibilityActions(View child) {
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_DISMISS);
    if (canSwipeDismissView(child)) {
      ViewCompat.replaceAccessibilityAction(
          child,
          AccessibilityActionCompat.ACTION_DISMISS,
          null,
          new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
              if (canSwipeDismissView(view)) {
                final boolean isRtl =
                    ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
                boolean dismissToLeft =
                    (swipeDirection == SWIPE_DIRECTION_START_TO_END && isRtl)
                        || (swipeDirection == SWIPE_DIRECTION_END_TO_START && !isRtl);
                int offset = dismissToLeft ? -view.getWidth() : view.getWidth();
                ViewCompat.offsetLeftAndRight(view, offset);
                view.setAlpha(0f);
                if (listener != null) {
                  listener.onDismiss(view);
                }
                return true;
              }
              return false;
            }
          });
    }
  }

  static float clamp(float min, float value, float max) {
    return Math.min(Math.max(min, value), max);
  }

  static int clamp(int min, int value, int max) {
    return Math.min(Math.max(min, value), max);
  }

  
  public int getDragState() {
    return viewDragHelper != null ? viewDragHelper.getViewDragState() : STATE_IDLE;
  }

  
  static float fraction(float startValue, float endValue, float value) {
    return (value - startValue) / (endValue - startValue);
  }
}
