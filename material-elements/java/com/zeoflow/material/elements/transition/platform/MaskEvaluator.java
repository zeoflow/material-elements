


package com.zeoflow.material.elements.transition.platform;

import static com.zeoflow.material.elements.transition.platform.TransitionUtils.lerp;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Op;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.ShapeAppearancePathProvider;


@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class MaskEvaluator {

  private final Path path = new Path();
  private final Path startPath = new Path();
  private final Path endPath = new Path();
  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();

  private ShapeAppearanceModel currentShapeAppearanceModel;

  
  void evaluate(
      float progress,
      ShapeAppearanceModel startShapeAppearanceModel,
      ShapeAppearanceModel endShapeAppearanceModel,
      RectF currentStartBounds,
      RectF currentStartBoundsMasked,
      RectF currentEndBoundsMasked,
      MaterialContainerTransform.ProgressThresholds shapeMaskThresholds) {

    
    
    float shapeStartFraction = shapeMaskThresholds.getStart();
    float shapeEndFraction = shapeMaskThresholds.getEnd();
    currentShapeAppearanceModel =
        lerp(
            startShapeAppearanceModel,
            endShapeAppearanceModel,
            currentStartBounds,
            currentEndBoundsMasked,
            shapeStartFraction,
            shapeEndFraction,
            progress);

    pathProvider.calculatePath(currentShapeAppearanceModel, 1, currentStartBoundsMasked, startPath);
    pathProvider.calculatePath(currentShapeAppearanceModel, 1, currentEndBoundsMasked, endPath);

    
    
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      path.op(startPath, endPath, Op.UNION);
    }
  }

  
  void clip(Canvas canvas) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      canvas.clipPath(path);
    } else {
      canvas.clipPath(startPath);
      canvas.clipPath(endPath, Region.Op.UNION);
    }
  }

  Path getPath() {
    return path;
  }

  ShapeAppearanceModel getCurrentShapeAppearanceModel() {
    return currentShapeAppearanceModel;
  }
}
