

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;
import java.util.Map;


@RestrictTo(LIBRARY_GROUP)
public class TextScale extends Transition {
  private static final String PROPNAME_SCALE = "android:textscale:scale";

  @Override
  public void captureStartValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  @Override
  public void captureEndValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  private void captureValues(@NonNull TransitionValues transitionValues) {
    if (transitionValues.view instanceof TextView) {
      TextView textview = (TextView) transitionValues.view;
      transitionValues.values.put(PROPNAME_SCALE, textview.getScaleX());
    }
  }

  @Override
  public Animator createAnimator(
      @NonNull ViewGroup sceneRoot,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (startValues == null
        || endValues == null
        || !(startValues.view instanceof TextView)
        || !(endValues.view instanceof TextView)) {
      return null;
    }
    final TextView view = (TextView) endValues.view;
    Map<String, Object> startVals = startValues.values;
    Map<String, Object> endVals = endValues.values;
    final float startSize =
        startVals.get(PROPNAME_SCALE) != null ? (float) startVals.get(PROPNAME_SCALE) : 1f;
    final float endSize =
        endVals.get(PROPNAME_SCALE) != null ? (float) endVals.get(PROPNAME_SCALE) : 1f;
    if (startSize == endSize) {
      return null;
    }

    ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);

    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            view.setScaleX(animatedValue);
            view.setScaleY(animatedValue);
          }
        });
    return animator;
  }
}
