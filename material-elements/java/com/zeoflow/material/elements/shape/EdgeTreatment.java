

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public class EdgeTreatment {

  
  @Deprecated
  public void getEdgePath(float length, float interpolation, @NonNull ShapePath shapePath) {
    
    float center = length / 2f;
    getEdgePath(length, center,  interpolation, shapePath);
  }

  
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    shapePath.lineTo(length, 0);
  }

  
  boolean forceIntersection() {
    return false;
  }
}
