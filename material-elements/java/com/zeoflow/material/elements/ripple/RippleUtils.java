

package com.zeoflow.material.elements.ripple;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;
import android.util.Log;
import android.util.StateSet;


@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleUtils {

  public static final boolean USE_FRAMEWORK_RIPPLE = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;

  private static final int[] PRESSED_STATE_SET = {
    android.R.attr.state_pressed,
  };
  private static final int[] HOVERED_FOCUSED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_focused,
  };
  private static final int[] FOCUSED_STATE_SET = {
    android.R.attr.state_focused,
  };
  private static final int[] HOVERED_STATE_SET = {
    android.R.attr.state_hovered,
  };

  private static final int[] SELECTED_PRESSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_pressed,
  };
  private static final int[] SELECTED_HOVERED_FOCUSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_hovered, android.R.attr.state_focused,
  };
  private static final int[] SELECTED_FOCUSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_focused,
  };
  private static final int[] SELECTED_HOVERED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_hovered,
  };
  private static final int[] SELECTED_STATE_SET = {
    android.R.attr.state_selected,
  };

  private static final int[] ENABLED_PRESSED_STATE_SET = {
    android.R.attr.state_enabled, android.R.attr.state_pressed
  };

  @VisibleForTesting static final String LOG_TAG = RippleUtils.class.getSimpleName();

  @VisibleForTesting
  static final String TRANSPARENT_DEFAULT_COLOR_WARNING =
      "Use a non-transparent color for the default color as it will be used to finish ripple"
          + " animations.";

  private RippleUtils() {}

  
  @NonNull
  public static ColorStateList convertToRippleDrawableColor(@Nullable ColorStateList rippleColor) {
    if (USE_FRAMEWORK_RIPPLE) {
      int size = 2;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      
      
      
      

      
      states[i] = SELECTED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_PRESSED_STATE_SET);
      i++;

      
      states[i] = StateSet.NOTHING;
      colors[i] = getColorForState(rippleColor, PRESSED_STATE_SET);
      i++;

      return new ColorStateList(states, colors);
    } else {
      int size = 10;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      states[i] = SELECTED_PRESSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_PRESSED_STATE_SET);
      i++;

      states[i] = SELECTED_HOVERED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_HOVERED_FOCUSED_STATE_SET);
      i++;

      states[i] = SELECTED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_FOCUSED_STATE_SET);
      i++;

      states[i] = SELECTED_HOVERED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_HOVERED_STATE_SET);
      i++;

      
      states[i] = SELECTED_STATE_SET;
      colors[i] = Color.TRANSPARENT;
      i++;

      states[i] = PRESSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, PRESSED_STATE_SET);
      i++;

      states[i] = HOVERED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, HOVERED_FOCUSED_STATE_SET);
      i++;

      states[i] = FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, FOCUSED_STATE_SET);
      i++;

      states[i] = HOVERED_STATE_SET;
      colors[i] = getColorForState(rippleColor, HOVERED_STATE_SET);
      i++;

      
      states[i] = StateSet.NOTHING;
      colors[i] = Color.TRANSPARENT;
      i++;

      return new ColorStateList(states, colors);
    }
  }

  
  @NonNull
  public static ColorStateList sanitizeRippleDrawableColor(@Nullable ColorStateList rippleColor) {
    if (rippleColor != null) {
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1
          && VERSION.SDK_INT <= VERSION_CODES.O_MR1
          && Color.alpha(rippleColor.getDefaultColor()) == 0
          && Color.alpha(rippleColor.getColorForState(ENABLED_PRESSED_STATE_SET, Color.TRANSPARENT))
              != 0) {
        Log.w(LOG_TAG, TRANSPARENT_DEFAULT_COLOR_WARNING);
      }
      return rippleColor;
    }
    return ColorStateList.valueOf(Color.TRANSPARENT);
  }

  
  public static boolean shouldDrawRippleCompat(@NonNull int[] stateSet) {
    boolean enabled = false;
    boolean interactedState = false;

    for (int state : stateSet) {
      if (state == android.R.attr.state_enabled) {
        enabled = true;
      } else if (state == android.R.attr.state_focused) {
        interactedState = true;
      } else if (state == android.R.attr.state_pressed) {
        interactedState = true;
      } else if (state == android.R.attr.state_hovered) {
        interactedState = true;
      }
    }
    return enabled && interactedState;
  }

  @ColorInt
  private static int getColorForState(@Nullable ColorStateList rippleColor, int[] state) {
    int color;
    if (rippleColor != null) {
      color = rippleColor.getColorForState(state, rippleColor.getDefaultColor());
    } else {
      color = Color.TRANSPARENT;
    }
    return USE_FRAMEWORK_RIPPLE ? doubleAlpha(color) : color;
  }

  
  @ColorInt
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private static int doubleAlpha(@ColorInt int color) {
    int alpha = Math.min(2 * Color.alpha(color), 255);
    return ColorUtils.setAlphaComponent(color, alpha);
  }
}
