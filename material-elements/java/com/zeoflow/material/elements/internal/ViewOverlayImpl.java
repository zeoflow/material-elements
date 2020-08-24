

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;


@RestrictTo(LIBRARY_GROUP)
public interface ViewOverlayImpl {

  
  void add(@NonNull Drawable drawable);

  
  void remove(@NonNull Drawable drawable);
}
