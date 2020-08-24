

package com.zeoflow.material.elements.transition;

import android.animation.Animator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import androidx.transition.TransitionValues;
import androidx.transition.Visibility;


public final class Hold extends Visibility {

  @NonNull
  @Override
  public Animator onAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return ValueAnimator.ofFloat(0);
  }

  @NonNull
  @Override
  public Animator onDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return ValueAnimator.ofFloat(0);
  }
}
