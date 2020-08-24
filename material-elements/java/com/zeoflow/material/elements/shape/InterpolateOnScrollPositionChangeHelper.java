

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.ScrollView;


public class InterpolateOnScrollPositionChangeHelper {

  private View shapedView;
  private MaterialShapeDrawable materialShapeDrawable;
  private ScrollView containingScrollView;
  private final int[] scrollLocation = new int[2];
  private final int[] containerLocation = new int[2];
  private final ViewTreeObserver.OnScrollChangedListener scrollChangedListener =
      new OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
          updateInterpolationForScreenPosition();
        }
      };

  
  public InterpolateOnScrollPositionChangeHelper(
      View shapedView,
      MaterialShapeDrawable materialShapeDrawable,
      ScrollView containingScrollView) {
    this.shapedView = shapedView;
    this.materialShapeDrawable = materialShapeDrawable;
    this.containingScrollView = containingScrollView;
  }

  
  public void setMaterialShapeDrawable(MaterialShapeDrawable materialShapeDrawable) {
    this.materialShapeDrawable = materialShapeDrawable;
  }

  
  public void setContainingScrollView(ScrollView containingScrollView) {
    this.containingScrollView = containingScrollView;
  }

  
  public void startListeningForScrollChanges(@NonNull ViewTreeObserver viewTreeObserver) {
    viewTreeObserver.addOnScrollChangedListener(scrollChangedListener);
  }

  
  public void stopListeningForScrollChanges(@NonNull ViewTreeObserver viewTreeObserver) {
    viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener);
  }

  
  public void updateInterpolationForScreenPosition() {
    if (containingScrollView == null) {
      
      return;
    }
    if (containingScrollView.getChildCount() == 0) {
      
      throw new IllegalStateException(
          "Scroll bar must contain a child to calculate interpolation.");
    }

    containingScrollView.getLocationInWindow(scrollLocation);
    containingScrollView.getChildAt(0).getLocationInWindow(containerLocation);
    int y = shapedView.getTop() - scrollLocation[1] + containerLocation[1];
    int viewHeight = shapedView.getHeight();
    int windowHeight = containingScrollView.getHeight();

    
    if (y < 0) {
      materialShapeDrawable.setInterpolation(
          Math.max(0f, Math.min(1f, 1f + (float) y / (float) viewHeight)));
      shapedView.invalidate();
    } else if (y + viewHeight > windowHeight) {
      int distanceOffScreen = y + viewHeight - windowHeight;
      materialShapeDrawable.setInterpolation(
          Math.max(0f, Math.min(1f, 1f - (float) distanceOffScreen / (float) viewHeight)));
      shapedView.invalidate();
    } else if (materialShapeDrawable.getInterpolation() != 1f) {
      materialShapeDrawable.setInterpolation(1f);
      shapedView.invalidate();
    }
  }
}
