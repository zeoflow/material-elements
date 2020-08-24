

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public final class MarkerEdgeTreatment extends EdgeTreatment {

  private final float radius;

  public MarkerEdgeTreatment(float radius) {
    this.radius = radius - 0.001f;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    float side = (float) (radius * Math.sqrt(2) / 2);
    float side2 = (float) Math.sqrt(Math.pow(radius, 2) - Math.pow(side, 2));
    shapePath.reset(center - side, (float) -(radius * Math.sqrt(2) - radius) + side2);
    shapePath.lineTo(center, (float) -(radius * Math.sqrt(2) - radius));
    shapePath.lineTo(center + side, (float) -(radius * Math.sqrt(2) - radius) + side2);
  }

  @Override
  boolean forceIntersection() {
    return true;
  }
}
