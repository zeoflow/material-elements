
package com.zeoflow.material.elements.datepicker;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;


class SmoothCalendarLayoutManager extends LinearLayoutManager {

  
  private static final float MILLISECONDS_PER_INCH = 100f;

  SmoothCalendarLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  @Override
  public void smoothScrollToPosition(
      RecyclerView recyclerView, RecyclerView.State state, int position) {
    final LinearSmoothScroller linearSmoothScroller =
        new LinearSmoothScroller(recyclerView.getContext()) {

          @Override
          protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
          }
        };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }
}
