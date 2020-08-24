
package com.zeoflow.material.elements.color;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import android.util.TypedValue;
import android.view.View;
import com.zeoflow.material.elements.resources.MaterialAttributes;


public class MaterialColors {

  public static final float ALPHA_FULL = 1.00F;
  public static final float ALPHA_MEDIUM = 0.54F;
  public static final float ALPHA_DISABLED = 0.38F;
  public static final float ALPHA_LOW = 0.32F;
  public static final float ALPHA_DISABLED_LOW = 0.12F;

  private MaterialColors() {
    
  }

  
  @ColorInt
  public static int getColor(@NonNull View view, @AttrRes int colorAttributeResId) {
    return MaterialAttributes.resolveOrThrow(view, colorAttributeResId);
  }

  
  @ColorInt
  public static int getColor(
      Context context, @AttrRes int colorAttributeResId, String errorMessageComponent) {
    return MaterialAttributes.resolveOrThrow(context, colorAttributeResId, errorMessageComponent);
  }

  
  @ColorInt
  public static int getColor(
      @NonNull View view, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    return getColor(view.getContext(), colorAttributeResId, defaultValue);
  }

  
  @ColorInt
  public static int getColor(
      @NonNull Context context, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    TypedValue typedValue = MaterialAttributes.resolve(context, colorAttributeResId);
    if (typedValue != null) {
      return typedValue.data;
    } else {
      return defaultValue;
    }
  }

  
  @ColorInt
  public static int layer(
      @NonNull View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId) {
    return layer(view, backgroundColorAttributeResId, overlayColorAttributeResId, 1f);
  }

  
  @ColorInt
  public static int layer(
      @NonNull View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int backgroundColor = getColor(view, backgroundColorAttributeResId);
    int overlayColor = getColor(view, overlayColorAttributeResId);
    return layer(backgroundColor, overlayColor, overlayAlpha);
  }

  
  @ColorInt
  public static int layer(
      @ColorInt int backgroundColor,
      @ColorInt int overlayColor,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int computedAlpha = Math.round(Color.alpha(overlayColor) * overlayAlpha);
    int computedOverlayColor = ColorUtils.setAlphaComponent(overlayColor, computedAlpha);
    return layer(backgroundColor, computedOverlayColor);
  }

  
  @ColorInt
  public static int layer(@ColorInt int backgroundColor, @ColorInt int overlayColor) {
    return ColorUtils.compositeColors(overlayColor, backgroundColor);
  }

  
  @ColorInt
  public static int compositeARGBWithAlpha(
      @ColorInt int originalARGB, @IntRange(from = 0, to = 255) int alpha) {
    alpha = Color.alpha(originalARGB) * alpha / 255;
    return ColorUtils.setAlphaComponent(originalARGB, alpha);
  }
}
