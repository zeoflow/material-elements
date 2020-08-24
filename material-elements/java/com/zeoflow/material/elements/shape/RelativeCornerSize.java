

package com.zeoflow.material.elements.shape;

import android.graphics.RectF;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import java.util.Arrays;


public final class RelativeCornerSize implements CornerSize {

  private final float percent;

  
  public RelativeCornerSize(@FloatRange(from = 0.0f, to = 1.0f) float percent) {
    this.percent = percent;
  }

  
  @FloatRange(from = 0.0f, to = 1.0f)
  public float getRelativePercent() {
    return percent;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return percent * bounds.height();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelativeCornerSize)) {
      return false;
    }
    RelativeCornerSize that = (RelativeCornerSize) o;
    return percent == that.percent;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {percent};
    return Arrays.hashCode(hashedFields);
  }
}
