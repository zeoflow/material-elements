
package com.zeoflow.material.elements.circularreveal;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewAnimationUtils;
import com.zeoflow.material.elements.circularreveal.CircularRevealWidget.CircularRevealEvaluator;
import com.zeoflow.material.elements.circularreveal.CircularRevealWidget.CircularRevealProperty;
import com.zeoflow.material.elements.circularreveal.CircularRevealWidget.RevealInfo;


public final class CircularRevealCompat {

  private CircularRevealCompat() {}

  
  @NonNull
  public static Animator createCircularReveal(
      @NonNull CircularRevealWidget view, float centerX, float centerY, float endRadius) {
    Animator revealInfoAnimator =
        ObjectAnimator.ofObject(
            view,
            CircularRevealProperty.CIRCULAR_REVEAL,
            CircularRevealEvaluator.CIRCULAR_REVEAL,
            new RevealInfo(centerX, centerY, endRadius));
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      
      
      RevealInfo revealInfo = view.getRevealInfo();
      if (revealInfo == null) {
        throw new IllegalStateException(
            "Caller must set a non-null RevealInfo before calling this.");
      }
      float startRadius = revealInfo.radius;
      Animator circularRevealAnimator =
          ViewAnimationUtils.createCircularReveal(
              (View) view, (int) centerX, (int) centerY, startRadius, endRadius);
      AnimatorSet set = new AnimatorSet();
      set.playTogether(revealInfoAnimator, circularRevealAnimator);
      return set;
    } else {
      return revealInfoAnimator;
    }
  }

  
  @NonNull
  public static Animator createCircularReveal(
      CircularRevealWidget view, float centerX, float centerY, float startRadius, float endRadius) {
    Animator revealInfoAnimator =
        ObjectAnimator.ofObject(
            view,
            CircularRevealProperty.CIRCULAR_REVEAL,
            CircularRevealEvaluator.CIRCULAR_REVEAL,
            new RevealInfo(centerX, centerY, startRadius),
            new RevealInfo(centerX, centerY, endRadius));
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      Animator circularRevealAnimator =
          ViewAnimationUtils.createCircularReveal(
              (View) view, (int) centerX, (int) centerY, startRadius, endRadius);
      AnimatorSet set = new AnimatorSet();
      set.playTogether(revealInfoAnimator, circularRevealAnimator);
      return set;
    } else {
      return revealInfoAnimator;
    }
  }

  
  @NonNull
  public static AnimatorListener createCircularRevealListener(
      @NonNull final CircularRevealWidget view) {
    return new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        view.buildCircularRevealCache();
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        view.destroyCircularRevealCache();
      }
    };
  }
}
