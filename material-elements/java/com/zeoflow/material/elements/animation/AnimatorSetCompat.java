
package com.zeoflow.material.elements.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.List;


@RestrictTo(Scope.LIBRARY_GROUP)
public class AnimatorSetCompat {

  
  public static void playTogether(@NonNull AnimatorSet animatorSet, @NonNull List<Animator> items) {
    
    
    long totalDuration = 0;
    for (int i = 0, count = items.size(); i < count; i++) {
      Animator animator = items.get(i);
      totalDuration = Math.max(totalDuration, animator.getStartDelay() + animator.getDuration());
    }
    Animator fix = ValueAnimator.ofInt(0, 0);
    fix.setDuration(totalDuration);
    items.add(0, fix);

    animatorSet.playTogether(items);
  }
}
