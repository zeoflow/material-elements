

package com.zeoflow.material.elements.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

class ViewGroupOverlayApi14 extends ViewOverlayApi14 implements ViewGroupOverlayImpl {

  ViewGroupOverlayApi14(Context context, ViewGroup hostView, View requestingView) {
    super(context, hostView, requestingView);
  }

  static ViewGroupOverlayApi14 createFrom(ViewGroup viewGroup) {
    return (ViewGroupOverlayApi14) ViewOverlayApi14.createFrom(viewGroup);
  }

  @Override
  public void add(@NonNull View view) {
    overlayViewGroup.add(view);
  }

  @Override
  public void remove(@NonNull View view) {
    overlayViewGroup.remove(view);
  }
}
