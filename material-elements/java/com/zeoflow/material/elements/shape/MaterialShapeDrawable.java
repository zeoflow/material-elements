

package com.zeoflow.material.elements.shape;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.graphics.drawable.TintAwareDrawable;
import androidx.core.util.ObjectsCompat;
import android.util.AttributeSet;
import android.util.Log;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.elevation.ElevationOverlayProvider;
import com.zeoflow.material.elements.shadow.ShadowRenderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.BitSet;


public class MaterialShapeDrawable extends Drawable implements TintAwareDrawable, Shapeable {

  private static final String TAG = MaterialShapeDrawable.class.getSimpleName();

  private static final float SHADOW_RADIUS_MULTIPLIER = .75f;

  private static final float SHADOW_OFFSET_MULTIPLIER = .25f;

  
  public static final int SHADOW_COMPAT_MODE_DEFAULT = 0;

  
  public static final int SHADOW_COMPAT_MODE_NEVER = 1;

  
  public static final int SHADOW_COMPAT_MODE_ALWAYS = 2;

  
  @IntDef({SHADOW_COMPAT_MODE_DEFAULT, SHADOW_COMPAT_MODE_NEVER, SHADOW_COMPAT_MODE_ALWAYS})
  @Retention(RetentionPolicy.SOURCE)
  public @interface CompatibilityShadowMode {}

  private static final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private MaterialShapeDrawableState drawableState;

  
  private final ShapePath.ShadowCompatOperation[] cornerShadowOperation = new ShapePath.ShadowCompatOperation[4];
  private final ShapePath.ShadowCompatOperation[] edgeShadowOperation = new ShapePath.ShadowCompatOperation[4];
  private final BitSet containsIncompatibleShadowOp = new BitSet(8);
  private boolean pathDirty;

  
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();
  private final Path pathInsetByStroke = new Path();
  private final RectF rectF = new RectF();
  private final RectF insetRectF = new RectF();
  private final Region transparentRegion = new Region();
  private final Region scratchRegion = new Region();
  private ShapeAppearanceModel strokeShapeAppearance;

  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final ShadowRenderer shadowRenderer = new ShadowRenderer();
  @NonNull private final ShapeAppearancePathProvider.PathListener pathShadowListener;
  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();

  @Nullable private PorterDuffColorFilter tintFilter;
  @Nullable private PorterDuffColorFilter strokeTintFilter;

  @NonNull private final RectF pathBounds = new RectF();

