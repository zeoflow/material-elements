

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

class ViewOverlayApi14 implements ViewOverlayImpl {

  
  protected OverlayViewGroup overlayViewGroup;

  ViewOverlayApi14(Context context, ViewGroup hostView, View requestingView) {
    overlayViewGroup = new OverlayViewGroup(context, hostView, requestingView, this);
  }

  static ViewOverlayApi14 createFrom(View view) {
    ViewGroup contentView = ViewUtils.getContentView(view);
    if (contentView != null) {
      final int numChildren = contentView.getChildCount();
      for (int i = 0; i < numChildren; ++i) {
        View child = contentView.getChildAt(i);
        if (child instanceof OverlayViewGroup) {
          return ((OverlayViewGroup) child).viewOverlay;
        }
      }
      return new ViewGroupOverlayApi14(contentView.getContext(), contentView, view);
    }
    return null;
  }

  @Override
  public void add(@NonNull Drawable drawable) {
    overlayViewGroup.add(drawable);
  }

  @Override
  public void remove(@NonNull Drawable drawable) {
    overlayViewGroup.remove(drawable);
  }

  
  @SuppressLint({"ViewConstructor", "PrivateApi"})
  static class OverlayViewGroup extends ViewGroup {

    static Method invalidateChildInParentFastMethod;

    static {
      try {
        invalidateChildInParentFastMethod =
            ViewGroup.class.getDeclaredMethod(
                "invalidateChildInParentFast", int.class, int.class, Rect.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    
    ViewGroup hostView;

    View requestingView;
    
    ArrayList<Drawable> drawables = null;
    
    ViewOverlayApi14 viewOverlay;

    private boolean disposed;

    OverlayViewGroup(
        Context context, ViewGroup hostView, View requestingView, ViewOverlayApi14 viewOverlay) {
      super(context);
      this.hostView = hostView;
      this.requestingView = requestingView;
      setRight(hostView.getWidth());
      setBottom(hostView.getHeight());
      hostView.addView(this);
      this.viewOverlay = viewOverlay;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
      
      return false;
    }

    @SuppressWarnings("deprecation")
    public void add(Drawable drawable) {
      assertNotDisposed();
      if (drawables == null) {

        drawables = new ArrayList<>();
      }
      if (!drawables.contains(drawable)) {
        
        drawables.add(drawable);
        invalidate(drawable.getBounds());
        drawable.setCallback(this);
      }
    }

    @SuppressWarnings("deprecation")
    public void remove(Drawable drawable) {
      if (drawables != null) {
        drawables.remove(drawable);
        invalidate(drawable.getBounds());
        drawable.setCallback(null);
        disposeIfEmpty();
      }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
      return super.verifyDrawable(who) || (drawables != null && drawables.contains(who));
    }

    public void add(View child) {
      assertNotDisposed();
      if (child.getParent() instanceof ViewGroup) {
        ViewGroup parent = (ViewGroup) child.getParent();
        if (parent != hostView
            && parent.getParent() != null
            && ViewCompat.isAttachedToWindow(parent)) {
          
          
          int[] parentLocation = new int[2];
          int[] hostViewLocation = new int[2];
          parent.getLocationOnScreen(parentLocation);
          hostView.getLocationOnScreen(hostViewLocation);
          ViewCompat.offsetLeftAndRight(child, parentLocation[0] - hostViewLocation[0]);
          ViewCompat.offsetTopAndBottom(child, parentLocation[1] - hostViewLocation[1]);
        }
        parent.removeView(child);
        
        
        
        
        
        if (child.getParent() != null) {
          parent.removeView(child);
        }
      }
      super.addView(child);
    }

    public void remove(View view) {
      super.removeView(view);
      disposeIfEmpty();
    }

    private void assertNotDisposed() {
      if (disposed) {
        throw new IllegalStateException(
            "This overlay was disposed already. "
                + "Please use a new one via ViewGroupUtils.getOverlay()");
      }
    }

    private void disposeIfEmpty() {
      if (getChildCount() == 0 && (drawables == null || drawables.size() == 0)) {
        disposed = true;
        hostView.removeView(this);
      }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
      invalidate(drawable.getBounds());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
      int[] contentViewLocation = new int[2];
      int[] hostViewLocation = new int[2];
      hostView.getLocationOnScreen(contentViewLocation);
      requestingView.getLocationOnScreen(hostViewLocation);
      canvas.translate(
          hostViewLocation[0] - contentViewLocation[0],
          hostViewLocation[1] - contentViewLocation[1]);
      canvas.clipRect(new Rect(0, 0, requestingView.getWidth(), requestingView.getHeight()));
      super.dispatchDraw(canvas);
      final int numDrawables = (drawables == null) ? 0 : drawables.size();
      for (int i = 0; i < numDrawables; ++i) {
        drawables.get(i).draw(canvas);
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      
    }

    

    private void getOffset(int[] offset) {
      int[] contentViewLocation = new int[2];
      int[] hostViewLocation = new int[2];
      hostView.getLocationOnScreen(contentViewLocation);
      requestingView.getLocationOnScreen(hostViewLocation);
      offset[0] = hostViewLocation[0] - contentViewLocation[0];
      offset[1] = hostViewLocation[1] - contentViewLocation[1];
    }

    
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected ViewParent invalidateChildInParentFast(int left, int top, Rect dirty) {
      if (hostView != null && invalidateChildInParentFastMethod != null) {
        try {
          int[] offset = new int[2];
          getOffset(offset);
          invalidateChildInParentFastMethod.invoke(hostView, left, top, dirty);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
      return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
      if (hostView != null) {
        dirty.offset(location[0], location[1]);
        if (hostView != null) {
          location[0] = 0;
          location[1] = 0;
          int[] offset = new int[2];
          getOffset(offset);
          dirty.offset(offset[0], offset[1]);
          return super.invalidateChildInParent(location, dirty);
          
        } else {
          invalidate(dirty);
        }
      }
      return null;
    }
  }
}
