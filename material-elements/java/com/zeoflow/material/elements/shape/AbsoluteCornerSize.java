

package com.zeoflow.material.elements.shape;

import android.graphics.RectF;
import androidx.annotation.NonNull;
import java.util.Arrays;


public final class AbsoluteCornerSize implements CornerSize {

  private final float size;

  public AbsoluteCornerSize(float size) {
    this.size = size;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return size;
  }

  
  public float getCornerSize() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbsoluteCornerSize)) {
      return false;
    }
    AbsoluteCornerSize that = (AbsoluteCornerSize) o;
    return size == that.size;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {size};
    return Arrays.hashCode(hashedFields);
  }
}
