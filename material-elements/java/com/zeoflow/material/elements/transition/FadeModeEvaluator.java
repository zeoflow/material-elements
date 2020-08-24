

package com.zeoflow.material.elements.transition;

interface FadeModeEvaluator {
  
  FadeModeResult evaluate(float progress, float fadeStartFraction, float fadeEndFraction);
}
