

package com.zeoflow.material.elements.shape;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.View;
import com.zeoflow.material.elements.internal.ViewUtils;


public class MaterialShapeUtils {

  private MaterialShapeUtils() {}

  @NonNull
  static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily) {
    switch (cornerFamily) {
      case CornerFamily.ROUNDED:
        return new RoundedCornerTreatment();
      case CornerFamily.CUT:
        return new CutCornerTreatment();
      default:
        return createDefaultCornerTreatment();
    }
  }

  @NonNull
  static CornerTreatment createDefaultCornerTreatment() {
    return new RoundedCornerTreatment();
  }

  @NonNull
  static EdgeTreatment createDefaultEdgeTreatment() {
    return new EdgeTreatment();
  }

  
  public static void setElevation(@NonNull View view, float elevation) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) background).setElevation(elevation);
    }
  }

  
  public static void setParentAbsoluteElevation(@NonNull View view) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      setParentAbsoluteElevation(view, (MaterialShapeDrawable) background);
    }
  }

  
  public static void setParentAbsoluteElevation(
      @NonNull View view, @NonNull MaterialShapeDrawable materialShapeDrawable) {
    if (materialShapeDrawable.isElevationOverlayEnabled()) {
      materialShapeDrawable.setParentAbsoluteElevation(ViewUtils.getParentAbsoluteElevation(view));
    }
  }
}
