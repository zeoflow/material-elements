/*
 * Copyright 2020 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.zeoflow.material.elements.transition.platform;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class FadeModeEvaluators
{

  private static final FadeModeEvaluator IN =
      new FadeModeEvaluator()
      {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction)
        {
          int startAlpha = 255;
          int endAlpha = TransitionUtils.lerp(0, 255, fadeStartFraction, fadeEndFraction, progress);
          return FadeModeResult.endOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator OUT =
      new FadeModeEvaluator()
      {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction)
        {
          int startAlpha = TransitionUtils.lerp(255, 0, fadeStartFraction, fadeEndFraction, progress);
          int endAlpha = 255;
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator CROSS =
      new FadeModeEvaluator()
      {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction)
        {
          int startAlpha = TransitionUtils.lerp(255, 0, fadeStartFraction, fadeEndFraction, progress);
          int endAlpha = TransitionUtils.lerp(0, 255, fadeStartFraction, fadeEndFraction, progress);
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  private static final FadeModeEvaluator THROUGH =
      new FadeModeEvaluator()
      {
        @Override
        public FadeModeResult evaluate(
            float progress, float fadeStartFraction, float fadeEndFraction)
        {
          float fadeFractionDiff = fadeEndFraction - fadeStartFraction;
          float fadeFractionThreshold =
              fadeStartFraction + fadeFractionDiff * FadeThroughProvider.PROGRESS_THRESHOLD;
          int startAlpha = TransitionUtils.lerp(255, 0, fadeStartFraction, fadeFractionThreshold, progress);
          int endAlpha = TransitionUtils.lerp(0, 255, fadeFractionThreshold, fadeEndFraction, progress);
          return FadeModeResult.startOnTop(startAlpha, endAlpha);
        }
      };

  private FadeModeEvaluators()
  {
  }

  static FadeModeEvaluator get(@MaterialContainerTransform.FadeMode int fadeMode, boolean entering)
  {
    switch (fadeMode)
    {
      case MaterialContainerTransform.FADE_MODE_IN:
        return entering ? IN : OUT;
      case MaterialContainerTransform.FADE_MODE_OUT:
        return entering ? OUT : IN;
      case MaterialContainerTransform.FADE_MODE_CROSS:
        return CROSS;
      case MaterialContainerTransform.FADE_MODE_THROUGH:
        return THROUGH;
      default:
        throw new IllegalArgumentException("Invalid fade mode: " + fadeMode);
    }
  }
}
