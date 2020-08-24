

package com.zeoflow.material.elements.transition;

import android.graphics.Path;
import android.graphics.PointF;
import androidx.annotation.NonNull;
import androidx.transition.PathMotion;


public final class MaterialArcMotion extends PathMotion {

  @NonNull
  @Override
  public Path getPath(float startX, float startY, float endX, float endY) {
    Path path = new Path();
    path.moveTo(startX, startY);

    PointF controlPoint = getControlPoint(startX, startY, endX, endY);
    path.quadTo(controlPoint.x, controlPoint.y, endX, endY);
    return path;
  }

  private static PointF getControlPoint(float startX, float startY, float endX, float endY) {
    if (startY > endY) {
      return new PointF(endX, startY);
    } else {
      return new PointF(startX, endY);
    }
  }
}
