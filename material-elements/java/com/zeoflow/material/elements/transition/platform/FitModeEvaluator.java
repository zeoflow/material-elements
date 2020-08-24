


package com.zeoflow.material.elements.transition.platform;

import android.graphics.RectF;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
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
