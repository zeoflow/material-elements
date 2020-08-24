
package com.zeoflow.material.elements.transformation;

import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import com.zeoflow.material.elements.expandable.ExpandableWidget;
import com.zeoflow.material.elements.transition.MaterialContainerTransform;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;


@Deprecated
public abstract class ExpandableBehavior extends Behavior<View> {

  
  private static final int STATE_UNINITIALIZED = 0;
  
  private static final int STATE_EXPANDED = 1;
  
  private static final int STATE_COLLAPSED = 2;

  @IntDef({STATE_UNINITIALIZED, STATE_EXPANDED, STATE_COLLAPSED})
  @Retention(RetentionPolicy.SOURCE)
  private @interface State {}

  
  @State private int currentState = STATE_UNINITIALIZED;

  public ExpandableBehavior() {}

  public ExpandableBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public abstract boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency);

  
  protected abstract boolean onExpandedStateChange(
      View dependency, View child, boolean expanded, boolean animated);

  @CallSuper
  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull final View child, int layoutDirection) {
    if (!ViewCompat.isLaidOut(child)) {
      final ExpandableWidget dep = findExpandableWidget(parent, child);
      if (dep != null && didStateChange(dep.isExpanded())) {
        currentState = dep.isExpanded() ? STATE_EXPANDED : STATE_COLLAPSED;
        @State final int expectedState = currentState;
        child
            .getViewTreeObserver()
            .addOnPreDrawListener(
                new OnPreDrawListener() {
                  @Override
                  public boolean onPreDraw() {
                    child.getViewTreeObserver().removeOnPreDrawListener(this);
                    
                    if (currentState == expectedState) {
                      onExpandedStateChange((View) dep, child, dep.isExpanded(), false);
                    }
                    return false;
                  }
                });
      }
    }

    return false;
  }

  @CallSuper
  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    ExpandableWidget dep = (ExpandableWidget) dependency;
    boolean expanded = dep.isExpanded();
    if (didStateChange(expanded)) {
      currentState = dep.isExpanded() ? STATE_EXPANDED : STATE_COLLAPSED;
      return onExpandedStateChange((View) dep, child, dep.isExpanded(), true);
    }

    return false;
  }

  @Nullable
  protected ExpandableWidget findExpandableWidget(
      @NonNull CoordinatorLayout parent, @NonNull View child) {
    List<View> dependencies = parent.getDependencies(child);
    for (int i = 0, size = dependencies.size(); i < size; i++) {
      View dependency = dependencies.get(i);
      if (layoutDependsOn(parent, child, dependency)) {
        return (ExpandableWidget) dependency;
      }
    }
    return null;
  }

  private boolean didStateChange(boolean expanded) {
    if (expanded) {
      
      return currentState == STATE_UNINITIALIZED || currentState == STATE_COLLAPSED;
    } else {
      
      return currentState == STATE_EXPANDED;
    }
  }

  
  @Nullable
  public static <T extends ExpandableBehavior> T from(@NonNull View view, @NonNull Class<T> klass) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof ExpandableBehavior)) {
      throw new IllegalArgumentException("The view is not associated with ExpandableBehavior");
    }
    return klass.cast(behavior);
  }
}
