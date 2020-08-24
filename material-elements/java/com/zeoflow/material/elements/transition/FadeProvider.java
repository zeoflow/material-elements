

package com.zeoflow.material.elements.transition;

import static com.zeoflow.material.elements.transition.TransitionUtils.lerp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;


public final class FadeProvider implements VisibilityAnimatorProvider {

  private float incomingEndThreshold = 1f;

  
  public float getIncomingEndThreshold() {
    return incomingEndThreshold;
  }

  
  public void setIncomingEndThreshold(float incomingEndThreshold) {
    this.incomingEndThreshold = incomingEndThreshold;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeAnimator(
        view,
         0f,
         1f,
         0F,
         incomingEndThreshold);
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createFadeAnimator(
        view,
         1f,
         0f,
         0F,
         1F);
  }

  private static Animator createFadeAnimator(
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
            view.setAlpha(lerp(startValue, endValue, startFraction, endFraction, progress));
          }
        });
    return animator;
  }
}
