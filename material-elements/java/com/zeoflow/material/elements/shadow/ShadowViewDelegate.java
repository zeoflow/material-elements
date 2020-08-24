

package com.zeoflow.material.elements.shadow;

import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;


public interface ShadowViewDelegate {
  float getRadius();

  void setShadowPadding(int left, int top, int right, int bottom);

  void setBackgroundDrawable(@Nullable Drawable background);

  boolean isCompatPaddingEnabled();
}
