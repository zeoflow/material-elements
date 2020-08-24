

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public class TriangleEdgeTreatment extends EdgeTreatment {

  private final float size;
  private final boolean inside;

  
  public TriangleEdgeTreatment(float size, boolean inside) {
    this.size = size;
    this.inside = inside;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    shapePath.lineTo(center - (size * interpolation), 0);
    shapePath.lineTo(center, inside ? size * interpolation : -size * interpolation);
    shapePath.lineTo(center + (size * interpolation), 0);
    shapePath.lineTo(length, 0);
  }
}
