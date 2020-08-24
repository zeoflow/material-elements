

package com.zeoflow.material.elements.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;


public final class ScaleProvider implements VisibilityAnimatorProvider {

  private float outgoingStartScale = 1f;
  private float outgoingEndScale = 1.1f;
  private float incomingStartScale = 0.8f;
  private float incomingEndScale = 1f;

  private boolean growing;
  private boolean scaleOnDisappear = true;

  public ScaleProvider() {
    this(true);
  }

  public ScaleProvider(boolean growing) {
    this.growing = growing;
  }

  
  public boolean isGrowing() {
    return growing;
  }

  
  public void setGrowing(boolean growing) {
    this.growing = growing;
  }

  
  public boolean isScaleOnDisappear() {
    return scaleOnDisappear;
  }

  
  public void setScaleOnDisappear(boolean scaleOnDisappear) {
    this.scaleOnDisappear = scaleOnDisappear;
  }

  
  public float getOutgoingStartScale() {
    return outgoingStartScale;
  }

  
  public void setOutgoingStartScale(float outgoingStartScale) {
    this.outgoingStartScale = outgoingStartScale;
  }

  
  public float getOutgoingEndScale() {
    return outgoingEndScale;
  }

  
  public void setOutgoingEndScale(float outgoingEndScale) {
    this.outgoingEndScale = outgoingEndScale;
  }

  
  public float getIncomingStartScale() {
    return incomingStartScale;
  }

  
  public void setIncomingStartScale(float incomingStartScale) {
    this.incomingStartScale = incomingStartScale;
  }

  
  public float getIncomingEndScale() {
    return incomingEndScale;
  }

  
  public void setIncomingEndScale(float incomingEndScale) {
    this.incomingEndScale = incomingEndScale;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    if (growing) {
      return createScaleAnimator(view, incomingStartScale, incomingEndScale);
    } else {
      return createScaleAnimator(view, outgoingEndScale, outgoingStartScale);
    }
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    if (!scaleOnDisappear) {
      return null;
    }

    if (growing) {
      return createScaleAnimator(view, outgoingStartScale, outgoingEndScale);
    } else {
      return createScaleAnimator(view, incomingEndScale, incomingStartScale);
    }
  }

  private static Animator createScaleAnimator(View view, float startScale, float endScale) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view,
        PropertyValuesHolder.ofFloat(View.SCALE_X, startScale, endScale),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, startScale, endScale));
  }
}
