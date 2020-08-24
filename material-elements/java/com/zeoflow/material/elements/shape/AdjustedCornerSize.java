

package com.zeoflow.material.elements.shape;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.Arrays;


@RestrictTo(LIBRARY_GROUP)
public final class AdjustedCornerSize implements CornerSize {

  private final CornerSize other;
  private final float adjustment;

  public AdjustedCornerSize(float adjustment, @NonNull CornerSize other) {
    
    while (other instanceof AdjustedCornerSize) {
      other = ((AdjustedCornerSize) other).other;
      adjustment += ((AdjustedCornerSize) other).adjustment;
    }

    this.other = other;
    this.adjustment = adjustment;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return Math.max(0, other.getCornerSize(bounds) + adjustment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdjustedCornerSize)) {
      return false;
    }
    AdjustedCornerSize that = (AdjustedCornerSize) o;
    return other.equals(that.other) && adjustment == that.adjustment;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {other, adjustment};
    return Arrays.hashCode(hashedFields);
  }
}
