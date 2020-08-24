
package com.zeoflow.material.elements.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;


public class MotionTiming {

  private long delay = 0;
  private long duration = 300;
  
  @Nullable private TimeInterpolator interpolator = null;
  
  private int repeatCount = 0;
  
  private int repeatMode = ValueAnimator.RESTART;

  public MotionTiming(long delay, long duration) {
    this.delay = delay;
    this.duration = duration;
  }

  public MotionTiming(long delay, long duration, @NonNull TimeInterpolator interpolator) {
    this.delay = delay;
    this.duration = duration;
    this.interpolator = interpolator;
  }

  public void apply(@NonNull Animator animator) {
    animator.setStartDelay(getDelay());
    animator.setDuration(getDuration());
    animator.setInterpolator(getInterpolator());
    if (animator instanceof ValueAnimator) {
      ((ValueAnimator) animator).setRepeatCount(getRepeatCount());
      ((ValueAnimator) animator).setRepeatMode(getRepeatMode());
    }
  }

  public long getDelay() {
    return delay;
  }

  public long getDuration() {
    return duration;
  }

  @Nullable
  public TimeInterpolator getInterpolator() {
    return interpolator != null ? interpolator : AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public int getRepeatMode() {
    return repeatMode;
  }

  @NonNull
  static MotionTiming createFromAnimator(@NonNull ValueAnimator animator) {
    MotionTiming timing =
        new MotionTiming(
            animator.getStartDelay(), animator.getDuration(), getInterpolatorCompat(animator));
    timing.repeatCount = animator.getRepeatCount();
    timing.repeatMode = animator.getRepeatMode();
    return timing;
  }

  
  private static TimeInterpolator getInterpolatorCompat(@NonNull ValueAnimator animator) {
    @Nullable TimeInterpolator interpolator = animator.getInterpolator();
    if (interpolator instanceof AccelerateDecelerateInterpolator || interpolator == null) {
      return AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
    } else if (interpolator instanceof AccelerateInterpolator) {
      return AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;
    } else if (interpolator instanceof DecelerateInterpolator) {
      return AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR;
    } else {
      return interpolator;
    }
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MotionTiming)) {
      return false;
    }

    MotionTiming that = (MotionTiming) o;

    if (getDelay() != that.getDelay()) {
      return false;
    }
    if (getDuration() != that.getDuration()) {
      return false;
    }
    if (getRepeatCount() != that.getRepeatCount()) {
      return false;
    }
    if (getRepeatMode() != that.getRepeatMode()) {
      return false;
    }
    return getInterpolator().getClass().equals(that.getInterpolator().getClass());
  }

  @Override
  public int hashCode() {
    int result = (int) (getDelay() ^ (getDelay() >>> 32));
    result = 31 * result + (int) (getDuration() ^ (getDuration() >>> 32));
    result = 31 * result + getInterpolator().getClass().hashCode();
    result = 31 * result + getRepeatCount();
    result = 31 * result + getRepeatMode();
    return result;
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append('\n');
    out.append(getClass().getName());
    out.append('{');
    out.append(Integer.toHexString(System.identityHashCode(this)));
    out.append(" delay: ");
    out.append(getDelay());
    out.append(" duration: ");
    out.append(getDuration());
    out.append(" interpolator: ");
    out.append(getInterpolator().getClass());
    out.append(" repeatCount: ");
    out.append(getRepeatCount());
    out.append(" repeatMode: ");
    out.append(getRepeatMode());
    out.append("}\n");
    return out.toString();
  }
}
