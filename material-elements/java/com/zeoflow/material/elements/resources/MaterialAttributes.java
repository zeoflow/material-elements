
package com.zeoflow.material.elements.resources;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import android.util.TypedValue;
import android.view.View;


@RestrictTo(LIBRARY_GROUP)
public class MaterialAttributes {

  
  @Nullable
  public static TypedValue resolve(@NonNull Context context, @AttrRes int attributeResId) {
    TypedValue typedValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
      return typedValue;
    }
    return null;
  }

  
  public static int resolveOrThrow(
      @NonNull Context context,
      @AttrRes int attributeResId,
      @NonNull String errorMessageComponent) {
    TypedValue typedValue = resolve(context, attributeResId);
    if (typedValue == null) {
      String errorMessage =
          "%1$s requires a value for the %2$s attribute to be set in your app theme. "
              + "You can either set the attribute in your theme or "
              + "update your theme to inherit from Theme.MaterialComponents (or a descendant).";
      throw new IllegalArgumentException(
          String.format(
              errorMessage,
              errorMessageComponent,
              context.getResources().getResourceName(attributeResId)));
    }
    return typedValue.data;
  }

  
  public static int resolveOrThrow(@NonNull View componentView, @AttrRes int attributeResId) {
    return resolveOrThrow(
        componentView.getContext(), attributeResId, componentView.getClass().getCanonicalName());
  }

  
  public static boolean resolveBooleanOrThrow(
      @NonNull Context context,
      @AttrRes int attributeResId,
      @NonNull String errorMessageComponent) {
    return resolveOrThrow(context, attributeResId, errorMessageComponent) != 0;
  }

  
  public static boolean resolveBoolean(
      @NonNull Context context, @AttrRes int attributeResId, boolean defaultValue) {
    TypedValue typedValue = resolve(context, attributeResId);
    return (typedValue != null && typedValue.type == TypedValue.TYPE_INT_BOOLEAN)
        ? typedValue.data != 0
        : defaultValue;
  }

  
  @Px
  public static int resolveMinimumAccessibleTouchTarget(@NonNull Context context) {
    return resolveDimension(context, R.attr.minTouchTargetSize, R.dimen.mtrl_min_touch_target_size);
  }

  
  @Px
  public static int resolveDimension(
      @NonNull Context context, @AttrRes int attributeResId, @DimenRes int defaultDimenResId) {
    TypedValue dimensionValue = resolve(context, attributeResId);
    if (dimensionValue == null || dimensionValue.type != TypedValue.TYPE_DIMENSION) {
      return (int) context.getResources().getDimension(defaultDimenResId);
    } else {
      return (int) dimensionValue.getDimension(context.getResources().getDisplayMetrics());
    }
  }
}
