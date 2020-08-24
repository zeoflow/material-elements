

package com.zeoflow.material.elements.slider;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;


@RestrictTo(Scope.LIBRARY_GROUP)
public interface BaseOnChangeListener<S> {

  
  void onValueChange(@NonNull S slider, float value, boolean fromUser);
}
