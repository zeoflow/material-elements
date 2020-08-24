

package com.zeoflow.material.elements.textfield;

import android.content.Context;
import androidx.annotation.NonNull;
import com.zeoflow.material.elements.internal.CheckableImageButton;


abstract class EndIconDelegate {

  TextInputLayout textInputLayout;
  Context context;
  CheckableImageButton endIconView;

  EndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    this.textInputLayout = textInputLayout;
    context = textInputLayout.getContext();
    endIconView = textInputLayout.getEndIconView();
  }

  
  abstract void initialize();

  
  boolean shouldTintIconOnError() {
    return false;
  }

  
  boolean isBoxBackgroundModeSupported(@TextInputLayout.BoxBackgroundMode int boxBackgroundMode) {
    return true;
  }

  
  void onSuffixVisibilityChanged(boolean visible) {}
}
