
package com.zeoflow.material.elements.animation;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;


public class ChildrenAlphaProperty extends Property<ViewGroup, Float> {

  
  public static final Property<ViewGroup, Float> CHILDREN_ALPHA =
      new ChildrenAlphaProperty("childrenAlpha");

  private ChildrenAlphaProperty(String name) {
    super(Float.class, name);
  }

  @NonNull
  @Override
  public Float get(@NonNull ViewGroup object) {
    Float alpha = (Float) object.getTag(R.id.mtrl_internal_children_alpha_tag);
    if (alpha != null) {
      return alpha;
    } else {
      return 1f;
    }
  }

  @Override
  public void set(@NonNull ViewGroup object, @NonNull Float value) {
    float alpha = value;

    object.setTag(R.id.mtrl_internal_children_alpha_tag, alpha);

    for (int i = 0, count = object.getChildCount(); i < count; i++) {
      View child = object.getChildAt(i);
      child.setAlpha(alpha);
    }
  }
}
