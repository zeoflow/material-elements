

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.StateSet;
import java.util.ArrayList;


@RestrictTo(LIBRARY_GROUP)
public final class StateListAnimator {

  private final ArrayList<Tuple> tuples = new ArrayList<>();

  @Nullable private Tuple lastMatch = null;
  @Nullable ValueAnimator runningAnimator = null;

  private final ValueAnimator.AnimatorListener animationListener =
      new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
          if (runningAnimator == animator) {
            runningAnimator = null;
          }
        }
      };

  
  public void addState(int[] specs, ValueAnimator animator) {
    Tuple tuple = new Tuple(specs, animator);
    animator.addListener(animationListener);
    tuples.add(tuple);
  }

  
  public void setState(int[] state) {
    Tuple match = null;
    final int count = tuples.size();
    for (int i = 0; i < count; i++) {
      final Tuple tuple = tuples.get(i);
      if (StateSet.stateSetMatches(tuple.specs, state)) {
        match = tuple;
        break;
      }
    }
    if (match == lastMatch) {
      return;
    }
    if (lastMatch != null) {
      cancel();
    }

    lastMatch = match;

    if (match != null) {
      start(match);
    }
  }

  private void start(@NonNull Tuple match) {
    runningAnimator = match.animator;
    runningAnimator.start();
  }

  private void cancel() {
    if (runningAnimator != null) {
      runningAnimator.cancel();
      runningAnimator = null;
    }
  }

  
  public void jumpToCurrentState() {
    if (runningAnimator != null) {
      runningAnimator.end();
      runningAnimator = null;
    }
  }

  static class Tuple {
    final int[] specs;
    final ValueAnimator animator;

    Tuple(int[] specs, ValueAnimator animator) {
      this.specs = specs;
      this.animator = animator;
    }
  }
}
