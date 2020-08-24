


package com.zeoflow.material.elements.transition.platform;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class SlideDistanceProvider implements VisibilityAnimatorProvider {

  private static final int DEFAULT_DISTANCE = -1;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM, Gravity.START, Gravity.END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface GravityFlag {}

  @GravityFlag private int slideEdge;
  @Px private int slideDistance = DEFAULT_DISTANCE;

  public SlideDistanceProvider(@GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
  }

  @GravityFlag
  public int getSlideEdge() {
    return slideEdge;
  }

  public void setSlideEdge(@GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
  }

  
  @Px
  public int getSlideDistance() {
    return slideDistance;
  }

  
  public void setSlideDistance(@Px int slideDistance) {
    if (slideDistance < 0) {
      throw new IllegalArgumentException(
          "Slide distance must be positive. If attempting to reverse the direction of the slide,"
              + " use setSlideEdge(int) instead.");
    }
    this.slideDistance = slideDistance;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createTranslationAppearAnimator(
        sceneRoot, view, slideEdge, getSlideDistanceOrDefault(view.getContext()));
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createTranslationDisappearAnimator(
        sceneRoot, view, slideEdge, getSlideDistanceOrDefault(view.getContext()));
  }

  private int getSlideDistanceOrDefault(Context context) {
    if (slideDistance != DEFAULT_DISTANCE) {
      return slideDistance;
    }

    return context
        .getResources()
        .getDimensionPixelSize(R.dimen.mtrl_transition_shared_axis_slide_distance);
  }

  private static Animator createTranslationAppearAnimator(
      View sceneRoot, View view, @GravityFlag int slideEdge, @Px int slideDistance) {
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, slideDistance, 0);
      case Gravity.TOP:
        return createTranslationYAnimator(view, -slideDistance, 0);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, -slideDistance, 0);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, slideDistance, 0);
      case Gravity.START:
        return createTranslationXAnimator(
            view, isRtl(sceneRoot) ? slideDistance : -slideDistance, 0);
      case Gravity.END:
        return createTranslationXAnimator(
            view, isRtl(sceneRoot) ? -slideDistance : slideDistance, 0);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private static Animator createTranslationDisappearAnimator(
      View sceneRoot, View view, @GravityFlag int slideEdge, @Px int slideDistance) {
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, 0, -slideDistance);
      case Gravity.TOP:
        return createTranslationYAnimator(view, 0, slideDistance);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, 0, slideDistance);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, 0, -slideDistance);
      case Gravity.START:
        return createTranslationXAnimator(
            view, 0, isRtl(sceneRoot) ? -slideDistance : slideDistance);
      case Gravity.END:
        return createTranslationXAnimator(
            view, 0, isRtl(sceneRoot) ? slideDistance : -slideDistance);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private static Animator createTranslationXAnimator(
      View view, float startTranslation, float endTranslation) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view, PropertyValuesHolder.ofFloat(View.TRANSLATION_X, startTranslation, endTranslation));
  }

  private static Animator createTranslationYAnimator(
      View view, float startTranslation, float endTranslation) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view, PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, startTranslation, endTranslation));
  }

  private static boolean isRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
