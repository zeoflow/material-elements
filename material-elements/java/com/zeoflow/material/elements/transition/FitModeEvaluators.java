

package com.zeoflow.material.elements.transition;

import static com.zeoflow.material.elements.transition.MaterialContainerTransform.FIT_MODE_AUTO;
import static com.zeoflow.material.elements.transition.MaterialContainerTransform.FIT_MODE_HEIGHT;
import static com.zeoflow.material.elements.transition.MaterialContainerTransform.FIT_MODE_WIDTH;
import static com.zeoflow.material.elements.transition.TransitionUtils.lerp;

import android.graphics.RectF;
import com.zeoflow.material.elements.transition.MaterialContainerTransform.FitMode;

class FitModeEvaluators {

  private static final FitModeEvaluator WIDTH =
      new FitModeEvaluator() {
        @Override
        public FitModeResult evaluate(
            float progress,
            float scaleStartFraction,
            float scaleEndFraction,
            float startWidth,
            float startHeight,
            float endWidth,
            float endHeight) {
          
          float currentWidth =
              TransitionUtils.lerp(startWidth, endWidth, scaleStartFraction, scaleEndFraction, progress);
          float startScale = currentWidth / startWidth;
          float endScale = currentWidth / endWidth;
          float currentStartHeight = startHeight * startScale;
          float currentEndHeight = endHeight * endScale;
          return new FitModeResult(
              startScale,
              endScale,
              currentWidth,
              currentStartHeight,
              currentWidth,
              currentEndHeight);
        }

        @Override
        public boolean shouldMaskStartBounds(FitModeResult fitModeResult) {
          return fitModeResult.currentStartHeight > fitModeResult.currentEndHeight;
        }

        @Override
        public void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult) {
          float currentHeightDiff =
              Math.abs(fitModeResult.currentEndHeight - fitModeResult.currentStartHeight);
          maskBounds.bottom -= currentHeightDiff * maskMultiplier;
        }
      };

  private static final FitModeEvaluator HEIGHT =
      new FitModeEvaluator() {
        @Override
        public FitModeResult evaluate(
            float progress,
            float scaleStartFraction,
            float scaleEndFraction,
            float startWidth,
            float startHeight,
            float endWidth,
            float endHeight) {
          
          float currentHeight =
              TransitionUtils.lerp(startHeight, endHeight, scaleStartFraction, scaleEndFraction, progress);
          float startScale = currentHeight / startHeight;
          float endScale = currentHeight / endHeight;
          float currentStartWidth = startWidth * startScale;
          float currentEndWidth = endWidth * endScale;
          return new FitModeResult(
              startScale,
              endScale,
              currentStartWidth,
              currentHeight,
              currentEndWidth,
              currentHeight);
        }

        @Override
        public boolean shouldMaskStartBounds(FitModeResult fitModeResult) {
          return fitModeResult.currentStartWidth > fitModeResult.currentEndWidth;
        }

        @Override
        public void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult) {
          float currentWidthDiff =
              Math.abs(fitModeResult.currentEndWidth - fitModeResult.currentStartWidth);
          maskBounds.left += currentWidthDiff / 2 * maskMultiplier;
          maskBounds.right -= currentWidthDiff / 2 * maskMultiplier;
        }
      };

  static FitModeEvaluator get(
      @FitMode int fitMode, boolean entering, RectF startBounds, RectF endBounds) {
    switch (fitMode) {
      case FIT_MODE_AUTO:
        return shouldAutoFitToWidth(entering, startBounds, endBounds) ? WIDTH : HEIGHT;
      case FIT_MODE_WIDTH:
        return WIDTH;
      case FIT_MODE_HEIGHT:
        return HEIGHT;
      default:
        throw new IllegalArgumentException("Invalid fit mode: " + fitMode);
    }
  }

  private static boolean shouldAutoFitToWidth(
      boolean entering, RectF startBounds, RectF endBounds) {
    float startWidth = startBounds.width();
    float startHeight = startBounds.height();
    float endWidth = endBounds.width();
    float endHeight = endBounds.height();

    float endHeightFitToWidth = endHeight * startWidth / endWidth;
    float startHeightFitToWidth = startHeight * endWidth / startWidth;
    return entering ? endHeightFitToWidth >= startHeight : startHeightFitToWidth >= endHeight;
  }

  private FitModeEvaluators() {}
}
