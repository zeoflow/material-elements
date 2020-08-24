

package com.zeoflow.material.elements.bottomappbar;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.zeoflow.material.elements.floatingactionbutton.FloatingActionButton;
import com.zeoflow.material.elements.shape.EdgeTreatment;
import com.zeoflow.material.elements.shape.ShapePath;


public class BottomAppBarTopEdgeTreatment extends EdgeTreatment implements Cloneable {

  private static final int ARC_QUARTER = 90;
  private static final int ARC_HALF = 180;
  private static final int ANGLE_UP = 270;
  private static final int ANGLE_LEFT = 180;

  private float roundedCornerRadius;
  private float fabMargin;

  private float fabDiameter;
  private float cradleVerticalOffset;
  private float horizontalOffset;

  
  public BottomAppBarTopEdgeTreatment(
      float fabMargin, float roundedCornerRadius, float cradleVerticalOffset) {
    this.fabMargin = fabMargin;
    this.roundedCornerRadius = roundedCornerRadius;
    setCradleVerticalOffset(cradleVerticalOffset);
    this.horizontalOffset = 0f;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    if (fabDiameter == 0) {
      
      shapePath.lineTo(length, 0);
      return;
    }

    float cradleDiameter = fabMargin * 2 + fabDiameter;
    float cradleRadius = cradleDiameter / 2f;
    float roundedCornerOffset = interpolation * roundedCornerRadius;
    float middle = center + horizontalOffset;

    
    
    float verticalOffset =
        interpolation * cradleVerticalOffset + (1 - interpolation) * cradleRadius;
    float verticalOffsetRatio = verticalOffset / cradleRadius;
    if (verticalOffsetRatio >= 1.0f) {
      
      
      shapePath.lineTo(length, 0);
      return; 
    }

    
    
    

    
    
    float distanceBetweenCenters = cradleRadius + roundedCornerOffset;
    float distanceBetweenCentersSquared = distanceBetweenCenters * distanceBetweenCenters;
    float distanceY = verticalOffset + roundedCornerOffset;
    float distanceX = (float) Math.sqrt(distanceBetweenCentersSquared - (distanceY * distanceY));

    
    float leftRoundedCornerCircleX = middle - distanceX;
    float rightRoundedCornerCircleX = middle + distanceX;

    
    float cornerRadiusArcLength = (float) Math.toDegrees(Math.atan(distanceX / distanceY));
    float cutoutArcOffset = ARC_QUARTER - cornerRadiusArcLength;

    
    shapePath.lineTo( leftRoundedCornerCircleX,  0);

    
    
    shapePath.addArc(
         leftRoundedCornerCircleX - roundedCornerOffset,
         0,
         leftRoundedCornerCircleX + roundedCornerOffset,
         roundedCornerOffset * 2,
         ANGLE_UP,
         cornerRadiusArcLength);

    
    shapePath.addArc(
         middle - cradleRadius,
         -cradleRadius - verticalOffset,
         middle + cradleRadius,
         cradleRadius - verticalOffset,
         ANGLE_LEFT - cutoutArcOffset,
         cutoutArcOffset * 2 - ARC_HALF);

    
    
    shapePath.addArc(
         rightRoundedCornerCircleX - roundedCornerOffset,
         0,
         rightRoundedCornerCircleX + roundedCornerOffset,
         roundedCornerOffset * 2,
         ANGLE_UP - cornerRadiusArcLength,
         cornerRadiusArcLength);

    
    shapePath.lineTo( length,  0);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public float getFabDiameter() {
    return fabDiameter;
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public void setFabDiameter(float fabDiameter) {
    this.fabDiameter = fabDiameter;
  }

  
  void setHorizontalOffset(float horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public float getHorizontalOffset() {
    return horizontalOffset;
  }

  
  float getCradleVerticalOffset() {
    return cradleVerticalOffset;
  }

  
  void setCradleVerticalOffset(@FloatRange(from = 0f) float cradleVerticalOffset) {
    if (cradleVerticalOffset < 0) {
      throw new IllegalArgumentException("cradleVerticalOffset must be positive.");
    }
    this.cradleVerticalOffset = cradleVerticalOffset;
  }

  float getFabCradleMargin() {
    return fabMargin;
  }

  void setFabCradleMargin(float fabMargin) {
    this.fabMargin = fabMargin;
  }

  float getFabCradleRoundedCornerRadius() {
    return roundedCornerRadius;
  }

  void setFabCradleRoundedCornerRadius(float roundedCornerRadius) {
    this.roundedCornerRadius = roundedCornerRadius;
  }
}
