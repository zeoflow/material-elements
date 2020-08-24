
package com.zeoflow.material.elements.circularreveal;

import static com.zeoflow.material.elements.math.MathUtils.DEFAULT_EPSILON;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import com.zeoflow.material.elements.circularreveal.CircularRevealWidget.RevealInfo;
import com.zeoflow.material.elements.math.MathUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class CircularRevealHelper {

  private static final boolean DEBUG = false;

  
  public interface Delegate {

    
    void actualDraw(Canvas canvas);

    
    boolean actualIsOpaque();
  }

  
  public static final int BITMAP_SHADER = 0;
  
  public static final int CLIP_PATH = 1;
  
  public static final int REVEAL_ANIMATOR = 2;

  
  @IntDef({CLIP_PATH, BITMAP_SHADER, REVEAL_ANIMATOR})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Strategy {}

  @Strategy public static final int STRATEGY;

  private final Delegate delegate;
  @NonNull private final View view;
  @NonNull private final Path revealPath;
  @NonNull private final Paint revealPaint;
  @NonNull private final Paint scrimPaint;
  
  @Nullable private RevealInfo revealInfo;
  
  @Nullable private Drawable overlayDrawable;

  private Paint debugPaint;

  private boolean buildingCircularRevealCache;
  private boolean hasCircularRevealCache;

  static {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      STRATEGY = REVEAL_ANIMATOR;
    } else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
      STRATEGY = CLIP_PATH;
    } else {
      STRATEGY = BITMAP_SHADER;
    }
  }

  public CircularRevealHelper(Delegate delegate) {
    this.delegate = delegate;
    this.view = (View) delegate;
    this.view.setWillNotDraw(false);

    revealPath = new Path();
    revealPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    scrimPaint.setColor(Color.TRANSPARENT);

    if (DEBUG) {
      debugPaint = new Paint();
      debugPaint.setStyle(Style.STROKE);
    }
  }

  public void buildCircularRevealCache() {
    if (STRATEGY == BITMAP_SHADER) {
      buildingCircularRevealCache = true;
      hasCircularRevealCache = false;

      view.buildDrawingCache();
      Bitmap bitmap = view.getDrawingCache();

      if (bitmap == null && view.getWidth() != 0 && view.getHeight() != 0) {
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
      }

      if (bitmap != null) {
        revealPaint.setShader(new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP));
      }

      buildingCircularRevealCache = false;
      hasCircularRevealCache = true;
    }
  }

  public void destroyCircularRevealCache() {
    if (STRATEGY == BITMAP_SHADER) {
      hasCircularRevealCache = false;
      view.destroyDrawingCache();
      revealPaint.setShader(null);
      view.invalidate();
    }
  }

  
  public void setRevealInfo(@Nullable RevealInfo revealInfo) {
    if (revealInfo == null) {
      this.revealInfo = null;
    } else {
      if (this.revealInfo == null) {
        this.revealInfo = new RevealInfo(revealInfo);
      } else {
        this.revealInfo.set(revealInfo);
      }

      
      if (MathUtils.geq(
          revealInfo.radius, getDistanceToFurthestCorner(revealInfo), DEFAULT_EPSILON)) {
        this.revealInfo.radius = RevealInfo.INVALID_RADIUS;
      }
    }

    invalidateRevealInfo();
  }

  @Nullable
  public RevealInfo getRevealInfo() {
    if (revealInfo == null) {
      return null;
    }

    RevealInfo revealInfo = new RevealInfo(this.revealInfo);
    if (revealInfo.isInvalid()) {
      revealInfo.radius = getDistanceToFurthestCorner(revealInfo);
    }
    return revealInfo;
  }

  public void setCircularRevealScrimColor(@ColorInt int color) {
    scrimPaint.setColor(color);
    view.invalidate();
  }

  @ColorInt
  public int getCircularRevealScrimColor() {
    return scrimPaint.getColor();
  }

  @Nullable
  public Drawable getCircularRevealOverlayDrawable() {
    return overlayDrawable;
  }

  public void setCircularRevealOverlayDrawable(@Nullable Drawable drawable) {
    overlayDrawable = drawable;
    view.invalidate();
  }

  private void invalidateRevealInfo() {
    if (STRATEGY == CLIP_PATH) {
      revealPath.rewind();
      if (revealInfo != null) {
        revealPath.addCircle(
            revealInfo.centerX, revealInfo.centerY, revealInfo.radius, Direction.CW);
      }
    }

    view.invalidate();
  }

  private float getDistanceToFurthestCorner(@NonNull RevealInfo revealInfo) {
    return MathUtils.distanceToFurthestCorner(
        revealInfo.centerX, revealInfo.centerY, 0, 0, view.getWidth(), view.getHeight());
  }

  public void draw(@NonNull Canvas canvas) {
    if (DEBUG) {
      drawDebugMode(canvas);
      return;
    }

    if (shouldDrawCircularReveal()) {
      switch (STRATEGY) {
        case REVEAL_ANIMATOR:
          delegate.actualDraw(canvas);
          if (shouldDrawScrim()) {
            canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), scrimPaint);
          }
          break;
        case CLIP_PATH:
          int count = canvas.save();
          canvas.clipPath(revealPath);

          delegate.actualDraw(canvas);
          if (shouldDrawScrim()) {
            canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), scrimPaint);
          }

          canvas.restoreToCount(count);
          break;
        case BITMAP_SHADER:
          canvas.drawCircle(revealInfo.centerX, revealInfo.centerY, revealInfo.radius, revealPaint);
          if (shouldDrawScrim()) {
            canvas.drawCircle(
                revealInfo.centerX, revealInfo.centerY, revealInfo.radius, scrimPaint);
          }
          break;
        default:
          throw new IllegalStateException("Unsupported strategy " + STRATEGY);
      }
    } else {
      delegate.actualDraw(canvas);
      if (shouldDrawScrim()) {
        canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), scrimPaint);
      }
    }

    drawOverlayDrawable(canvas);
  }

  private void drawOverlayDrawable(@NonNull Canvas canvas) {
    if (shouldDrawOverlayDrawable()) {
      Rect bounds = overlayDrawable.getBounds();
      float translationX = revealInfo.centerX - bounds.width() / 2f;
      float translationY = revealInfo.centerY - bounds.height() / 2f;

      canvas.translate(translationX, translationY);
      overlayDrawable.draw(canvas);
      canvas.translate(-translationX, -translationY);
    }
  }

  public boolean isOpaque() {
    return delegate.actualIsOpaque() && !shouldDrawCircularReveal();
  }

  private boolean shouldDrawCircularReveal() {
    boolean invalidRevealInfo = revealInfo == null || revealInfo.isInvalid();
    if (STRATEGY == BITMAP_SHADER) {
      return !invalidRevealInfo && hasCircularRevealCache;
    } else {
      return !invalidRevealInfo;
    }
  }

  private boolean shouldDrawScrim() {
    return !buildingCircularRevealCache && Color.alpha(scrimPaint.getColor()) != 0;
  }

  private boolean shouldDrawOverlayDrawable() {
    return !buildingCircularRevealCache && overlayDrawable != null && revealInfo != null;
  }

  private void drawDebugMode(@NonNull Canvas canvas) {
    delegate.actualDraw(canvas);
    if (shouldDrawScrim()) {
      canvas.drawCircle(revealInfo.centerX, revealInfo.centerY, revealInfo.radius, scrimPaint);
    }

    
    if (shouldDrawCircularReveal()) {
      drawDebugCircle(canvas, Color.BLACK, 10f);
      drawDebugCircle(canvas, Color.RED, 5f);
    }

    drawOverlayDrawable(canvas);
  }

  private void drawDebugCircle(@NonNull Canvas canvas, int color, float width) {
    debugPaint.setColor(color);
    debugPaint.setStrokeWidth(width);
    canvas.drawCircle(
        revealInfo.centerX, revealInfo.centerY, revealInfo.radius - width / 2, debugPaint);
  }
}
