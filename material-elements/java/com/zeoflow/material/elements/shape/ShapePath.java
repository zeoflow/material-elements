

package com.zeoflow.material.elements.shape;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.zeoflow.material.elements.shadow.ShadowRenderer;

import java.util.ArrayList;
import java.util.List;


public class ShapePath {

  private static final float ANGLE_UP = 270;
  
  protected static final float ANGLE_LEFT = 180;

  
  @Deprecated public float startX;
  
  @Deprecated public float startY;
  
  @Deprecated public float endX;
  
  @Deprecated public float endY;
  
  @Deprecated public float currentShadowAngle;
  
  @Deprecated public float endShadowAngle;

  private final List<PathOperation> operations = new ArrayList<>();
  private final List<ShadowCompatOperation> shadowCompatOperations = new ArrayList<>();
  private boolean containsIncompatibleShadowOp;

  public ShapePath() {
    reset(0, 0);
  }

  public ShapePath(float startX, float startY) {
    reset(startX, startY);
  }

  
  public void reset(float startX, float startY) {
    reset(startX, startY, ANGLE_UP, 0);
  }

  
  public void reset(float startX, float startY, float shadowStartAngle, float shadowSweepAngle) {
    setStartX(startX);
    setStartY(startY);
    setEndX(startX);
    setEndY(startY);
    setCurrentShadowAngle(shadowStartAngle);
    setEndShadowAngle((shadowStartAngle + shadowSweepAngle) % 360);
    this.operations.clear();
    this.shadowCompatOperations.clear();
    this.containsIncompatibleShadowOp = false;
  }

  
  public void lineTo(float x, float y) {
    PathLineOperation operation = new PathLineOperation();
    operation.x = x;
    operation.y = y;
    operations.add(operation);

    LineShadowOperation shadowOperation = new LineShadowOperation(operation, getEndX(), getEndY());

    
    addShadowCompatOperation(
        shadowOperation,
        ANGLE_UP + shadowOperation.getAngle(),
        ANGLE_UP + shadowOperation.getAngle());

    setEndX(x);
    setEndY(y);
  }

  
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  public void quadToPoint(float controlX, float controlY, float toX, float toY) {
    PathQuadOperation operation = new PathQuadOperation();
    operation.setControlX(controlX);
    operation.setControlY(controlY);
    operation.setEndX(toX);
    operation.setEndY(toY);
    operations.add(operation);

    containsIncompatibleShadowOp = true;

    setEndX(toX);
    setEndY(toY);
  }

  
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  public void cubicToPoint(
      float controlX1, float controlY1, float controlX2, float controlY2, float toX, float toY) {
    PathCubicOperation operation =
        new PathCubicOperation(controlX1, controlY1, controlX2, controlY2, toX, toY);
    operations.add(operation);

    containsIncompatibleShadowOp = true;

    setEndX(toX);
    setEndY(toY);
  }

  
  public void addArc(
      float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
    PathArcOperation operation = new PathArcOperation(left, top, right, bottom);
    operation.setStartAngle(startAngle);
    operation.setSweepAngle(sweepAngle);
    operations.add(operation);

    ArcShadowOperation arcShadowOperation = new ArcShadowOperation(operation);
    float endAngle = startAngle + sweepAngle;
    
    
    
    boolean drawShadowInsideBounds = sweepAngle < 0;
    addShadowCompatOperation(
        arcShadowOperation,
        drawShadowInsideBounds ? (180 + startAngle) % 360 : startAngle,
        drawShadowInsideBounds ? (180 + endAngle) % 360 : endAngle);

    setEndX(
        (left + right) * 0.5f
            + (right - left) / 2 * (float) Math.cos(Math.toRadians(startAngle + sweepAngle)));
    setEndY(
        (top + bottom) * 0.5f
            + (bottom - top) / 2 * (float) Math.sin(Math.toRadians(startAngle + sweepAngle)));
  }

  
  public void applyToPath(Matrix transform, Path path) {
    for (int i = 0, size = operations.size(); i < size; i++) {
      PathOperation operation = operations.get(i);
      operation.applyToPath(transform, path);
    }
  }

  
  @NonNull
  ShadowCompatOperation createShadowCompatOperation(final Matrix transform) {
    
    addConnectingShadowIfNecessary(getEndShadowAngle());
    final List<ShadowCompatOperation> operations = new ArrayList<>(shadowCompatOperations);
    return new ShadowCompatOperation() {
      @Override
      public void draw(
              Matrix matrix, ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas) {
        for (ShadowCompatOperation op : operations) {
          op.draw(transform, shadowRenderer, shadowElevation, canvas);
        }
      }
    };
  }

  
  private void addShadowCompatOperation(
      ShadowCompatOperation shadowOperation, float startShadowAngle, float endShadowAngle) {
    addConnectingShadowIfNecessary(startShadowAngle);
    shadowCompatOperations.add(shadowOperation);
    setCurrentShadowAngle(endShadowAngle);
  }

  
  boolean containsIncompatibleShadowOp() {
    return containsIncompatibleShadowOp;
  }

  
  private void addConnectingShadowIfNecessary(float nextShadowAngle) {
    if (getCurrentShadowAngle() == nextShadowAngle) {
      
      return;
    }
    float shadowSweep = (nextShadowAngle - getCurrentShadowAngle() + 360) % 360;
    if (shadowSweep > 180) {
      
      return;
    }
    PathArcOperation pathArcOperation =
        new PathArcOperation(getEndX(), getEndY(), getEndX(), getEndY());
    pathArcOperation.setStartAngle(getCurrentShadowAngle());
    pathArcOperation.setSweepAngle(shadowSweep);
    shadowCompatOperations.add(new ArcShadowOperation(pathArcOperation));
    setCurrentShadowAngle(nextShadowAngle);
  }

