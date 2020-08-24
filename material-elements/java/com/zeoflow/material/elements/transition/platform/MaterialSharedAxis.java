


package com.zeoflow.material.elements.transition.platform;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import android.view.Gravity;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialSharedAxis extends MaterialVisibility<VisibilityAnimatorProvider> {

  
  public static final int X = 0;

  
  public static final int Y = 1;

  
  public static final int Z = 2;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({X, Y, Z})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Axis {}

  @Axis private final int axis;
  private final boolean forward;

  public MaterialSharedAxis(@Axis int axis, boolean forward) {
    super(createPrimaryAnimatorProvider(axis, forward), createSecondaryAnimatorProvider());
    this.axis = axis;
    this.forward = forward;
  }

  @Axis
  public int getAxis() {
    return axis;
  }

  public boolean isForward() {
    return forward;
  }

  private static VisibilityAnimatorProvider createPrimaryAnimatorProvider(
      @Axis int axis, boolean forward) {
    switch (axis) {
      case X:
        return new SlideDistanceProvider(forward ? Gravity.END : Gravity.START);
      case Y:
        return new SlideDistanceProvider(forward ? Gravity.BOTTOM : Gravity.TOP);
      case Z:
        return new ScaleProvider(forward);
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    return new FadeThroughProvider();
  }
}
