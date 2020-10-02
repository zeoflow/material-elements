/**
 * Copyright 2020 ZeoFlow SRL
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.button;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.zeoflow.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MaterialButtonLoading extends FrameLayout
{

  private boolean isLoading = false;
  private MaterialButton zButton;
  private ProgressBar zPbar;
  private String text;
  private int textColor;
  private int rippleColor;
  private int loadingColor;
  private int accentColor;
  private int backgroundColor;
  private Context zContext;
  private AttributeSet zAttrs;
  private boolean rippleDefault;
  private int zFont;

  private int btnStrokeWidth;

  public static final int BUTTON_DESIGN_OUTLINED = 0x1;
  public static final int BUTTON_DESIGN_FILLED = 0x2;

  @IntDef({BUTTON_DESIGN_FILLED, BUTTON_DESIGN_OUTLINED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ButtonDesign
  {
  }

  @ButtonDesign
  private int buttonDesign;

  public static final int LOADING_DESIGN_CIRCLE = 0x1;
  public static final int LOADING_DESIGN_HORIZONTAL = 0x2;

  @IntDef({LOADING_DESIGN_CIRCLE, LOADING_DESIGN_HORIZONTAL})
  @Retention(RetentionPolicy.SOURCE)
  public @interface LoadingDesign
  {
  }

  @ButtonDesign
  private int loadingDesign;

  public MaterialButtonLoading(@NonNull Context context)
  {
    super(context);
  }

  public MaterialButtonLoading(@NonNull Context context, @Nullable AttributeSet attrs)
  {
    super(context, attrs);
    zContext = context;
    zAttrs = attrs;
    loadBtn();
  }

  public MaterialButtonLoading(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
    zContext = context;
    zAttrs = attrs;
    loadBtn();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public MaterialButtonLoading(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
  {
    super(context, attrs, defStyleAttr, defStyleRes);
    zContext = context;
    zAttrs = attrs;
    loadBtn();
  }

  private void loadBtn()
  {
    LayoutInflater.from(zContext).inflate(R.layout.loading_button, this, true);

    TypedArray attributes = zContext.getTheme().obtainStyledAttributes(zAttrs, R.styleable.MaterialLoadingButton, 0, 0);

    btnStrokeWidth = (int) attributes.getDimension(R.styleable.MaterialLoadingButton_mlbStrokeWidth, zContext.getResources().getDimension(R.dimen.mtrl_btn_stroke_width));
    buttonDesign = attributes.getInteger(R.styleable.MaterialLoadingButton_mlbButtonDesign, BUTTON_DESIGN_OUTLINED);
    loadingDesign = attributes.getInteger(R.styleable.MaterialLoadingButton_mlbLoadingDesign, LOADING_DESIGN_CIRCLE);
    rippleColor = attributes.getColor(R.styleable.MaterialLoadingButton_mlbRippleColor, 0);
    text = attributes.getString(R.styleable.MaterialLoadingButton_mlbText);
    textColor = attributes.getColor(R.styleable.MaterialLoadingButton_mlbTextColor, 0);
    loadingColor = attributes.getColor(R.styleable.MaterialLoadingButton_mlbLoadingColor, 0);
    accentColor = attributes.getColor(R.styleable.MaterialLoadingButton_mlbAccentColor, 0);
    backgroundColor = attributes.getColor(R.styleable.MaterialLoadingButton_mlbBackgroundColor, 0);
    rippleDefault = attributes.getBoolean(R.styleable.MaterialLoadingButton_mlbRippleDefault, true);
    zFont = attributes.getResourceId(R.styleable.MaterialLoadingButton_mlbFontFamily, 0);

    zButton = findViewById(R.id.btn);
    if (zFont != 0)
    {
      Typeface typeface = ResourcesCompat.getFont(zContext, zFont);
      zButton.setTypeface(typeface, Typeface.BOLD);
    }
    if (loadingDesign == LOADING_DESIGN_CIRCLE)
    {
      zPbar = findViewById(R.id.pbCircle);
    } else if (loadingDesign == LOADING_DESIGN_HORIZONTAL)
    {
      zPbar = findViewById(R.id.pb);
    }
    zPbar.setVisibility(GONE);

    drawBtn();
  }

  public void setTypeface(Typeface typeface)
  {
    zButton.setTypeface(typeface, Typeface.BOLD);
  }

  public void setFont(int font)
  {
    this.zFont = font;
    Typeface typeface = ResourcesCompat.getFont(zContext, font);
    zButton.setTypeface(typeface, Typeface.BOLD);
  }

  public void setText(String text)
  {
    this.text = text;
    zButton.setText(text);
  }

  public void setTextColor(int textColor)
  {
    this.textColor = textColor;
    zButton.setTextColor(textColor);
  }

  public void setRippleColor(int rippleColor)
  {
    this.rippleColor = rippleColor;
    zButton.setRippleColor(ColorStateList.valueOf(rippleColor));
  }

  public void setRippleColor(ColorStateList rippleColor)
  {
    this.rippleColor = rippleColor.getDefaultColor();
    zButton.setRippleColor(rippleColor);
  }

  public void setAccentColor(int accentColor)
  {
    this.accentColor = accentColor;
    if (buttonDesign == BUTTON_DESIGN_OUTLINED)
    {
      zButton.setStrokeColor(ColorStateList.valueOf(accentColor));
    } else if (buttonDesign == BUTTON_DESIGN_FILLED)
    {
      zButton.setBackgroundColor(accentColor);
    }
    if (rippleDefault)
    {
      if (isDark(accentColor))
      {
        zButton.setRippleColor(ColorStateList.valueOf(lighten(accentColor, 0.2f)));
      } else
      {
        zButton.setRippleColor(ColorStateList.valueOf(darken(accentColor, 0.2f)));
      }
    }
  }

  public void setRippleDeafult(boolean rippleDefault)
  {
    this.rippleDefault = rippleDefault;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public void setLoadingColor(int loadingColor)
  {
    zPbar.setIndeterminateTintList(ColorStateList.valueOf(loadingColor));
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public void setLoadingColor(ColorStateList loadingColor)
  {
    zPbar.setIndeterminateTintList(loadingColor);
  }

  private void drawBtn()
  {
    zButton.setText(text);
    zButton.setTextColor(textColor);
    if (buttonDesign == BUTTON_DESIGN_OUTLINED)
    {
      zButton.setStrokeColor(ColorStateList.valueOf(accentColor));
      zButton.setStrokeWidth(btnStrokeWidth);
    } else if (buttonDesign == BUTTON_DESIGN_FILLED)
    {
      zButton.setBackgroundColor(accentColor);
    }
    if (backgroundColor != 0)
    {
      zButton.setBackgroundColor(backgroundColor);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      zPbar.setIndeterminateTintList(ColorStateList.valueOf(loadingColor));
    }
    if (rippleDefault)
    {
      if (isDark(accentColor))
      {
        zButton.setRippleColor(ColorStateList.valueOf(lighten(accentColor, 0.2f)));
      } else
      {
        zButton.setRippleColor(ColorStateList.valueOf(darken(accentColor, 0.2f)));
      }
    } else
    {
      zButton.setRippleColor(ColorStateList.valueOf(rippleColor));
    }

  }

  public static int lighten(int color, double fraction)
  {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    red = lightenColor(red, fraction);
    green = lightenColor(green, fraction);
    blue = lightenColor(blue, fraction);
    int alpha = Color.alpha(color);
    return Color.argb(alpha, red, green, blue);
  }

  public static int darken(int color, double fraction)
  {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    red = darkenColor(red, fraction);
    green = darkenColor(green, fraction);
    blue = darkenColor(blue, fraction);
    int alpha = Color.alpha(color);

    return Color.argb(alpha, red, green, blue);
  }

  private static int darkenColor(int color, double fraction)
  {
    return (int) Math.max(color - (color * fraction), 0);
  }

  private static int lightenColor(int color, double fraction)
  {
    return (int) Math.min(color + (color * fraction), 255);
  }

  boolean isDark(int color)
  {
    return ColorUtils.calculateLuminance(color) < 0.5;
  }

  public void setLoading(boolean isLoading)
  {
    this.isLoading = isLoading;
    if (isLoading)
    {
      zPbar.setVisibility(VISIBLE);
      zButton.setText("");
    } else
    {
      zPbar.setVisibility(GONE);
      zButton.setText(text);
    }
  }

}
