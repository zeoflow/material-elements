


package com.zeoflow.material.elements.transition.platform;

import static com.zeoflow.material.elements.transition.platform.TransitionUtils.lerp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;


@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class FadeThroughProvider implements VisibilityAnimatorProvider {

  static final float PROGRESS_THRESHOLD = 0.35f;

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeThroughAnimator(
        view,
         0f,
         1f,
         PROGRESS_THRESHOLD,
         1f);
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeThroughAnimator(
        view,
         1f,
         0f,
         0f,
         PROGRESS_THRESHOLD);
  }

  private static Animator createFadeThroughAnimator(
      final View view,
      final float startValue,
      final float endValue,
      final @FloatRange(from = 0.0, to = 1.0) float startFraction,
      final @FloatRange(from = 0.0, to = 1.0) float endFraction) {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(TransitionUtils.lerp(startValue, endValue, startFraction, endFraction, progress));
          }
        });
    return animator;
  }
}
