

package com.zeoflow.material.elements.expandable;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewParent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


public final class ExpandableWidgetHelper {

  @NonNull private final View widget;

  private boolean expanded = false;
  @IdRes private int expandedComponentIdHint = 0;

  
  public ExpandableWidgetHelper(ExpandableWidget widget) {
    this.widget = (View) widget;
  }

  
  public boolean setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      dispatchExpandedStateChanged();
      return true;
    }
    return false;
  }

  
  public boolean isExpanded() {
    return expanded;
  }

  
  @NonNull
  public Bundle onSaveInstanceState() {
    Bundle state = new Bundle();
    state.putBoolean("expanded", expanded);
    state.putInt("expandedComponentIdHint", expandedComponentIdHint);

    return state;
  }

  
  public void onRestoreInstanceState(@NonNull Bundle state) {
    expanded = state.getBoolean("expanded", false);
    expandedComponentIdHint = state.getInt("expandedComponentIdHint", 0);

    if (expanded) {
      dispatchExpandedStateChanged();
    }
  }

  
  public void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint) {
    this.expandedComponentIdHint = expandedComponentIdHint;
  }

  
  @IdRes
  public int getExpandedComponentIdHint() {
    return expandedComponentIdHint;
  }

  private void dispatchExpandedStateChanged() {
    ViewParent parent = widget.getParent();
    if (parent instanceof CoordinatorLayout) {
      ((CoordinatorLayout) parent).dispatchDependentViewsChanged(widget);
    }
  }
}
