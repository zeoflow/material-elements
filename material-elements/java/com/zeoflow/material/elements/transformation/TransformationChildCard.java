
package com.zeoflow.material.elements.transformation;

import android.content.Context;
import android.util.AttributeSet;
import com.zeoflow.material.elements.circularreveal.cardview.CircularRevealCardView;
import com.zeoflow.material.elements.expandable.ExpandableWidget;
import com.zeoflow.material.elements.transition.MaterialContainerTransform;


@Deprecated
public class TransformationChildCard extends CircularRevealCardView
{

  public TransformationChildCard(Context context) {
    this(context, null);
  }

  public TransformationChildCard(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
