

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


@RestrictTo(LIBRARY_GROUP)
public class DescendantOffsetUtils {
  private static final ThreadLocal<Matrix> matrix = new ThreadLocal<>();
  private static final ThreadLocal<RectF> rectF = new ThreadLocal<>();

  
  public static void offsetDescendantRect(
      @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect rect) {
    Matrix m = matrix.get();
    if (m == null) {
      m = new Matrix();
      matrix.set(m);
    } else {
      m.reset();
    }

    offsetDescendantMatrix(parent, descendant, m);

    RectF rectF = DescendantOffsetUtils.rectF.get();
    if (rectF == null) {
      rectF = new RectF();
      DescendantOffsetUtils.rectF.set(rectF);
    }
    rectF.set(rect);
    m.mapRect(rectF);
    rect.set(
        (int) (rectF.left + 0.5f),
        (int) (rectF.top + 0.5f),
        (int) (rectF.right + 0.5f),
        (int) (rectF.bottom + 0.5f));
  }

  
  public static void getDescendantRect(
      @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect out) {
    out.set(0, 0, descendant.getWidth(), descendant.getHeight());
    offsetDescendantRect(parent, descendant, out);
  }

  private static void offsetDescendantMatrix(
      ViewParent target, @NonNull View view, @NonNull Matrix m) {
    final ViewParent parent = view.getParent();
    if (parent instanceof View && parent != target) {
      final View vp = (View) parent;
      offsetDescendantMatrix(target, vp, m);
      m.preTranslate(-vp.getScrollX(), -vp.getScrollY());
    }

    m.preTranslate(view.getLeft(), view.getTop());

    if (!view.getMatrix().isIdentity()) {
      m.preConcat(view.getMatrix());
    }
  }
}