  private boolean shadowBitmapDrawingEnable = true;

  
  @NonNull
  public static MaterialShapeDrawable createWithElevationOverlay(Context context) {
    return createWithElevationOverlay(context, 0);
  }

  
  @NonNull
  public static MaterialShapeDrawable createWithElevationOverlay(Context context, float elevation) {
    int colorSurface =
        MaterialColors.getColor(
            context, R.attr.colorSurface, MaterialShapeDrawable.class.getSimpleName());
    MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    materialShapeDrawable.initializeElevationOverlay(context);
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(colorSurface));
    materialShapeDrawable.setElevation(elevation);
    return materialShapeDrawable;
  }

  public MaterialShapeDrawable() {
    this(new ShapeAppearanceModel());
  }

  public MaterialShapeDrawable(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    this(ShapeAppearanceModel.builder(context, attrs, defStyleAttr, defStyleRes).build());
  }

  @Deprecated
  public MaterialShapeDrawable(@NonNull ShapePathModel shapePathModel) {
    this((ShapeAppearanceModel) shapePathModel);
  }

  
  public MaterialShapeDrawable(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this(new MaterialShapeDrawableState(shapeAppearanceModel, null));
  }

  private MaterialShapeDrawable(@NonNull MaterialShapeDrawableState drawableState) {
    this.drawableState = drawableState;
    strokePaint.setStyle(Style.STROKE);
    fillPaint.setStyle(Style.FILL);
    clearPaint.setColor(Color.WHITE);
    clearPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
    updateTintFilter();
    updateColorsForState(getState());
    
    pathShadowListener =
        new ShapeAppearancePathProvider.PathListener() {
          @Override
          public void onCornerPathCreated(
              @NonNull ShapePath cornerPath, Matrix transform, int count) {
            containsIncompatibleShadowOp.set(count, cornerPath.containsIncompatibleShadowOp());
            cornerShadowOperation[count] = cornerPath.createShadowCompatOperation(transform);
          }

          @Override
          public void onEdgePathCreated(@NonNull ShapePath edgePath, Matrix transform, int count) {
            containsIncompatibleShadowOp.set(count + 4, edgePath.containsIncompatibleShadowOp());
            edgeShadowOperation[count] = edgePath.createShadowCompatOperation(transform);
          }
        };
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return drawableState;
  }

  @NonNull
  @Override
  public Drawable mutate() {
    MaterialShapeDrawableState newDrawableState = new MaterialShapeDrawableState(drawableState);
    drawableState = newDrawableState;
    return this;
  }

  private static int modulateAlpha(int paintAlpha, int alpha) {
    int scale = alpha + (alpha >>> 7); 
    return (paintAlpha * scale) >>> 8;
  }

  
  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    drawableState.shapeAppearanceModel = shapeAppearanceModel;
    invalidateSelf();
  }

  
  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return drawableState.shapeAppearanceModel;
  }

  
  @Deprecated
  public void setShapedViewModel(@NonNull ShapePathModel shapedViewModel) {
    setShapeAppearanceModel(shapedViewModel);
  }

  
  @Deprecated
  @Nullable
  public ShapePathModel getShapedViewModel() {
    ShapeAppearanceModel shapeAppearance = getShapeAppearanceModel();
    return shapeAppearance instanceof ShapePathModel ? (ShapePathModel) shapeAppearance : null;
  }

  
  public void setFillColor(@Nullable ColorStateList fillColor) {
    if (drawableState.fillColor != fillColor) {
      drawableState.fillColor = fillColor;
      onStateChange(getState());
    }
  }

  
  @Nullable
  public ColorStateList getFillColor() {
    return drawableState.fillColor;
  }

  
  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (drawableState.strokeColor != strokeColor) {
      drawableState.strokeColor = strokeColor;
      onStateChange(getState());
    }
  }

  
  @Nullable
  public ColorStateList getStrokeColor() {
    return drawableState.strokeColor;
  }

  @Override
  public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (drawableState.tintMode != tintMode) {
      drawableState.tintMode = tintMode;
      updateTintFilter();
      invalidateSelfIgnoreShape();
    }
  }

  @Override
  public void setTintList(@Nullable ColorStateList tintList) {
    drawableState.tintList = tintList;
    updateTintFilter();
    invalidateSelfIgnoreShape();
  }

  
  @Nullable
  public ColorStateList getTintList() {
    return drawableState.tintList;
  }

  
  @Nullable
  public ColorStateList getStrokeTintList() {
    return drawableState.strokeTintList;
  }

  @Override
  public void setTint(@ColorInt int tintColor) {
    setTintList(ColorStateList.valueOf(tintColor));
  }

  
  public void setStrokeTint(ColorStateList tintList) {
    drawableState.strokeTintList = tintList;
    updateTintFilter();
    invalidateSelfIgnoreShape();
  }

  
  public void setStrokeTint(@ColorInt int tintColor) {
    setStrokeTint(ColorStateList.valueOf(tintColor));
  }

  
  public void setStroke(float strokeWidth, @ColorInt int strokeColor) {
    setStrokeWidth(strokeWidth);
    setStrokeColor(ColorStateList.valueOf(strokeColor));
  }

  
  public void setStroke(float strokeWidth, @Nullable ColorStateList strokeColor) {
    setStrokeWidth(strokeWidth);
    setStrokeColor(strokeColor);
  }

  
  public float getStrokeWidth() {
    return drawableState.strokeWidth;
  }

  
  public void setStrokeWidth(float strokeWidth) {
    drawableState.strokeWidth = strokeWidth;
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    
    
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    if (drawableState.alpha != alpha) {
      drawableState.alpha = alpha;
      invalidateSelfIgnoreShape();
    }
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    drawableState.colorFilter = colorFilter;
    invalidateSelfIgnoreShape();
  }

  @Override
  public Region getTransparentRegion() {
    Rect bounds = getBounds();
    transparentRegion.set(bounds);
    calculatePath(getBoundsAsRectF(), path);
    scratchRegion.setPath(path, transparentRegion);
    transparentRegion.op(scratchRegion, Op.DIFFERENCE);
    return transparentRegion;
  }

  @NonNull
  protected RectF getBoundsAsRectF() {
    rectF.set(getBounds());
    return rectF;
  }

  
  public void setCornerSize(float cornerSize) {
    setShapeAppearanceModel(drawableState.shapeAppearanceModel.withCornerSize(cornerSize));
  }

  
  public void setCornerSize(@NonNull CornerSize cornerSize) {
    setShapeAppearanceModel(drawableState.shapeAppearanceModel.withCornerSize(cornerSize));
  }

  
  public boolean isPointInTransparentRegion(int x, int y) {
    return getTransparentRegion().contains(x, y);
  }

  @CompatibilityShadowMode
  public int getShadowCompatibilityMode() {
    return drawableState.shadowCompatMode;
  }

  @Override
  public boolean getPadding(@NonNull Rect padding) {
    if (drawableState.padding != null) {
      padding.set(drawableState.padding);
      return true;
    } else {
      return super.getPadding(padding);
    }
  }

  
  public void setPadding(int left, int top, int right, int bottom) {
    if (drawableState.padding == null) {
      drawableState.padding = new Rect();
    }

    drawableState.padding.set(left, top, right, bottom);
    invalidateSelf();
  }

  
  public void setShadowCompatibilityMode(@CompatibilityShadowMode int mode) {
    if (drawableState.shadowCompatMode != mode) {
      drawableState.shadowCompatMode = mode;
      invalidateSelfIgnoreShape();
    }
  }

  
  @Deprecated
  public boolean isShadowEnabled() {
    return drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_DEFAULT
        || drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS;
  }

  
  @Deprecated
  public void setShadowEnabled(boolean shadowEnabled) {
    setShadowCompatibilityMode(
        shadowEnabled ? SHADOW_COMPAT_MODE_DEFAULT : SHADOW_COMPAT_MODE_NEVER);
  }

  
  public boolean isElevationOverlayEnabled() {
    return drawableState.elevationOverlayProvider != null
        && drawableState.elevationOverlayProvider.isThemeElevationOverlayEnabled();
  }

  
  public boolean isElevationOverlayInitialized() {
    return drawableState.elevationOverlayProvider != null;
  }

  
  public void initializeElevationOverlay(Context context) {
    drawableState.elevationOverlayProvider = new ElevationOverlayProvider(context);
    updateZ();
  }

  @ColorInt
  private int compositeElevationOverlayIfNeeded(@ColorInt int backgroundColor) {
    float elevation = getZ() + getParentAbsoluteElevation();
    return drawableState.elevationOverlayProvider != null
        ? drawableState.elevationOverlayProvider.compositeOverlayIfNeeded(
            backgroundColor, elevation)
        : backgroundColor;
  }

  
  public float getInterpolation() {
    return drawableState.interpolation;
  }

  
  public void setInterpolation(float interpolation) {
    if (drawableState.interpolation != interpolation) {
      drawableState.interpolation = interpolation;
      pathDirty = true;
      invalidateSelf();
    }
  }

  
  public float getParentAbsoluteElevation() {
    return drawableState.parentAbsoluteElevation;
  }

  
  public void setParentAbsoluteElevation(float parentAbsoluteElevation) {
    if (drawableState.parentAbsoluteElevation != parentAbsoluteElevation) {
      drawableState.parentAbsoluteElevation = parentAbsoluteElevation;
      updateZ();
    }
  }

  
  public float getElevation() {
    return drawableState.elevation;
  }

  
  public void setElevation(float elevation) {
    if (drawableState.elevation != elevation) {
      drawableState.elevation = elevation;
      updateZ();
    }
  }

  
  public float getTranslationZ() {
    return drawableState.translationZ;
  }

  
  public void setTranslationZ(float translationZ) {
    if (drawableState.translationZ != translationZ) {
      drawableState.translationZ = translationZ;
      updateZ();
    }
  }

  
  public float getZ() {
    return getElevation() + getTranslationZ();
  }

  
  public void setZ(float z) {
    setTranslationZ(z - getElevation());
  }

  private void updateZ() {
    float z = getZ();
    drawableState.shadowCompatRadius = (int) Math.ceil(z * SHADOW_RADIUS_MULTIPLIER);
    drawableState.shadowCompatOffset = (int) Math.ceil(z * SHADOW_OFFSET_MULTIPLIER);
    
    updateTintFilter();
    invalidateSelfIgnoreShape();
  }

  
  @Deprecated
  public int getShadowElevation() {
    return (int) getElevation();
  }

  
  @Deprecated
  public void setShadowElevation(int shadowElevation) {
    setElevation(shadowElevation);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public int getShadowVerticalOffset() {
    return drawableState.shadowCompatOffset;
  }

  @RestrictTo(LIBRARY_GROUP)
  public void setShadowBitmapDrawingEnable(boolean enable) {
    shadowBitmapDrawingEnable = enable;
  }

  @RestrictTo(LIBRARY_GROUP)
  public void setEdgeIntersectionCheckEnable(boolean enable) {
    pathProvider.setEdgeIntersectionCheckEnable(enable);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public void setShadowVerticalOffset(int shadowOffset) {
    if (drawableState.shadowCompatOffset != shadowOffset) {
      drawableState.shadowCompatOffset = shadowOffset;
      invalidateSelfIgnoreShape();
    }
  }

  
  public int getShadowCompatRotation() {
    return drawableState.shadowCompatRotation;
  }

  
  public void setShadowCompatRotation(int shadowRotation) {
    if (drawableState.shadowCompatRotation != shadowRotation) {
      drawableState.shadowCompatRotation = shadowRotation;
      invalidateSelfIgnoreShape();
    }
  }

  
  public int getShadowRadius() {
    return drawableState.shadowCompatRadius;
  }

  
  @Deprecated
  public void setShadowRadius(int shadowRadius) {
    drawableState.shadowCompatRadius = shadowRadius;
  }

  
  public boolean requiresCompatShadow() {
    return VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
        || (!isRoundRect() && !path.isConvex() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
  }

  
  public float getScale() {
    return drawableState.scale;
  }

  
  public void setScale(float scale) {
    if (drawableState.scale != scale) {
      drawableState.scale = scale;
      invalidateSelf();
    }
  }

  @Override
  public void invalidateSelf() {
    pathDirty = true;
    super.invalidateSelf();
  }

  
  private void invalidateSelfIgnoreShape() {
    super.invalidateSelf();
  }

  
  public void setUseTintColorForShadow(boolean useTintColorForShadow) {
    if (drawableState.useTintColorForShadow != useTintColorForShadow) {
      drawableState.useTintColorForShadow = useTintColorForShadow;
      invalidateSelf();
    }
  }

  
  public void setShadowColor(int shadowColor) {
    shadowRenderer.setShadowColor(shadowColor);
    drawableState.useTintColorForShadow = false;
    invalidateSelfIgnoreShape();
  }

  
  public Style getPaintStyle() {
    return drawableState.paintStyle;
  }

  
  public void setPaintStyle(Style paintStyle) {
    drawableState.paintStyle = paintStyle;
    invalidateSelfIgnoreShape();
  }

  
  private boolean hasCompatShadow() {
    return drawableState.shadowCompatMode != SHADOW_COMPAT_MODE_NEVER
        && drawableState.shadowCompatRadius > 0
        && (drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS || requiresCompatShadow());
  }

  
  private boolean hasFill() {
    return drawableState.paintStyle == Style.FILL_AND_STROKE
        || drawableState.paintStyle == Style.FILL;
  }

  
  private boolean hasStroke() {
    return (drawableState.paintStyle == Style.FILL_AND_STROKE
            || drawableState.paintStyle == Style.STROKE)
        && strokePaint.getStrokeWidth() > 0;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    pathDirty = true;
    super.onBoundsChange(bounds);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    fillPaint.setColorFilter(tintFilter);
    final int prevAlpha = fillPaint.getAlpha();
    fillPaint.setAlpha(modulateAlpha(prevAlpha, drawableState.alpha));

    strokePaint.setColorFilter(strokeTintFilter);
    strokePaint.setStrokeWidth(drawableState.strokeWidth);

    final int prevStrokeAlpha = strokePaint.getAlpha();
    strokePaint.setAlpha(modulateAlpha(prevStrokeAlpha, drawableState.alpha));

    if (pathDirty) {
      calculateStrokePath();
      calculatePath(getBoundsAsRectF(), path);
      pathDirty = false;
    }

    maybeDrawCompatShadow(canvas);
    if (hasFill()) {
      drawFillShape(canvas);
    }
    if (hasStroke()) {
      drawStrokeShape(canvas);
    }

    fillPaint.setAlpha(prevAlpha);
    strokePaint.setAlpha(prevStrokeAlpha);
  }

  private void maybeDrawCompatShadow(@NonNull Canvas canvas) {
    if (!hasCompatShadow()) {
      return;
    }
    
    canvas.save();
    prepareCanvasForShadow(canvas);
    if (!shadowBitmapDrawingEnable) {
      drawCompatShadow(canvas);
      canvas.restore();
      return;
    }

    
    
    int pathExtraWidth = (int) (pathBounds.width() - getBounds().width());
    int pathExtraHeight = (int) (pathBounds.height() - getBounds().height());

    if (pathExtraWidth < 0 || pathExtraHeight < 0) {
      throw new IllegalStateException(
          "Invalid shadow bounds. Check that the treatments result in a valid path.");
    }

    
    
    Bitmap shadowLayer =
        Bitmap.createBitmap(
            (int) pathBounds.width() + drawableState.shadowCompatRadius * 2 + pathExtraWidth,
            (int) pathBounds.height() + drawableState.shadowCompatRadius * 2 + pathExtraHeight,
            Bitmap.Config.ARGB_8888);
    Canvas shadowCanvas = new Canvas(shadowLayer);

    
    
    float shadowLeft = getBounds().left - drawableState.shadowCompatRadius - pathExtraWidth;
    float shadowTop = getBounds().top - drawableState.shadowCompatRadius - pathExtraHeight;
    shadowCanvas.translate(-shadowLeft, -shadowTop);
    drawCompatShadow(shadowCanvas);
    canvas.drawBitmap(shadowLayer, shadowLeft, shadowTop, null);
    
    
    shadowLayer.recycle();

    
    canvas.restore();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  protected void drawShape(
      @NonNull Canvas canvas, @NonNull Paint paint, @NonNull Path path, @NonNull RectF bounds) {
    drawShape(canvas, paint, path, drawableState.shapeAppearanceModel, bounds);
  }

  
  private void drawShape(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull Path path,
      @NonNull ShapeAppearanceModel shapeAppearanceModel,
      @NonNull RectF bounds) {
    if (shapeAppearanceModel.isRoundRect(bounds)) {
      float cornerSize =
          shapeAppearanceModel.getTopRightCornerSize().getCornerSize(bounds)
              * drawableState.interpolation;
      canvas.drawRoundRect(bounds, cornerSize, cornerSize, paint);
    } else {
      canvas.drawPath(path, paint);
    }
  }

  private void drawFillShape(@NonNull Canvas canvas) {
    drawShape(canvas, fillPaint, path, drawableState.shapeAppearanceModel, getBoundsAsRectF());
  }

  private void drawStrokeShape(@NonNull Canvas canvas) {
    drawShape(
        canvas, strokePaint, pathInsetByStroke, strokeShapeAppearance, getBoundsInsetByStroke());
  }

  private void prepareCanvasForShadow(@NonNull Canvas canvas) {
    
    int shadowOffsetX = getShadowOffsetX();
    int shadowOffsetY = getShadowOffsetY();

    
    
    
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP && shadowBitmapDrawingEnable) {
      
      
      Rect canvasClipBounds = canvas.getClipBounds();
      canvasClipBounds.inset(-drawableState.shadowCompatRadius, -drawableState.shadowCompatRadius);
      canvasClipBounds.offset(shadowOffsetX, shadowOffsetY);
      canvas.clipRect(canvasClipBounds, Region.Op.REPLACE);
    }

    
    
    canvas.translate(shadowOffsetX, shadowOffsetY);
  }

  
  private void drawCompatShadow(@NonNull Canvas canvas) {
    if (containsIncompatibleShadowOp.cardinality() > 0) {
      Log.w(
          TAG,
          "Compatibility shadow requested but can't be drawn for all operations in this shape.");
    }

    if (drawableState.shadowCompatOffset != 0) {
      canvas.drawPath(path, shadowRenderer.getShadowPaint());
    }

    
    for (int index = 0; index < 4; index++) {
      cornerShadowOperation[index].draw(shadowRenderer, drawableState.shadowCompatRadius, canvas);
      edgeShadowOperation[index].draw(shadowRenderer, drawableState.shadowCompatRadius, canvas);
    }

    if (shadowBitmapDrawingEnable) {
      int shadowOffsetX = getShadowOffsetX();
      int shadowOffsetY = getShadowOffsetY();

      canvas.translate(-shadowOffsetX, -shadowOffsetY);
      canvas.drawPath(path, clearPaint);
      canvas.translate(shadowOffsetX, shadowOffsetY);
    }
  }

  
  public int getShadowOffsetX() {
    return (int)
        (drawableState.shadowCompatOffset
            * Math.sin(Math.toRadians(drawableState.shadowCompatRotation)));
  }

  
  public int getShadowOffsetY() {
    return (int)
        (drawableState.shadowCompatOffset
            * Math.cos(Math.toRadians(drawableState.shadowCompatRotation)));
  }

  
  @Deprecated
  public void getPathForSize(int width, int height, @NonNull Path path) {
    calculatePathForSize(new RectF(0, 0, width, height), path);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  protected final void calculatePathForSize(@NonNull RectF bounds, @NonNull Path path) {
    pathProvider.calculatePath(
        drawableState.shapeAppearanceModel,
        drawableState.interpolation,
        bounds,
        pathShadowListener,
        path);
  }

  
  private void calculateStrokePath() {
    
    
    final float strokeInsetLength = -getStrokeInsetLength();
    strokeShapeAppearance =
        getShapeAppearanceModel()
            .withTransformedCornerSizes(
                new ShapeAppearanceModel.CornerSizeUnaryOperator() {
                  @NonNull
                  @Override
                  public CornerSize apply(@NonNull CornerSize cornerSize) {
                    
                    
                    return cornerSize instanceof RelativeCornerSize
                        ? cornerSize
                        : new AdjustedCornerSize(strokeInsetLength, cornerSize);
                  }
                });

    pathProvider.calculatePath(
        strokeShapeAppearance,
        drawableState.interpolation,
        getBoundsInsetByStroke(),
        pathInsetByStroke);
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void getOutline(@NonNull Outline outline) {
    if (drawableState.shadowCompatMode == SHADOW_COMPAT_MODE_ALWAYS) {
      
      return;
    }

    if (isRoundRect()) {
      float radius = getTopLeftCornerResolvedSize() * drawableState.interpolation;
      outline.setRoundRect(getBounds(), radius);
      return;
    }

    calculatePath(getBoundsAsRectF(), path);
    if (path.isConvex() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        outline.setConvexPath(path);
      } catch (IllegalArgumentException ignored) {
        
        
      }

    }
  }

  private void calculatePath(@NonNull RectF bounds, @NonNull Path path) {
    calculatePathForSize(bounds, path);

    if (drawableState.scale != 1f) {
      matrix.reset();
      matrix.setScale(
          drawableState.scale, drawableState.scale, bounds.width() / 2.0f, bounds.height() / 2.0f);
      path.transform(matrix);
    }

    
    path.computeBounds(pathBounds, true);
  }

  private boolean updateTintFilter() {
    PorterDuffColorFilter originalTintFilter = tintFilter;
    PorterDuffColorFilter originalStrokeTintFilter = strokeTintFilter;
    tintFilter =
        calculateTintFilter(
            drawableState.tintList,
            drawableState.tintMode,
            fillPaint,
             true);
    strokeTintFilter =
        calculateTintFilter(
            drawableState.strokeTintList,
            drawableState.tintMode,
            strokePaint,
             false);
    if (drawableState.useTintColorForShadow) {
      shadowRenderer.setShadowColor(
          drawableState.tintList.getColorForState(getState(), Color.TRANSPARENT));
    }
    return !ObjectsCompat.equals(originalTintFilter, tintFilter)
        || !ObjectsCompat.equals(originalStrokeTintFilter, strokeTintFilter);
  }

  @NonNull
  private PorterDuffColorFilter calculateTintFilter(
      @Nullable ColorStateList tintList,
      @Nullable PorterDuff.Mode tintMode,
      @NonNull Paint paint,
      boolean requiresElevationOverlay) {
    return tintList == null || tintMode == null
        ? calculatePaintColorTintFilter(paint, requiresElevationOverlay)
        : calculateTintColorTintFilter(tintList, tintMode, requiresElevationOverlay);
  }

  @Nullable
  private PorterDuffColorFilter calculatePaintColorTintFilter(
      @NonNull Paint paint, boolean requiresElevationOverlay) {
    if (requiresElevationOverlay) {
      int paintColor = paint.getColor();
      int tintColor = compositeElevationOverlayIfNeeded(paintColor);
      if (tintColor != paintColor) {
        return new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
      }
    }
    return null;
  }

  @NonNull
  private PorterDuffColorFilter calculateTintColorTintFilter(
      @NonNull ColorStateList tintList,
      @NonNull PorterDuff.Mode tintMode,
      boolean requiresElevationOverlay) {
    int tintColor = tintList.getColorForState(getState(), Color.TRANSPARENT);
    if (requiresElevationOverlay) {
      tintColor = compositeElevationOverlayIfNeeded(tintColor);
    }
    return new PorterDuffColorFilter(tintColor, tintMode);
  }

  @Override
  public boolean isStateful() {
    return super.isStateful()
        || (drawableState.tintList != null && drawableState.tintList.isStateful())
        || (drawableState.strokeTintList != null && drawableState.strokeTintList.isStateful())
        || (drawableState.strokeColor != null && drawableState.strokeColor.isStateful())
        || (drawableState.fillColor != null && drawableState.fillColor.isStateful());
  }

  @Override
  protected boolean onStateChange(int[] state) {
    boolean paintColorChanged = updateColorsForState(state);
    boolean tintFilterChanged = updateTintFilter();
    boolean invalidateSelf = paintColorChanged || tintFilterChanged;
    if (invalidateSelf) {
      invalidateSelf();
    }
    return invalidateSelf;
  }

  private boolean updateColorsForState(int[] state) {
    boolean invalidateSelf = false;

    if (drawableState.fillColor != null) {
      final int previousFillColor = fillPaint.getColor();
      final int newFillColor = drawableState.fillColor.getColorForState(state, previousFillColor);
      if (previousFillColor != newFillColor) {
        fillPaint.setColor(newFillColor);
        invalidateSelf = true;
      }
    }

    if (drawableState.strokeColor != null) {
      final int previousStrokeColor = strokePaint.getColor();
      final int newStrokeColor =
          drawableState.strokeColor.getColorForState(state, previousStrokeColor);
      if (previousStrokeColor != newStrokeColor) {
        strokePaint.setColor(newStrokeColor);
        invalidateSelf = true;
      }
    }

    return invalidateSelf;
  }

  private float getStrokeInsetLength() {
    if (hasStroke()) {
      return strokePaint.getStrokeWidth() / 2.0f;
    }
    return 0f;
  }

  @NonNull
  private RectF getBoundsInsetByStroke() {
    insetRectF.set(getBoundsAsRectF());
    float inset = getStrokeInsetLength();
    insetRectF.inset(inset, inset);
    return insetRectF;
  }

  
  public float getTopLeftCornerResolvedSize() {
    return drawableState
        .shapeAppearanceModel
        .getTopLeftCornerSize()
        .getCornerSize(getBoundsAsRectF());
  }

  
  public float getTopRightCornerResolvedSize() {
    return drawableState
        .shapeAppearanceModel
        .getTopRightCornerSize()
        .getCornerSize(getBoundsAsRectF());
  }

  
  public float getBottomLeftCornerResolvedSize() {
    return drawableState
        .shapeAppearanceModel
        .getBottomLeftCornerSize()
        .getCornerSize(getBoundsAsRectF());
  }

  
  public float getBottomRightCornerResolvedSize() {
    return drawableState
        .shapeAppearanceModel
        .getBottomRightCornerSize()
        .getCornerSize(getBoundsAsRectF());
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public boolean isRoundRect() {
    return drawableState.shapeAppearanceModel.isRoundRect(getBoundsAsRectF());
  }

  static final class MaterialShapeDrawableState extends ConstantState {

    @NonNull public ShapeAppearanceModel shapeAppearanceModel;
    @Nullable public ElevationOverlayProvider elevationOverlayProvider;

    @Nullable public ColorFilter colorFilter;
    @Nullable public ColorStateList fillColor = null;
    @Nullable public ColorStateList strokeColor = null;
    @Nullable public ColorStateList strokeTintList = null;
    @Nullable public ColorStateList tintList = null;
    @Nullable public PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;
    @Nullable public Rect padding = null;

    public float scale = 1f;
    public float interpolation = 1f;
    public float strokeWidth;

    public int alpha = 255;
    public float parentAbsoluteElevation = 0;
    public float elevation = 0;
    public float translationZ = 0;
    public int shadowCompatMode = SHADOW_COMPAT_MODE_DEFAULT;
    public int shadowCompatRadius = 0;
    public int shadowCompatOffset = 0;
    public int shadowCompatRotation = 0;

    public boolean useTintColorForShadow = false;

    public Style paintStyle = Style.FILL_AND_STROKE;

    public MaterialShapeDrawableState(
        ShapeAppearanceModel shapeAppearanceModel,
        ElevationOverlayProvider elevationOverlayProvider) {
      this.shapeAppearanceModel = shapeAppearanceModel;
      this.elevationOverlayProvider = elevationOverlayProvider;
    }

    public MaterialShapeDrawableState(@NonNull MaterialShapeDrawableState orig) {
      shapeAppearanceModel = orig.shapeAppearanceModel;
      elevationOverlayProvider = orig.elevationOverlayProvider;
      strokeWidth = orig.strokeWidth;
      colorFilter = orig.colorFilter;
      fillColor = orig.fillColor;
      strokeColor = orig.strokeColor;
      tintMode = orig.tintMode;
      tintList = orig.tintList;
      alpha = orig.alpha;
      scale = orig.scale;
      shadowCompatOffset = orig.shadowCompatOffset;
      shadowCompatMode = orig.shadowCompatMode;
      useTintColorForShadow = orig.useTintColorForShadow;
      interpolation = orig.interpolation;
      parentAbsoluteElevation = orig.parentAbsoluteElevation;
      elevation = orig.elevation;
      translationZ = orig.translationZ;
      shadowCompatRadius = orig.shadowCompatRadius;
      shadowCompatRotation = orig.shadowCompatRotation;
      strokeTintList = orig.strokeTintList;
      paintStyle = orig.paintStyle;
      if (orig.padding != null) {
        padding = new Rect(orig.padding);
      }
    }

    @NonNull
    @Override
    public Drawable newDrawable() {
      MaterialShapeDrawable msd = new MaterialShapeDrawable(this);
      
      msd.pathDirty = true;
      return msd;
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
