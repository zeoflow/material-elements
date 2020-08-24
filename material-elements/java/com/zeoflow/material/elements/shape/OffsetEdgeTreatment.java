

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public final class OffsetEdgeTreatment extends EdgeTreatment {

  private final EdgeTreatment other;
  private final float offset;

  public OffsetEdgeTreatment(@NonNull EdgeTreatment other, float offset) {
    this.other = other;
    this.offset = offset;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    other.getEdgePath(length, center - offset, interpolation, shapePath);
  }

  @Override
  boolean forceIntersection() {
    return other.forceIntersection();
  }
}
