

package com.zeoflow.material.elements.internal;

import com.google.android.material.R;
import com.zeoflow.material.elements.resources.MaterialResources;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;


@RestrictTo(LIBRARY_GROUP)
public final class ThemeEnforcement {

  private static final int[] APPCOMPAT_CHECK_ATTRS = {R.attr.colorPrimary};
  private static final String APPCOMPAT_THEME_NAME = "Theme.AppCompat";

  private static final int[] MATERIAL_CHECK_ATTRS = {R.attr.colorPrimaryVariant};
  private static final String MATERIAL_THEME_NAME = "Theme.MaterialComponents";

  private ThemeEnforcement() {}

  
  @NonNull
  public static TypedArray obtainStyledAttributes(
      @NonNull Context context,
      AttributeSet set,
      @NonNull @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {

    
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    
    checkTextAppearance(context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);

    
    return context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
  }

  
  public static TintTypedArray obtainTintedStyledAttributes(
      @NonNull Context context,
      AttributeSet set,
      @NonNull @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @StyleableRes int... textAppearanceResIndices) {

    
    checkCompatibleTheme(context, set, defStyleAttr, defStyleRes);

    
    checkTextAppearance(context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);

    
    return TintTypedArray.obtainStyledAttributes(context, set, attrs, defStyleAttr, defStyleRes);
  }

  private static void checkCompatibleTheme(
      @NonNull Context context,
      AttributeSet set,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes);
    boolean enforceMaterialTheme =
        a.getBoolean(R.styleable.ThemeEnforcement_enforceMaterialTheme, false);
    a.recycle();

    if (enforceMaterialTheme) {
      TypedValue isMaterialTheme = new TypedValue();
      boolean resolvedValue =
          context.getTheme().resolveAttribute(R.attr.isMaterialTheme, isMaterialTheme, true);

      if (!resolvedValue
          || (isMaterialTheme.type == TypedValue.TYPE_INT_BOOLEAN && isMaterialTheme.data == 0)) {
        
        
        checkMaterialTheme(context);
      }
    }
    checkAppCompatTheme(context);
  }

  private static void checkTextAppearance(
      @NonNull Context context,
      AttributeSet set,
      @NonNull @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @Nullable @StyleableRes int... textAppearanceResIndices) {
    TypedArray themeEnforcementAttrs =
        context.obtainStyledAttributes(
            set, R.styleable.ThemeEnforcement, defStyleAttr, defStyleRes);
    boolean enforceTextAppearance =
        themeEnforcementAttrs.getBoolean(R.styleable.ThemeEnforcement_enforceTextAppearance, false);

    if (!enforceTextAppearance) {
      themeEnforcementAttrs.recycle();
      return;
    }

    boolean validTextAppearance;

    if (textAppearanceResIndices == null || textAppearanceResIndices.length == 0) {
      
      validTextAppearance =
          themeEnforcementAttrs.getResourceId(
                  R.styleable.ThemeEnforcement_android_textAppearance, -1)
              != -1;
    } else {
      
      validTextAppearance =
          isCustomTextAppearanceValid(
              context, set, attrs, defStyleAttr, defStyleRes, textAppearanceResIndices);
    }

    themeEnforcementAttrs.recycle();

    if (!validTextAppearance) {
      throw new IllegalArgumentException(
          "This component requires that you specify a valid TextAppearance attribute. Update your "
              + "app theme to inherit from Theme.MaterialComponents (or a descendant).");
    }
  }

  private static boolean isCustomTextAppearanceValid(
      @NonNull Context context,
      AttributeSet set,
      @NonNull @StyleableRes int[] attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @NonNull @StyleableRes int... textAppearanceResIndices) {
    TypedArray componentAttrs =
        context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
    for (int customTextAppearanceIndex : textAppearanceResIndices) {
      if (componentAttrs.getResourceId(customTextAppearanceIndex, -1) == -1) {
        componentAttrs.recycle();
        return false;
      }
    }
    componentAttrs.recycle();
    return true;
  }

  public static void checkAppCompatTheme(@NonNull Context context) {
    checkTheme(context, APPCOMPAT_CHECK_ATTRS, APPCOMPAT_THEME_NAME);
  }

  public static void checkMaterialTheme(@NonNull Context context) {
    checkTheme(context, MATERIAL_CHECK_ATTRS, MATERIAL_THEME_NAME);
  }

  public static boolean isAppCompatTheme(@NonNull Context context) {
    return isTheme(context, APPCOMPAT_CHECK_ATTRS);
  }

  public static boolean isMaterialTheme(@NonNull Context context) {
    return isTheme(context, MATERIAL_CHECK_ATTRS);
  }

  private static boolean isTheme(@NonNull Context context, @NonNull int[] themeAttributes) {
    TypedArray a = context.obtainStyledAttributes(themeAttributes);
    for (int i = 0; i < themeAttributes.length; i++) {
      if (!a.hasValue(i)) {
        a.recycle();
        return false;
      }
    }
    a.recycle();
    return true;
  }

  private static void checkTheme(
      @NonNull Context context, @NonNull int[] themeAttributes, String themeName) {
    if (!isTheme(context, themeAttributes)) {
      throw new IllegalArgumentException(
          "The style on this component requires your app theme to be "
              + themeName
              + " (or a descendant).");
    }
  }
}
