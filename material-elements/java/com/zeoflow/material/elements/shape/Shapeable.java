

package com.zeoflow.material.elements.shape;

import androidx.annotation.NonNull;


public interface Shapeable {

  
  void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel);

  
  @NonNull
  ShapeAppearanceModel getShapeAppearanceModel();
}
