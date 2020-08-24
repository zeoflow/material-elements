

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public class RoundedCornerTreatment extends CornerTreatment {

  float radius = -1;

  public RoundedCornerTreatment() {}

  
  @Deprecated
  public RoundedCornerTreatment(float radius) {
    this.radius = radius;
  }

  @Override
  public void getCornerPath(
      @NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
    shapePath.reset(0, radius * interpolation, ShapePath.ANGLE_LEFT, 180 - angle);
    shapePath.addArc(0, 0, 2 * radius * interpolation, 2 * radius * interpolation, 180, angle);
  }
}
