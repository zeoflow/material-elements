

package com.zeoflow.material.elements.animation;

import android.animation.TimeInterpolator;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;


@RestrictTo(Scope.LIBRARY_GROUP)
public class AnimationUtils {

  public static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  public static final TimeInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR =
      new FastOutSlowInInterpolator();
  public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR =
      new FastOutLinearInInterpolator();
  public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR =
      new LinearOutSlowInInterpolator();
  public static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

  
  public static float lerp(float startValue, float endValue, float fraction) {
    return startValue + (fraction * (endValue - startValue));
  }

  
  public static int lerp(int startValue, int endValue, float fraction) {
    return startValue + Math.round(fraction * (endValue - startValue));
  }
}
