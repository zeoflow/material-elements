
package com.zeoflow.material.elements.transformation;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import com.zeoflow.material.elements.circularreveal.CircularRevealFrameLayout;
import com.zeoflow.material.elements.expandable.ExpandableWidget;
import com.zeoflow.material.elements.transition.MaterialContainerTransform;


@Deprecated
public class TransformationChildLayout extends CircularRevealFrameLayout {

  public TransformationChildLayout(@NonNull Context context) {
    this(context, null);
  }

  public TransformationChildLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }
}
