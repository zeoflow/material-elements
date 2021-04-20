/*
 * Copyright (C) 2021 ZeoFlow SRL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.windowpreferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import com.zeoflow.material.elements.resources.MaterialAttributes;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
import static android.view.View.SYSTEM_UI_FLAG_VISIBLE;

/** Helper that saves the current window preferences. */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class WindowPreferencesManager
{

  private static final String PREFERENCES_NAME = "window_preferences";
  private static final String KEY_EDGE_TO_EDGE_ENABLED = "edge_to_edge_enabled";
  private static final int EDGE_TO_EDGE_BAR_ALPHA = 128;

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  private static final int EDGE_TO_EDGE_FLAGS =
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

  private final Context context;

  public WindowPreferencesManager(Context context) {
    this.context = context;
  }

  @SuppressWarnings("ApplySharedPref")
  public void toggleEdgeToEdgeEnabled() {
    getSharedPreferences()
        .edit()
        .putBoolean(KEY_EDGE_TO_EDGE_ENABLED, !isEdgeToEdgeEnabled())
        .commit();
  }

  public boolean isEdgeToEdgeEnabled() {
    return getSharedPreferences().getBoolean(KEY_EDGE_TO_EDGE_ENABLED, false);
  }

  @SuppressWarnings("RestrictTo")
  public void applyEdgeToEdgePreference(Window window) {
    boolean edgeToEdgeEnabled = isEdgeToEdgeEnabled();

    int statusBarColor = getStatusBarColor(isEdgeToEdgeEnabled());
    int navbarColor = getNavBarColor(isEdgeToEdgeEnabled());

    boolean lightBackground = isColorLight(getColor(context, android.R.attr.colorBackground, Color.BLACK));
    boolean lightNavbar = isColorLight(navbarColor);
    boolean showDarkNavbarIcons = lightNavbar || (navbarColor == TRANSPARENT && lightBackground);

    View decorView = window.getDecorView();
    int currentStatusBar = VERSION.SDK_INT >= VERSION_CODES.M
        ? decorView.getSystemUiVisibility() & SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        : 0;
    int currentNavBar = showDarkNavbarIcons && VERSION.SDK_INT >= VERSION_CODES.O
        ? SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        : 0;

    window.setNavigationBarColor(navbarColor);
    window.setStatusBarColor(statusBarColor);
    int systemUiVisibility = (edgeToEdgeEnabled ? EDGE_TO_EDGE_FLAGS : SYSTEM_UI_FLAG_VISIBLE)
        | currentStatusBar
        | currentNavBar;

    decorView.setSystemUiVisibility(systemUiVisibility);
  }

  @SuppressWarnings("RestrictTo")
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private int getStatusBarColor(boolean isEdgeToEdgeEnabled) {
    if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.M) {
      int opaqueStatusBarColor =
          getColor(context, android.R.attr.statusBarColor, Color.BLACK);
      return ColorUtils.setAlphaComponent(opaqueStatusBarColor, EDGE_TO_EDGE_BAR_ALPHA);
    }
    if (isEdgeToEdgeEnabled) {
      return TRANSPARENT;
    }
    return getColor(context, android.R.attr.statusBarColor, Color.BLACK);
  }

  @SuppressWarnings("RestrictTo")
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private int getNavBarColor(boolean isEdgeToEdgeEnabled) {
    if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.O_MR1) {
      int opaqueNavBarColor =
          getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
      return ColorUtils.setAlphaComponent(opaqueNavBarColor, EDGE_TO_EDGE_BAR_ALPHA);
    }
    if (isEdgeToEdgeEnabled) {
      return TRANSPARENT;
    }
    return getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }
  /**
   * Determines if a color should be considered light or dark.
   */
  public boolean isColorLight(@ColorInt int color)
  {
    return color != TRANSPARENT && androidx.core.graphics.ColorUtils.calculateLuminance(color) > 0.5;
  }
  /**
   * Returns the color int for the provided theme color attribute.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public int getColor(Context context, @AttrRes int colorAttributeResId, String errorMessageComponent)
  {
    return MaterialAttributes.resolveOrThrow(context, colorAttributeResId, errorMessageComponent);
  }
  /**
   * Returns the color int for the provided theme color attribute, using the {@link Context} of the
   * provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public int getColor(@NonNull View view, @AttrRes int colorAttributeResId)
  {
    return MaterialAttributes.resolveOrThrow(view, colorAttributeResId);
  }
  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme, using the {@code view}'s {@link Context}.
   */
  @ColorInt
  public int getColor(@NonNull View view, @AttrRes int colorAttributeResId, @ColorInt int defaultValue)
  {
    return getColor(view.getContext(), colorAttributeResId, defaultValue);
  }
  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(@NonNull Context context, @AttrRes int colorAttributeResId, @ColorInt int defaultValue)
  {
    TypedValue typedValue = MaterialAttributes.resolve(context, colorAttributeResId);
    if (typedValue != null)
    {
      return typedValue.data;
    } else
    {
      return defaultValue;
    }
  }
}
