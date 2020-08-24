

package com.zeoflow.material.elements.resources;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TintTypedArray;
import android.util.TypedValue;


@RestrictTo(LIBRARY_GROUP)
public class MaterialResources {

  private MaterialResources() {}

  
  @Nullable
  public static ColorStateList getColorStateList(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }

    
    
    if (VERSION.SDK_INT <= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      int color = attributes.getColor(index, -1);
      if (color != -1) {
        return ColorStateList.valueOf(color);
      }
    }

    return attributes.getColorStateList(index);
  }

  
  @Nullable
  public static ColorStateList getColorStateList(
      @NonNull Context context, @NonNull TintTypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }

    
    
    if (VERSION.SDK_INT <= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      int color = attributes.getColor(index, -1);
      if (color != -1) {
        return ColorStateList.valueOf(color);
      }
    }

    return attributes.getColorStateList(index);
  }

  
  @Nullable
  public static Drawable getDrawable(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        Drawable value = AppCompatResources.getDrawable(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }
    return attributes.getDrawable(index);
  }

  
  @Nullable
  public static TextAppearance getTextAppearance(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        return new TextAppearance(context, resourceId);
      }
    }
    return null;
  }

  
  public static int getDimensionPixelSize(
      @NonNull Context context,
      @NonNull TypedArray attributes,
      @StyleableRes int index,
      final int defaultValue) {
    TypedValue value = new TypedValue();
    if (!attributes.getValue(index, value) || value.type != TypedValue.TYPE_ATTRIBUTE) {
      return attributes.getDimensionPixelSize(index, defaultValue);
    }

    TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(new int[] {value.data});
    int dimension = styledAttrs.getDimensionPixelSize(0, defaultValue);
    styledAttrs.recycle();
    return dimension;
  }

  
  @StyleableRes
  static int getIndexWithValue(
      @NonNull TypedArray attributes, @StyleableRes int a, @StyleableRes int b) {
    if (attributes.hasValue(a)) {
      return a;
    }
    return b;
  }
}
