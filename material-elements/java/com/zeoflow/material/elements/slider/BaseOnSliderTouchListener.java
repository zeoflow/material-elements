

package com.zeoflow.material.elements.slider;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;


@RestrictTo(Scope.LIBRARY_GROUP)
public interface BaseOnSliderTouchListener<S> {
  void onStartTrackingTouch(@NonNull S slider);

  void onStopTrackingTouch(@NonNull S slider);
}
