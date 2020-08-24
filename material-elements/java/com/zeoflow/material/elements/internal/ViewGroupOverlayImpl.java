

package com.zeoflow.material.elements.internal;

import androidx.annotation.NonNull;
import android.view.View;

interface ViewGroupOverlayImpl extends ViewOverlayImpl {

  
  void add(@NonNull View view);

  
  void remove(@NonNull View view);
}
