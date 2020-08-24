

package com.zeoflow.material.elements.appbar;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;


abstract class HeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {

  private static final int INVALID_POINTER = -1;

  @Nullable private Runnable flingRunnable;
  OverScroller scroller;

  private boolean isBeingDragged;
  private int activePointerId = INVALID_POINTER;
  private int lastMotionY;
  private int touchSlop = -1;
  @Nullable private VelocityTracker velocityTracker;

  public HeaderBehavior() {}

  public HeaderBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    if (touchSlop < 0) {
      touchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
    }

    
    if (ev.getActionMasked() == MotionEvent.ACTION_MOVE && isBeingDragged) {
      if (activePointerId == INVALID_POINTER) {
        
        return false;
      }
      int pointerIndex = ev.findPointerIndex(activePointerId);
      if (pointerIndex == -1) {
        return false;
      }

      int y = (int) ev.getY(pointerIndex);
      int yDiff = Math.abs(y - lastMotionY);
      if (yDiff > touchSlop) {
        lastMotionY = y;
        return true;
      }
    }

    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
      activePointerId = INVALID_POINTER;

      int x = (int) ev.getX();
      int y = (int) ev.getY();
      isBeingDragged = canDragView(child) && parent.isPointInChildBounds(child, x, y);
      if (isBeingDragged) {
        lastMotionY = y;
        activePointerId = ev.getPointerId(0);
        ensureVelocityTracker();

        
        if (scroller != null && !scroller.isFinished()) {
          scroller.abortAnimation();
          return true;
        }
      }
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(ev);
    }

    return false;
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    boolean consumeUp = false;
    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_MOVE:
        final int activePointerIndex = ev.findPointerIndex(activePointerId);
        if (activePointerIndex == -1) {
          return false;
        }

        final int y = (int) ev.getY(activePointerIndex);
        int dy = lastMotionY - y;
        lastMotionY = y;
        
        scroll(parent, child, dy, getMaxDragOffset(child), 0);
        break;
      case MotionEvent.ACTION_POINTER_UP:
        int newIndex = ev.getActionIndex() == 0 ? 1 : 0;
        activePointerId = ev.getPointerId(newIndex);
        lastMotionY = (int) (ev.getY(newIndex) + 0.5f);
        break;
      case MotionEvent.ACTION_UP:
        if (velocityTracker != null) {
          consumeUp = true;
          velocityTracker.addMovement(ev);
          velocityTracker.computeCurrentVelocity(1000);
          float yvel = velocityTracker.getYVelocity(activePointerId);
          fling(parent, child, -getScrollRangeForDragFling(child), 0, yvel);
        }

        
      case MotionEvent.ACTION_CANCEL:
        isBeingDragged = false;
        activePointerId = INVALID_POINTER;
        if (velocityTracker != null) {
          velocityTracker.recycle();
          velocityTracker = null;
        }
        break;
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(ev);
    }

    return isBeingDragged || consumeUp;
  }

  int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset) {
    return setHeaderTopBottomOffset(
        parent, header, newOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  int setHeaderTopBottomOffset(
      CoordinatorLayout parent, V header, int newOffset, int minOffset, int maxOffset) {
    final int curOffset = getTopAndBottomOffset();
    int consumed = 0;

    if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
      
      
      newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);

      if (curOffset != newOffset) {
        setTopAndBottomOffset(newOffset);
        
        consumed = curOffset - newOffset;
      }
    }

    return consumed;
  }

  int getTopBottomOffsetForScrollingSibling() {
    return getTopAndBottomOffset();
  }

  final int scroll(
      CoordinatorLayout coordinatorLayout, V header, int dy, int minOffset, int maxOffset) {
    return setHeaderTopBottomOffset(
        coordinatorLayout,
        header,
        getTopBottomOffsetForScrollingSibling() - dy,
        minOffset,
        maxOffset);
  }

  final boolean fling(
      CoordinatorLayout coordinatorLayout,
      @NonNull V layout,
      int minOffset,
      int maxOffset,
      float velocityY) {
    if (flingRunnable != null) {
      layout.removeCallbacks(flingRunnable);
      flingRunnable = null;
    }

    if (scroller == null) {
      scroller = new OverScroller(layout.getContext());
    }

    scroller.fling(
        0,
        getTopAndBottomOffset(), 
        0,
        Math.round(velocityY), 
        0,
        0, 
        minOffset,
        maxOffset); 

    if (scroller.computeScrollOffset()) {
      flingRunnable = new FlingRunnable(coordinatorLayout, layout);
      ViewCompat.postOnAnimation(layout, flingRunnable);
      return true;
    } else {
      onFlingFinished(coordinatorLayout, layout);
      return false;
    }
  }

  
  void onFlingFinished(CoordinatorLayout parent, V layout) {
    
  }

  
  boolean canDragView(V view) {
    return false;
  }

  
  int getMaxDragOffset(@NonNull V view) {
    return -view.getHeight();
  }

  int getScrollRangeForDragFling(@NonNull V view) {
    return view.getHeight();
  }

  private void ensureVelocityTracker() {
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
  }

  private class FlingRunnable implements Runnable {
    private final CoordinatorLayout parent;
    private final V layout;

    FlingRunnable(CoordinatorLayout parent, V layout) {
      this.parent = parent;
      this.layout = layout;
    }

    @Override
    public void run() {
      if (layout != null && scroller != null) {
        if (scroller.computeScrollOffset()) {
          setHeaderTopBottomOffset(parent, layout, scroller.getCurrY());
          
          ViewCompat.postOnAnimation(layout, this);
        } else {
          onFlingFinished(parent, layout);
        }
      }
    }
  }
}
