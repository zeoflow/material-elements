

package com.zeoflow.material.elements.elevation;

import com.google.android.material.R;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import android.view.View;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialAttributes;


public class ElevationOverlayProvider {

  private static final float FORMULA_MULTIPLIER = 4.5f;
  private static final float FORMULA_OFFSET = 2f;

  private final boolean elevationOverlayEnabled;
  private final int elevationOverlayColor;
  private final int colorSurface;
  private final float displayDensity;

  public ElevationOverlayProvider(@NonNull Context context) {
    this.elevationOverlayEnabled =
        MaterialAttributes.resolveBoolean(context, R.attr.elevationOverlayEnabled, false);
    this.elevationOverlayColor =
        MaterialColors.getColor(context, R.attr.elevationOverlayColor, Color.TRANSPARENT);
    this.colorSurface = MaterialColors.getColor(context, R.attr.colorSurface, Color.TRANSPARENT);
    this.displayDensity = context.getResources().getDisplayMetrics().density;
  }

  
  @ColorInt
  public int compositeOverlayWithThemeSurfaceColorIfNeeded(
      float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlayWithThemeSurfaceColorIfNeeded(elevation);
  }

  
  @ColorInt
  public int compositeOverlayWithThemeSurfaceColorIfNeeded(float elevation) {
    return compositeOverlayIfNeeded(colorSurface, elevation);
  }

  
  @ColorInt
  public int compositeOverlayIfNeeded(
      @ColorInt int backgroundColor, float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlayIfNeeded(backgroundColor, elevation);
  }

  
  @ColorInt
  public int compositeOverlayIfNeeded(@ColorInt int backgroundColor, float elevation) {
    if (elevationOverlayEnabled && isThemeSurfaceColor(backgroundColor)) {
      return compositeOverlay(backgroundColor, elevation);
    } else {
      return backgroundColor;
    }
  }

  
  @ColorInt
  public int compositeOverlay(
      @ColorInt int backgroundColor, float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlay(backgroundColor, elevation);
  }

  
  @ColorInt
  public int compositeOverlay(@ColorInt int backgroundColor, float elevation) {
    float overlayAlphaFraction = calculateOverlayAlphaFraction(elevation);
    int backgroundAlpha = Color.alpha(backgroundColor);
    int backgroundColorOpaque = ColorUtils.setAlphaComponent(backgroundColor, 255);
    int overlayColorOpaque =
        MaterialColors.layer(backgroundColorOpaque, elevationOverlayColor, overlayAlphaFraction);
    return ColorUtils.setAlphaComponent(overlayColorOpaque, backgroundAlpha);
  }

  
  public int calculateOverlayAlpha(float elevation) {
    return Math.round(calculateOverlayAlphaFraction(elevation) * 255);
  }

  
  public float calculateOverlayAlphaFraction(float elevation) {
    if (displayDensity <= 0 || elevation <= 0) {
      return 0;
    }
    float elevationDp = elevation / displayDensity;
    float alphaFraction =
        (FORMULA_MULTIPLIER * (float) Math.log1p(elevationDp) + FORMULA_OFFSET) / 100;
    return Math.min(alphaFraction, 1);
  }

  
  public boolean isThemeElevationOverlayEnabled() {
    return elevationOverlayEnabled;
  }

  
  @ColorInt
  public int getThemeElevationOverlayColor() {
    return elevationOverlayColor;
  }

  
  @ColorInt
  public int getThemeSurfaceColor() {
    return colorSurface;
  }

  
  public float getParentAbsoluteElevation(@NonNull View overlayView) {
    return ViewUtils.getParentAbsoluteElevation(overlayView);
  }

  private boolean isThemeSurfaceColor(@ColorInt int color) {
    return ColorUtils.setAlphaComponent(color, 255) == colorSurface;
  }
}
