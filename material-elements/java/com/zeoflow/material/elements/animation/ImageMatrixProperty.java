
package com.zeoflow.material.elements.animation;

import android.graphics.Matrix;
import androidx.annotation.NonNull;
import android.util.Property;
import android.widget.ImageView;


public class ImageMatrixProperty extends Property<ImageView, Matrix> {
  private final Matrix matrix = new Matrix();

  public ImageMatrixProperty() {
    super(Matrix.class, "imageMatrixProperty");
  }

  @Override
  public void set(@NonNull ImageView object, @NonNull Matrix value) {
    object.setImageMatrix(value);
  }

  @NonNull
  @Override
  public Matrix get(@NonNull ImageView object) {
    matrix.set(object.getImageMatrix());
    return matrix;
  }
}
