

package com.zeoflow.material.elements.internal;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOverlay;

@RequiresApi(18)
class ViewOverlayApi18 implements ViewOverlayImpl {

  private final ViewOverlay viewOverlay;

  ViewOverlayApi18(@NonNull View view) {
    viewOverlay = view.getOverlay();
  }

  @Override
  public void add(@NonNull Drawable drawable) {
    viewOverlay.add(drawable);
  }

  @Override
  public void remove(@NonNull Drawable drawable) {
    viewOverlay.remove(drawable);
  }
}
