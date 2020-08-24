

package com.zeoflow.material.elements.textfield;

import androidx.annotation.NonNull;


class NoEndIconDelegate extends EndIconDelegate {
  NoEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconOnClickListener(null);
    textInputLayout.setEndIconDrawable(null);
    textInputLayout.setEndIconContentDescription(null);
  }
}
