

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public class CutCornerTreatment extends CornerTreatment {

  float size = -1;

  public CutCornerTreatment() {}

  
  @Deprecated
  public CutCornerTreatment(float size) {
    this.size = size;
  }

  @Override
  public void getCornerPath(
      @NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
    shapePath.reset(0, radius * interpolation, ShapePath.ANGLE_LEFT, 180 - angle);
    shapePath.lineTo(
        (float) (Math.sin(Math.toRadians(angle)) * radius * interpolation),
        
        
        (float) (Math.sin(Math.toRadians(90 - angle)) * radius * interpolation));
  }
}
