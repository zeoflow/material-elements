

package com.zeoflow.material.elements.canvas;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;


@RestrictTo(LIBRARY_GROUP)
public class CanvasCompat {

  private CanvasCompat() {}

  
  public static int saveLayerAlpha(@NonNull Canvas canvas, @Nullable RectF bounds, int alpha) {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      return canvas.saveLayerAlpha(bounds, alpha);
    } else {
      return canvas.saveLayerAlpha(bounds, alpha, Canvas.ALL_SAVE_FLAG);
    }
  }

  
  public static int saveLayerAlpha(
      @NonNull Canvas canvas, float left, float top, float right, float bottom, int alpha) {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      return canvas.saveLayerAlpha(left, top, right, bottom, alpha);
    } else {
      return canvas.saveLayerAlpha(left, top, right, bottom, alpha, Canvas.ALL_SAVE_FLAG);
    }
  }
}
