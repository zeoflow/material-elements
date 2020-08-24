

package com.zeoflow.material.elements.floatingactionbutton;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zeoflow.material.elements.animation.MotionSpec;

import java.util.List;


interface MotionStrategy {

  void performNow();

  MotionSpec getCurrentMotionSpec();

  @AnimatorRes int getDefaultMotionSpecResource();

  void setMotionSpec(@Nullable MotionSpec spec);

  @Nullable MotionSpec getMotionSpec();

  AnimatorSet createAnimator();

  void addAnimationListener(@NonNull AnimatorListener listener);

  void removeAnimationListener(@NonNull AnimatorListener listener);

  List<AnimatorListener> getListeners();

  void onAnimationStart(Animator animator);

  void onAnimationEnd();

  void onAnimationCancel();

  void onChange(@Nullable ExtendedFloatingActionButton.OnChangedCallback callback);

  boolean shouldCancel();
}


