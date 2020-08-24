

package com.zeoflow.material.elements.shape;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.Op;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;


public class ShapeAppearancePathProvider {

  
  @RestrictTo(Scope.LIBRARY_GROUP)
  public interface PathListener {

    void onCornerPathCreated(ShapePath cornerPath, Matrix transform, int count);

    void onEdgePathCreated(ShapePath edgePath, Matrix transform, int count);
  }

  
  private final ShapePath[] cornerPaths = new ShapePath[4];
  private final Matrix[] cornerTransforms = new Matrix[4];
  private final Matrix[] edgeTransforms = new Matrix[4];

  
  private final PointF pointF = new PointF();
  private final Path overlappedEdgePath = new Path();
  private final Path boundsPath = new Path();
  private final ShapePath shapePath = new ShapePath();
  private final float[] scratch = new float[2];
  private final float[] scratch2 = new float[2];

  private boolean edgeIntersectionCheckEnabled = true;

  public ShapeAppearancePathProvider() {
    for (int i = 0; i < 4; i++) {
      cornerPaths[i] = new ShapePath();
      cornerTransforms[i] = new Matrix();
      edgeTransforms[i] = new Matrix();
    }
  }

  
  public void calculatePath(
      ShapeAppearanceModel shapeAppearanceModel,
      float interpolation,
      RectF bounds,
      @NonNull Path path) {
    calculatePath(shapeAppearanceModel, interpolation, bounds, null, path);
  }

  
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void calculatePath(
      ShapeAppearanceModel shapeAppearanceModel,
      float interpolation,
      RectF bounds,
      PathListener pathListener,
      @NonNull Path path) {
    path.rewind();
    overlappedEdgePath.rewind();
    boundsPath.rewind();
    boundsPath.addRect(bounds, Direction.CW);
    ShapeAppearancePathSpec spec =
        new ShapeAppearancePathSpec(
            shapeAppearanceModel, interpolation, bounds, pathListener, path);

    
    
    for (int index = 0; index < 4; index++) {
      setCornerPathAndTransform(spec, index);
      setEdgePathAndTransform(index);
    }

    for (int index = 0; index < 4; index++) {
      appendCornerPath(spec, index);
      appendEdgePath(spec, index);
    }

    path.close();
    overlappedEdgePath.close();

    
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT && !overlappedEdgePath.isEmpty()) {
      path.op(overlappedEdgePath, Op.UNION);
    }
  }

  private void setCornerPathAndTransform(@NonNull ShapeAppearancePathSpec spec, int index) {
    CornerSize size = getCornerSizeForIndex(index, spec.shapeAppearanceModel);
    getCornerTreatmentForIndex(index, spec.shapeAppearanceModel)
        .getCornerPath(cornerPaths[index], 90, spec.interpolation, spec.bounds, size);

    float edgeAngle = angleOfEdge(index);
    cornerTransforms[index].reset();
    getCoordinatesOfCorner(index, spec.bounds, pointF);
    cornerTransforms[index].setTranslate(pointF.x, pointF.y);
    cornerTransforms[index].preRotate(edgeAngle);
  }

  private void setEdgePathAndTransform(int index) {
    scratch[0] = cornerPaths[index].getEndX();
    scratch[1] = cornerPaths[index].getEndY();
    cornerTransforms[index].mapPoints(scratch);
    float edgeAngle = angleOfEdge(index);
    edgeTransforms[index].reset();
    edgeTransforms[index].setTranslate(scratch[0], scratch[1]);
    edgeTransforms[index].preRotate(edgeAngle);
  }

  private void appendCornerPath(@NonNull ShapeAppearancePathSpec spec, int index) {
    scratch[0] = cornerPaths[index].getStartX();
    scratch[1] = cornerPaths[index].getStartY();
    cornerTransforms[index].mapPoints(scratch);
    if (index == 0) {
      spec.path.moveTo(scratch[0], scratch[1]);
    } else {
      spec.path.lineTo(scratch[0], scratch[1]);
    }
    cornerPaths[index].applyToPath(cornerTransforms[index], spec.path);
    if (spec.pathListener != null) {
      spec.pathListener.onCornerPathCreated(cornerPaths[index], cornerTransforms[index], index);
    }
  }

  private void appendEdgePath(@NonNull ShapeAppearancePathSpec spec, int index) {
    int nextIndex = (index + 1) % 4;
    scratch[0] = cornerPaths[index].getEndX();
    scratch[1] = cornerPaths[index].getEndY();
    cornerTransforms[index].mapPoints(scratch);

    scratch2[0] = cornerPaths[nextIndex].getStartX();
    scratch2[1] = cornerPaths[nextIndex].getStartY();
    cornerTransforms[nextIndex].mapPoints(scratch2);

    float edgeLength = (float) Math.hypot(scratch[0] - scratch2[0], scratch[1] - scratch2[1]);
    
    edgeLength = Math.max(edgeLength - .001f, 0);
    float center = getEdgeCenterForIndex(spec.bounds, index);
    shapePath.reset(0, 0);
    EdgeTreatment edgeTreatment = getEdgeTreatmentForIndex(index, spec.shapeAppearanceModel);
    edgeTreatment.getEdgePath(edgeLength, center, spec.interpolation, shapePath);
    Path edgePath = new Path();
    shapePath.applyToPath(edgeTransforms[index], edgePath);

    if (edgeIntersectionCheckEnabled
        && VERSION.SDK_INT >= VERSION_CODES.KITKAT
        && (edgeTreatment.forceIntersection()
            || pathOverlapsCorner(edgePath, index)
            || pathOverlapsCorner(edgePath, nextIndex))) {

      
      
      edgePath.op(edgePath, boundsPath, Op.DIFFERENCE);

      
      
      scratch[0] = shapePath.getStartX();
      scratch[1] = shapePath.getStartY();
      edgeTransforms[index].mapPoints(scratch);
      overlappedEdgePath.moveTo(scratch[0], scratch[1]);

      
      shapePath.applyToPath(edgeTransforms[index], overlappedEdgePath);
    } else {
      shapePath.applyToPath(edgeTransforms[index], spec.path);
    }

    if (spec.pathListener != null) {
      spec.pathListener.onEdgePathCreated(shapePath, edgeTransforms[index], index);
    }
  }

  @RequiresApi(VERSION_CODES.KITKAT)
  private boolean pathOverlapsCorner(Path edgePath, int index) {
    Path cornerPath = new Path();
    cornerPaths[index].applyToPath(cornerTransforms[index], cornerPath);

    RectF bounds = new RectF();
    edgePath.computeBounds(bounds,  true);
    cornerPath.computeBounds(bounds,  true);
    edgePath.op(cornerPath, Op.INTERSECT);
    edgePath.computeBounds(bounds,  true);

    return !bounds.isEmpty() || (bounds.width() > 1 && bounds.height() > 1);
  }

  private float getEdgeCenterForIndex(@NonNull RectF bounds, int index) {
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);
    switch (index) {
      case 1:
      case 3:
        return Math.abs(bounds.centerX() - scratch[0]);
      case 2:
      case 0:
      default:
        return Math.abs(bounds.centerY() - scratch[1]);
    }
  }

  private CornerTreatment getCornerTreatmentForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomRightCorner();
      case 2:
        return shapeAppearanceModel.getBottomLeftCorner();
      case 3:
        return shapeAppearanceModel.getTopLeftCorner();
      case 0:
      default:
        return shapeAppearanceModel.getTopRightCorner();
    }
  }

  private CornerSize getCornerSizeForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomRightCornerSize();
      case 2:
        return shapeAppearanceModel.getBottomLeftCornerSize();
      case 3:
        return shapeAppearanceModel.getTopLeftCornerSize();
      case 0:
      default:
        return shapeAppearanceModel.getTopRightCornerSize();
    }
  }

  private EdgeTreatment getEdgeTreatmentForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomEdge();
      case 2:
        return shapeAppearanceModel.getLeftEdge();
      case 3:
        return shapeAppearanceModel.getTopEdge();
      case 0:
      default:
        return shapeAppearanceModel.getRightEdge();
    }
  }

  private void getCoordinatesOfCorner(int index, @NonNull RectF bounds, @NonNull PointF pointF) {
    switch (index) {
      case 1: 
        pointF.set(bounds.right, bounds.bottom);
        break;
      case 2: 
        pointF.set(bounds.left, bounds.bottom);
        break;
      case 3: 
        pointF.set(bounds.left, bounds.top);
        break;
      case 0: 
      default:
        pointF.set(bounds.right, bounds.top);
        break;
    }
  }

  private float angleOfEdge(int index) {
    return 90 * (index + 1 % 4);
  }

  void setEdgeIntersectionCheckEnable(boolean enable) {
    edgeIntersectionCheckEnabled = enable;
  }

  
  static final class ShapeAppearancePathSpec {

    @NonNull public final ShapeAppearanceModel shapeAppearanceModel;
    @NonNull public final Path path;
    @NonNull public final RectF bounds;

    @Nullable public final PathListener pathListener;

    public final float interpolation;

    ShapeAppearancePathSpec(
        @NonNull ShapeAppearanceModel shapeAppearanceModel,
        float interpolation,
        RectF bounds,
        @Nullable PathListener pathListener,
        Path path) {
      this.pathListener = pathListener;
      this.shapeAppearanceModel = shapeAppearanceModel;
      this.interpolation = interpolation;
      this.bounds = bounds;
      this.path = path;
    }
  }
}
