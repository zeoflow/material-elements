

package com.zeoflow.material.elements.transition;


public final class MaterialElevationScale extends MaterialVisibility<ScaleProvider> {

  private static final float DEFAULT_SCALE = 0.85f;

  private final boolean growing;

  public MaterialElevationScale(boolean growing) {
    super(createPrimaryAnimatorProvider(growing), createSecondaryAnimatorProvider());
    this.growing = growing;
  }

  public boolean isGrowing() {
    return growing;
  }

  private static ScaleProvider createPrimaryAnimatorProvider(boolean growing) {
    ScaleProvider scaleProvider = new ScaleProvider(growing);
    scaleProvider.setOutgoingEndScale(DEFAULT_SCALE);
    scaleProvider.setIncomingStartScale(DEFAULT_SCALE);
    return scaleProvider;
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    return new FadeProvider();
  }
}
