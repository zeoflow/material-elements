

package com.zeoflow.material.elements.theme.overlay;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.AttributeSet;


public class MaterialThemeOverlay {

  private MaterialThemeOverlay() {
  }

  private static final int[] ANDROID_THEME_OVERLAY_ATTRS =
      new int[] {android.R.attr.theme, R.attr.theme};

  private static final int[] MATERIAL_THEME_OVERLAY_ATTR = new int[] {R.attr.materialThemeOverlay};

  
  @NonNull
  public static Context wrap(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    int materialThemeOverlayId =
        obtainMaterialThemeOverlayId(context, attrs, defStyleAttr, defStyleRes);
    boolean contextHasOverlay = context instanceof ContextThemeWrapper
        && ((ContextThemeWrapper) context).getThemeResId() == materialThemeOverlayId;

    if (materialThemeOverlayId == 0 || contextHasOverlay) {
      return context;
    }

    Context contextThemeWrapper = new ContextThemeWrapper(context, materialThemeOverlayId);

    
    
    int androidThemeOverlayId = obtainAndroidThemeOverlayId(context, attrs);
    if (androidThemeOverlayId != 0) {
      contextThemeWrapper.getTheme().applyStyle(androidThemeOverlayId, true);
    }

    return contextThemeWrapper;
  }

  
  @StyleRes
  private static int obtainAndroidThemeOverlayId(@NonNull Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, ANDROID_THEME_OVERLAY_ATTRS);
    int androidThemeId = a.getResourceId(0 , 0 );
    int appThemeId = a.getResourceId(1 , 0 );
    a.recycle();

    return androidThemeId != 0 ? androidThemeId : appThemeId;
  }

  
  @StyleRes
  private static int obtainMaterialThemeOverlayId(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, MATERIAL_THEME_OVERLAY_ATTR, defStyleAttr, defStyleRes);
    int materialThemeOverlayId = a.getResourceId(0 , 0 );
    a.recycle();

    return materialThemeOverlayId;
  }
}