  float getStartX() {
    return startX;
  }

  float getStartY() {
    return startY;
  }

  float getEndX() {
    return endX;
  }

  float getEndY() {
    return endY;
  }

  private float getCurrentShadowAngle() {
    return currentShadowAngle;
  }

  private float getEndShadowAngle() {
    return endShadowAngle;
  }

  private void setStartX(float startX) {
    this.startX = startX;
  }

  private void setStartY(float startY) {
    this.startY = startY;
  }

  private void setEndX(float endX) {
    this.endX = endX;
  }

  private void setEndY(float endY) {
    this.endY = endY;
  }

  private void setCurrentShadowAngle(float currentShadowAngle) {
    this.currentShadowAngle = currentShadowAngle;
  }

  private void setEndShadowAngle(float endShadowAngle) {
    this.endShadowAngle = endShadowAngle;
  }

  
  abstract static class ShadowCompatOperation {

    static final Matrix IDENTITY_MATRIX = new Matrix();

    
    public final void draw(ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas) {
      draw(IDENTITY_MATRIX, shadowRenderer, shadowElevation, canvas);
    }

    
    public abstract void draw(
        Matrix transform, ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas);
  }

  
  static class LineShadowOperation extends ShadowCompatOperation {

    private final PathLineOperation operation;
    private final float startX;
    private final float startY;

    public LineShadowOperation(PathLineOperation operation, float startX, float startY) {
      this.operation = operation;
      this.startX = startX;
      this.startY = startY;
    }

    @Override
    public void draw(
        Matrix transform,
        @NonNull ShadowRenderer shadowRenderer,
        int shadowElevation,
        @NonNull Canvas canvas) {
      final float height = operation.y - startY;
      final float width = operation.x - startX;
      final RectF rect = new RectF(0, 0, (float) Math.hypot(height, width), 0);
      final Matrix edgeTransform = new Matrix(transform);
      
      edgeTransform.preTranslate(startX, startY);
      edgeTransform.preRotate(getAngle());
      shadowRenderer.drawEdgeShadow(canvas, edgeTransform, rect, shadowElevation);
    }

    float getAngle() {
      return (float) Math.toDegrees(Math.atan((operation.y - startY) / (operation.x - startX)));
    }
  }

  
  static class ArcShadowOperation extends ShadowCompatOperation {

    private final PathArcOperation operation;

    public ArcShadowOperation(PathArcOperation operation) {
      this.operation = operation;
    }

