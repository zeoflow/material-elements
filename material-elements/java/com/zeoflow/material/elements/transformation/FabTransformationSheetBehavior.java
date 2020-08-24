
package com.zeoflow.material.elements.transformation;

import com.google.android.material.R;

import android.content.Context;
import android.os.Build;
import androidx.annotation.AnimatorRes;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.zeoflow.material.elements.animation.MotionSpec;
import com.zeoflow.material.elements.animation.Positioning;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButton;
import com.zeoflow.material.elements.transition.MaterialContainerTransform;

import java.util.HashMap;
import java.util.Map;


@Deprecated
public class FabTransformationSheetBehavior extends FabTransformationBehavior {

  @Nullable private Map<View, Integer> importantForAccessibilityMap;

  public FabTransformationSheetBehavior() {}

  public FabTransformationSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @NonNull
  @Override
  protected FabTransformationSpec onCreateMotionSpec(Context context, boolean expanded) {
    @AnimatorRes int specRes;
    if (expanded) {
      specRes = R.animator.mtrl_fab_transformation_sheet_expand_spec;
    } else {
      specRes = R.animator.mtrl_fab_transformation_sheet_collapse_spec;
    }

    FabTransformationSpec spec = new FabTransformationSpec();
    spec.timings = MotionSpec.createFromResource(context, specRes);
    spec.positioning = new Positioning(Gravity.CENTER, 0f, 0f);
    return spec;
  }

  @CallSuper
  @Override
  protected boolean onExpandedStateChange(
      @NonNull View dependency, @NonNull View child, boolean expanded, boolean animated) {
    updateImportantForAccessibility(child, expanded);
    return super.onExpandedStateChange(dependency, child, expanded, animated);
  }

  private void updateImportantForAccessibility(@NonNull View sheet, boolean expanded) {
    ViewParent viewParent = sheet.getParent();
    if (!(viewParent instanceof CoordinatorLayout)) {
      return;
    }

    CoordinatorLayout parent = (CoordinatorLayout) viewParent;
    final int childCount = parent.getChildCount();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && expanded) {
      importantForAccessibilityMap = new HashMap<>(childCount);
    }

    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);

      
      boolean hasScrimBehavior =
          (child.getLayoutParams() instanceof CoordinatorLayout.LayoutParams)
              && (((CoordinatorLayout.LayoutParams) child.getLayoutParams()).getBehavior()
                  instanceof FabTransformationScrimBehavior);
      if (child == sheet || hasScrimBehavior) {
        continue;
      }

      if (!expanded) {
        if (importantForAccessibilityMap != null
            && importantForAccessibilityMap.containsKey(child)) {
          
          ViewCompat.setImportantForAccessibility(child, importantForAccessibilityMap.get(child));
        }
      } else {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          importantForAccessibilityMap.put(child, child.getImportantForAccessibility());
        }

        ViewCompat.setImportantForAccessibility(
            child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }

    if (!expanded) {
      importantForAccessibilityMap = null;
    }
  }
}
