

package com.zeoflow.material.elements.slider;

import androidx.annotation.NonNull;


public interface LabelFormatter {

  int LABEL_FLOATING = 0;
  int LABEL_WITHIN_BOUNDS = 1;
  int LABEL_GONE = 2;

  @NonNull
  String getFormattedValue(float value);
}
