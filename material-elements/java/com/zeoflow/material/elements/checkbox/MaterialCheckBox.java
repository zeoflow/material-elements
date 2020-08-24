

package com.zeoflow.material.elements.checkbox;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialCheckBox extends AppCompatCheckBox {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CompoundButton_CheckBox;
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} 
      };
  @Nullable private ColorStateList materialThemeColorsTintList;
  private boolean useMaterialThemeColors;

  public MaterialCheckBox(Context context) {
    this(context, null);
  }

  public MaterialCheckBox(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.checkboxStyle);
  }

  public MaterialCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialCheckBox, defStyleAttr, DEF_STYLE_RES);

    
    
    if (attributes.hasValue(R.styleable.MaterialCheckBox_buttonTint)) {
      CompoundButtonCompat.setButtonTintList(
          this,
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.MaterialCheckBox_buttonTint));
    }

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialCheckBox_useMaterialThemeColors, false);

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && CompoundButtonCompat.getButtonTintList(this) == null) {
      setUseMaterialThemeColors(true);
    }
  }

  
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      CompoundButtonCompat.setButtonTintList(this, getMaterialThemeColorsTintList());
    } else {
      CompoundButtonCompat.setButtonTintList(this, null);
    }
  }

  
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  private ColorStateList getMaterialThemeColorsTintList() {
    if (materialThemeColorsTintList == null) {
      int[] checkBoxColorsList = new int[ENABLED_CHECKED_STATES.length];
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);

      checkBoxColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      checkBoxColorsList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
      checkBoxColorsList[2] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      checkBoxColorsList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);

      materialThemeColorsTintList = new ColorStateList(ENABLED_CHECKED_STATES, checkBoxColorsList);
    }
    return materialThemeColorsTintList;
  }
}
