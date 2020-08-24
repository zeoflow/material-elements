

package com.zeoflow.material.elements.floatingactionbutton;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;
import android.view.View;
import com.zeoflow.material.elements.animation.AnimatorSetCompat;
import com.zeoflow.material.elements.animation.MotionSpec;
import java.util.ArrayList;
import java.util.List;


abstract class BaseMotionStrategy implements MotionStrategy {

  private final Context context;
  @NonNull private final ExtendedFloatingActionButton fab;
  private final ArrayList<AnimatorListener> listeners = new ArrayList<>();
  private final AnimatorTracker tracker;

  @Nullable private MotionSpec defaultMotionSpec;
  @Nullable private MotionSpec motionSpec;

  BaseMotionStrategy(@NonNull ExtendedFloatingActionButton fab, AnimatorTracker tracker) {
    this.fab = fab;
    this.context = fab.getContext();
    this.tracker = tracker;
  }

  @Override
  public final void setMotionSpec(@Nullable MotionSpec motionSpec) {
    this.motionSpec = motionSpec;
  }

  @Override
  public final MotionSpec getCurrentMotionSpec() {
    if (motionSpec != null) {
      return motionSpec;
    }

    if (defaultMotionSpec == null) {
      defaultMotionSpec =
          MotionSpec.createFromResource(
              context,
              getDefaultMotionSpecResource());
    }

    return Preconditions.checkNotNull(defaultMotionSpec);
  }

  @Override
  public final void addAnimationListener(@NonNull AnimatorListener listener) {
    listeners.add(listener);
  }

  @Override
  public final void removeAnimationListener(@NonNull AnimatorListener listener) {
    listeners.remove(listener);
  }

  @NonNull
  @Override
  public final List<AnimatorListener> getListeners() {
    return listeners;
  }

  @Override
  @Nullable
  public MotionSpec getMotionSpec() {
    return motionSpec;
  }

  @Override
  @CallSuper
  public void onAnimationStart(Animator animator) {
    tracker.onNextAnimationStart(animator);
  }

  @Override
  @CallSuper
  public void onAnimationEnd() {
    tracker.clear();
  }

  @Override
  @CallSuper
  public void onAnimationCancel() {
    tracker.clear();
  }

  @Override
  public AnimatorSet createAnimator() {
    return createAnimator(getCurrentMotionSpec());
  }

  @NonNull
  AnimatorSet createAnimator(@NonNull MotionSpec spec) {
    List<Animator> animators = new ArrayList<>();

    if (spec.hasPropertyValues("opacity")) {
      animators.add(spec.getAnimator("opacity", fab, View.ALPHA));
    }

    if (spec.hasPropertyValues("scale")) {
      animators.add(spec.getAnimator("scale", fab, View.SCALE_Y));
      animators.add(spec.getAnimator("scale", fab, View.SCALE_X));
    }

    if (spec.hasPropertyValues("width")) {
      animators.add(spec.getAnimator("width", fab, ExtendedFloatingActionButton.WIDTH));
    }

    if (spec.hasPropertyValues("height")) {
      animators.add(spec.getAnimator("height", fab, ExtendedFloatingActionButton.HEIGHT));
    }

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }
}
