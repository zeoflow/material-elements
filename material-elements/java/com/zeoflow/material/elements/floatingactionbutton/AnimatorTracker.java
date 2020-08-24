

package com.zeoflow.material.elements.floatingactionbutton;

import android.animation.Animator;
import androidx.annotation.Nullable;


class AnimatorTracker {

  @Nullable private Animator currentAnimator;

  public void onNextAnimationStart(Animator animator) {
    cancelCurrent();
    currentAnimator = animator;
  }

  public void cancelCurrent() {
    if (currentAnimator != null) {
      currentAnimator.cancel();
    }
  }

  public void clear() {
    currentAnimator = null;
  }
}
