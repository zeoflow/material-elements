


package com.zeoflow.material.elements.transition.platform;


@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialFadeThrough extends MaterialVisibility<FadeThroughProvider> {

  private static final float DEFAULT_START_SCALE = 0.92f;

  public MaterialFadeThrough() {
    super(createPrimaryAnimatorProvider(), createSecondaryAnimatorProvider());
  }

  private static FadeThroughProvider createPrimaryAnimatorProvider() {
    return new FadeThroughProvider();
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    ScaleProvider scaleProvider = new ScaleProvider();
    scaleProvider.setScaleOnDisappear(false);
    scaleProvider.setIncomingStartScale(DEFAULT_START_SCALE);
    return scaleProvider;
  }
}
