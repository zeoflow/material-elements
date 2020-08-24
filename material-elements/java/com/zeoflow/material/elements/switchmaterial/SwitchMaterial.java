

package com.zeoflow.material.elements.switchmaterial;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.elevation.ElevationOverlayProvider;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class SwitchMaterial extends SwitchCompat {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CompoundButton_Switch;
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} 
      };

  @NonNull private final ElevationOverlayProvider elevationOverlayProvider;

  @Nullable private ColorStateList materialThemeColorsThumbTintList;
  @Nullable private ColorStateList materialThemeColorsTrackTintList;
  private boolean useMaterialThemeColors;

  public SwitchMaterial(@NonNull Context context) {
    this(context, null);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.switchStyle);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    elevationOverlayProvider = new ElevationOverlayProvider(context);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.SwitchMaterial, defStyleAttr, DEF_STYLE_RES);

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.SwitchMaterial_useMaterialThemeColors, false);

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && getThumbTintList() == null) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
    }
    if (useMaterialThemeColors && getTrackTintList() == null) {
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    }
  }

  
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    } else {
      setThumbTintList(null);
      setTrackTintList(null);
    }
  }

  
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  private ColorStateList getMaterialThemeColorsThumbTintList() {
    if (materialThemeColorsThumbTintList == null) {
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      float thumbElevation = getResources().getDimension(R.dimen.mtrl_switch_thumb_elevation);
      if (elevationOverlayProvider.isThemeElevationOverlayEnabled()) {
        thumbElevation += ViewUtils.getParentAbsoluteElevation(this);
      }
      int colorThumbOff =
          elevationOverlayProvider.compositeOverlayIfNeeded(colorSurface, thumbElevation);

      int[] switchThumbColorsList = new int[ENABLED_CHECKED_STATES.length];
      switchThumbColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      switchThumbColorsList[1] = colorThumbOff;
      switchThumbColorsList[2] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED);
      switchThumbColorsList[3] = colorThumbOff;
      materialThemeColorsThumbTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, switchThumbColorsList);
    }
    return materialThemeColorsThumbTintList;
  }

  private ColorStateList getMaterialThemeColorsTrackTintList() {
    if (materialThemeColorsTrackTintList == null) {
      int[] switchTrackColorsList = new int[ENABLED_CHECKED_STATES.length];
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
      switchTrackColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_MEDIUM);
      switchTrackColorsList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_LOW);
      switchTrackColorsList[2] =
          MaterialColors.layer(
              colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED_LOW);
      switchTrackColorsList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED_LOW);
      materialThemeColorsTrackTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, switchTrackColorsList);
    }
    return materialThemeColorsTrackTintList;
  }
}
