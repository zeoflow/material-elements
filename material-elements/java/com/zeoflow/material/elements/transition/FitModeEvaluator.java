

package com.zeoflow.material.elements.transition;

import android.graphics.RectF;

interface FitModeEvaluator {

  
  FitModeResult evaluate(
      float progress,
      float scaleStartFraction,
      float scaleEndFraction,
      float startWidth,
      float startHeight,
      float endWidth,
      float endHeight);

  
  boolean shouldMaskStartBounds(FitModeResult fitModeResult);

  
  void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult);
}
