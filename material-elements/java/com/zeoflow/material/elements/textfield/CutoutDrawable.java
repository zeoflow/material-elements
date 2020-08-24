

package com.zeoflow.material.elements.textfield;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;


class CutoutDrawable extends MaterialShapeDrawable {
  @NonNull private final Paint cutoutPaint;
  @NonNull private final RectF cutoutBounds;
  private int savedLayer;

  CutoutDrawable() {
    this(null);
  }

  CutoutDrawable(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
    super(shapeAppearanceModel != null ? shapeAppearanceModel : new ShapeAppearanceModel());
    cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    setPaintStyles();
    cutoutBounds = new RectF();
  }

  private void setPaintStyles() {
    cutoutPaint.setStyle(Style.FILL_AND_STROKE);
    cutoutPaint.setColor(Color.WHITE);
    cutoutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
  }

  boolean hasCutout() {
    return !cutoutBounds.isEmpty();
  }

  void setCutout(float left, float top, float right, float bottom) {
    
    
    if (left != cutoutBounds.left
        || top != cutoutBounds.top
        || right != cutoutBounds.right
        || bottom != cutoutBounds.bottom) {
      cutoutBounds.set(left, top, right, bottom);
      invalidateSelf();
    }
  }

  void setCutout(@NonNull RectF bounds) {
    setCutout(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  void removeCutout() {
    
    setCutout(0, 0, 0, 0);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    preDraw(canvas);
    super.draw(canvas);

    
    canvas.drawRect(cutoutBounds, cutoutPaint);

    postDraw(canvas);
  }

  private void preDraw(@NonNull Canvas canvas) {
    Callback callback = getCallback();

    if (useHardwareLayer(callback)) {
      View viewCallback = (View) callback;
      
      if (viewCallback.getLayerType() != View.LAYER_TYPE_HARDWARE) {
        viewCallback.setLayerType(View.LAYER_TYPE_HARDWARE, null);
      }
    } else {
      
      saveCanvasLayer(canvas);
    }
  }

  private void saveCanvasLayer(@NonNull Canvas canvas) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      savedLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
    } else {
      savedLayer =
          canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
    }
  }

  private void postDraw(@NonNull Canvas canvas) {
    if (!useHardwareLayer(getCallback())) {
      canvas.restoreToCount(savedLayer);
    }
  }

  private boolean useHardwareLayer(Callback callback) {
    return callback instanceof View;
  }
}
