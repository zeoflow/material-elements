
package com.zeoflow.material.elements.animation;

import android.animation.TypeEvaluator;
import android.graphics.Matrix;
import androidx.annotation.NonNull;


public class MatrixEvaluator implements TypeEvaluator<Matrix> {
  private final float[] tempStartValues = new float[9];
  private final float[] tempEndValues = new float[9];
  private final Matrix tempMatrix = new Matrix();

  @NonNull
  @Override
  public Matrix evaluate(float fraction, @NonNull Matrix startValue, @NonNull Matrix endValue) {
    startValue.getValues(tempStartValues);
    endValue.getValues(tempEndValues);
    for (int i = 0; i < 9; i++) {
      float diff = tempEndValues[i] - tempStartValues[i];
      tempEndValues[i] = tempStartValues[i] + (fraction * diff);
    }
    tempMatrix.setValues(tempEndValues);
    return tempMatrix;
  }
}
