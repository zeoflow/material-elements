

package com.zeoflow.material.elements.textfield;

import androidx.annotation.NonNull;


class CustomEndIconDelegate extends EndIconDelegate {

  CustomEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconOnClickListener(null);
    textInputLayout.setEndIconOnLongClickListener(null);
  }
}
