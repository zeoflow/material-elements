


package com.zeoflow.material.elements.transition.platform;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
interface FadeModeEvaluator {
  
  FadeModeResult evaluate(float progress, float fadeStartFraction, float fadeEndFraction);
}
