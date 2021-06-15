package com.zeoflow.material.elements.bottomsheet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class LockableBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    public LockableBottomSheetBehavior() {
    }

    public LockableBottomSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean swipeEnabled = true;

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.swipeEnabled = swipeEnabled;
    }

    public boolean isSwipeEnabled() {
        return swipeEnabled;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        if (swipeEnabled) {
            return super.onInterceptTouchEvent(parent, child, event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        if (swipeEnabled) {
            return super.onTouchEvent(parent, child, event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        if (swipeEnabled) {
            return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
        } else {
            return false;
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (swipeEnabled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int type) {
        if (swipeEnabled) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type);
        }
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, float velocityX, float velocityY) {
        if (swipeEnabled) {
            return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
        } else {
            return false;
        }
    }
}
