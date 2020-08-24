

package com.zeoflow.material.elements.internal;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;

@RequiresApi(18)
class ViewGroupOverlayApi18 implements ViewGroupOverlayImpl {

  private final ViewGroupOverlay viewGroupOverlay;

  ViewGroupOverlayApi18(@NonNull ViewGroup group) {
    viewGroupOverlay = group.getOverlay();
  }

  @Override
  public void add(@NonNull Drawable drawable) {
    viewGroupOverlay.add(drawable);
  }

  @Override
  public void remove(@NonNull Drawable drawable) {
    viewGroupOverlay.remove(drawable);
  }

  @Override
  public void add(@NonNull View view) {
    viewGroupOverlay.add(view);
  }

  @Override
  public void remove(@NonNull View view) {
    viewGroupOverlay.remove(view);
  }
}