    @Override
    public void draw(
        Matrix transform,
        @NonNull ShadowRenderer shadowRenderer,
        int shadowElevation,
        @NonNull Canvas canvas) {
      float startAngle = operation.getStartAngle();
      float sweepAngle = operation.getSweepAngle();
      RectF rect =
          new RectF(
              operation.getLeft(), operation.getTop(), operation.getRight(), operation.getBottom());
      shadowRenderer.drawCornerShadow(
          canvas, transform, rect, shadowElevation, startAngle, sweepAngle);
    }
  }

  
  public abstract static class PathOperation {

    
    protected final Matrix matrix = new Matrix();

    
    public abstract void applyToPath(Matrix transform, Path path);
  }

  
  public static class PathLineOperation extends PathOperation {
    private float x;
    private float y;

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.lineTo(x, y);
      path.transform(transform);
    }
  }

  
  public static class PathQuadOperation extends PathOperation {
    
    @Deprecated public float controlX;
    
    @Deprecated public float controlY;
    
    @Deprecated public float endX;
    
    @Deprecated public float endY;

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.quadTo(getControlX(), getControlY(), getEndX(), getEndY());
      path.transform(transform);
    }

    private float getEndX() {
      return endX;
    }

    private void setEndX(float endX) {
      this.endX = endX;
    }

    private float getControlY() {
      return controlY;
    }

    private void setControlY(float controlY) {
      this.controlY = controlY;
    }

    private float getEndY() {
      return endY;
    }

    private void setEndY(float endY) {
      this.endY = endY;
    }

    private float getControlX() {
      return controlX;
    }

    private void setControlX(float controlX) {
      this.controlX = controlX;
    }
  }

  
  public static class PathArcOperation extends PathOperation {
    private static final RectF rectF = new RectF();

    
    @Deprecated public float left;
    
    @Deprecated public float top;
    
    @Deprecated public float right;
    
    @Deprecated public float bottom;
    
    @Deprecated public float startAngle;
    
    @Deprecated public float sweepAngle;

    public PathArcOperation(float left, float top, float right, float bottom) {
      setLeft(left);
      setTop(top);
      setRight(right);
      setBottom(bottom);
    }

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      rectF.set(getLeft(), getTop(), getRight(), getBottom());
      path.arcTo(rectF, getStartAngle(), getSweepAngle(), false);
      path.transform(transform);
    }

    private float getLeft() {
      return left;
    }

    private float getTop() {
      return top;
    }

    private float getRight() {
      return right;
    }

    private float getBottom() {
      return bottom;
    }

    private void setLeft(float left) {
      this.left = left;
    }

    private void setTop(float top) {
      this.top = top;
    }

    private void setRight(float right) {
      this.right = right;
    }

    private void setBottom(float bottom) {
      this.bottom = bottom;
    }

    private float getStartAngle() {
      return startAngle;
    }

    private float getSweepAngle() {
      return sweepAngle;
    }

    private void setStartAngle(float startAngle) {
      this.startAngle = startAngle;
    }

    private void setSweepAngle(float sweepAngle) {
      this.sweepAngle = sweepAngle;
    }
  }

  
  public static class PathCubicOperation extends PathOperation {

    private float controlX1;

    private float controlY1;

    private float controlX2;

    private float controlY2;

    private float endX;

    private float endY;

    public PathCubicOperation(
        float controlX1,
        float controlY1,
        float controlX2,
        float controlY2,
        float endX,
        float endY) {
      setControlX1(controlX1);
      setControlY1(controlY1);
      setControlX2(controlX2);
      setControlY2(controlY2);
      setEndX(endX);
      setEndY(endY);
    }

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY);
      path.transform(transform);
    }

    private float getControlX1() {
      return controlX1;
    }

    private void setControlX1(float controlX1) {
      this.controlX1 = controlX1;
    }

    private float getControlY1() {
      return controlY1;
    }

    private void setControlY1(float controlY1) {
      this.controlY1 = controlY1;
    }

    private float getControlX2() {
      return controlX2;
    }

    private void setControlX2(float controlX2) {
      this.controlX2 = controlX2;
    }

    private float getControlY2() {
      return controlY1;
    }

    private void setControlY2(float controlY2) {
      this.controlY2 = controlY2;
    }

    private float getEndX() {
      return endX;
    }

    private void setEndX(float endX) {
      this.endX = endX;
    }

    private float getEndY() {
      return endY;
    }

    private void setEndY(float endY) {
      this.endY = endY;
    }
  }
}
