

package com.zeoflow.material.elements.radiobutton;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialRadioButton extends AppCompatRadioButton {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CompoundButton_RadioButton;
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, 
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} 
      };
  @Nullable private ColorStateList materialThemeColorsTintList;
  private boolean useMaterialThemeColors;

  public MaterialRadioButton(@NonNull Context context) {
    this(context, null);
  }

  public MaterialRadioButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.radioButtonStyle);
  }

  public MaterialRadioButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialRadioButton, defStyleAttr, DEF_STYLE_RES);

    
    
    if (attributes.hasValue(R.styleable.MaterialRadioButton_buttonTint)) {
      CompoundButtonCompat.setButtonTintList(
          this,
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.MaterialRadioButton_buttonTint));
    }

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialRadioButton_useMaterialThemeColors, false);

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
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);

      int[] radioButtonColorList = new int[ENABLED_CHECKED_STATES.length];
      radioButtonColorList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      radioButtonColorList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
      radioButtonColorList[2] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      radioButtonColorList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      materialThemeColorsTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, radioButtonColorList);
    }
    return materialThemeColorsTintList;
  }
}
