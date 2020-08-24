


package com.zeoflow.material.elements.transition.platform;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class FitModeResult {
  final float startScale;
  final float endScale;
  final float currentStartWidth;
  final float currentStartHeight;
  final float currentEndWidth;
  final float currentEndHeight;

  FitModeResult(
      float startScale,
      float endScale,
      float currentStartWidth,
      float currentStartHeight,
      float currentEndWidth,
      float currentEndHeight) {
    this.startScale = startScale;
    this.endScale = endScale;
    this.currentStartWidth = currentStartWidth;
    this.currentStartHeight = currentStartHeight;
    this.currentEndWidth = currentEndWidth;
    this.currentEndHeight = currentEndHeight;
  }
}
