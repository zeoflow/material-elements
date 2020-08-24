
package com.zeoflow.material.elements.expandable;

import androidx.annotation.IdRes;


public interface ExpandableTransformationWidget extends ExpandableWidget {

  
  @IdRes
  int getExpandedComponentIdHint();

  
  void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint);
}
