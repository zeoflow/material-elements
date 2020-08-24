

package com.zeoflow.material.elements.transition;


public final class MaterialFade extends MaterialVisibility<FadeProvider> {

  private static final float DEFAULT_START_SCALE = 0.8f;
  private static final float DEFAULT_FADE_END_THRESHOLD_ENTER = 0.3f;

  public MaterialFade() {
    super(createPrimaryAnimatorProvider(), createSecondaryAnimatorProvider());
  }

  private static FadeProvider createPrimaryAnimatorProvider() {
    FadeProvider fadeProvider = new FadeProvider();
    fadeProvider.setIncomingEndThreshold(DEFAULT_FADE_END_THRESHOLD_ENTER);
    return fadeProvider;
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    ScaleProvider scaleProvider = new ScaleProvider();
    scaleProvider.setScaleOnDisappear(false);
    scaleProvider.setIncomingStartScale(DEFAULT_START_SCALE);
    return scaleProvider;
  }
}
