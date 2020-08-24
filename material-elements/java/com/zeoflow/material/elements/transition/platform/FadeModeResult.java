


package com.zeoflow.material.elements.transition.platform;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class FadeModeResult {
  final int startAlpha;
  final int endAlpha;
  final boolean endOnTop;

  static FadeModeResult startOnTop(int startAlpha, int endAlpha) {
    return new FadeModeResult(startAlpha, endAlpha, false);
  }

  static FadeModeResult endOnTop(int startAlpha, int endAlpha) {
    return new FadeModeResult(startAlpha, endAlpha, true);
  }

  private FadeModeResult(int startAlpha, int endAlpha, boolean endOnTop) {
    this.startAlpha = startAlpha;
    this.endAlpha = endAlpha;
    this.endOnTop = endOnTop;
  }
}
