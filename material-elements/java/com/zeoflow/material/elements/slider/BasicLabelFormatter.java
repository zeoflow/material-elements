

package com.zeoflow.material.elements.slider;

import androidx.annotation.NonNull;
import java.util.Locale;


public final class BasicLabelFormatter implements LabelFormatter {

  private static final long TRILLION = 1000000000000L;
  private static final int BILLION = 1000000000;
  private static final int MILLION = 1000000;
  private static final int THOUSAND = 1000;

  @NonNull
  @Override
  public String getFormattedValue(float value) {
    if (value >= TRILLION) {
      return String.format(Locale.US, "%.1fT", value / TRILLION);
    } else if (value >= BILLION) {
      return String.format(Locale.US, "%.1fB", value / BILLION);
    } else if (value >= MILLION) {
      return String.format(Locale.US, "%.1fM", value / MILLION);
    } else if (value >= THOUSAND) {
      return String.format(Locale.US, "%.1fK", value / THOUSAND);
    }

    return String.format(Locale.US, "%.0f", value);
  }
}
