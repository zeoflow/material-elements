

package com.zeoflow.material.elements.transition;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.core.util.Preconditions.checkNotNull;
import static com.zeoflow.material.elements.transition.TransitionUtils.lerp;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.transition.ArcMotion;
import androidx.transition.PathMotion;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public final class MaterialContainerTransform extends Transition {

  
  public static final int TRANSITION_DIRECTION_AUTO = 0;

  
  public static final int TRANSITION_DIRECTION_ENTER = 1;

  
  public static final int TRANSITION_DIRECTION_RETURN = 2;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({TRANSITION_DIRECTION_AUTO, TRANSITION_DIRECTION_ENTER, TRANSITION_DIRECTION_RETURN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface TransitionDirection {}

  
  public static final int FADE_MODE_IN = 0;

  
  public static final int FADE_MODE_OUT = 1;

  
  public static final int FADE_MODE_CROSS = 2;

  
  public static final int FADE_MODE_THROUGH = 3;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({FADE_MODE_IN, FADE_MODE_OUT, FADE_MODE_CROSS, FADE_MODE_THROUGH})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FadeMode {}

  
  public static final int FIT_MODE_AUTO = 0;

  
  public static final int FIT_MODE_WIDTH = 1;

  
  public static final int FIT_MODE_HEIGHT = 2;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({FIT_MODE_AUTO, FIT_MODE_WIDTH, FIT_MODE_HEIGHT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface FitMode {}

  private static final String TAG = MaterialContainerTransform.class.getSimpleName();
  private static final String PROP_BOUNDS = "materialContainerTransition:bounds";
  private static final String PROP_SHAPE_APPEARANCE = "materialContainerTransition:shapeAppearance";
  private static final String[] TRANSITION_PROPS =
      new String[] {PROP_BOUNDS, PROP_SHAPE_APPEARANCE};

  
  
  
  private static final ProgressThresholdsGroup DEFAULT_ENTER_THRESHOLDS =
      new ProgressThresholdsGroup(
           new ProgressThresholds(0f, 0.25f),
           new ProgressThresholds(0f, 1f),
           new ProgressThresholds(0f, 1f),
           new ProgressThresholds(0f, 0.75f));
  private static final ProgressThresholdsGroup DEFAULT_RETURN_THRESHOLDS =
      new ProgressThresholdsGroup(
           new ProgressThresholds(0.60f, 0.90f),
           new ProgressThresholds(0f, 1f),
           new ProgressThresholds(0f, 0.90f),
           new ProgressThresholds(0.30f, 0.90f));

  
  
  private static final ProgressThresholdsGroup DEFAULT_ENTER_THRESHOLDS_ARC =
      new ProgressThresholdsGroup(
           new ProgressThresholds(0.10f, 0.40f),
           new ProgressThresholds(0.10f, 1f),
           new ProgressThresholds(0.10f, 1f),
           new ProgressThresholds(0.10f, 0.90f));
  private static final ProgressThresholdsGroup DEFAULT_RETURN_THRESHOLDS_ARC =
      new ProgressThresholdsGroup(
           new ProgressThresholds(0.60f, 0.90f),
           new ProgressThresholds(0f, 0.90f),
           new ProgressThresholds(0f, 0.90f),
           new ProgressThresholds(0.20f, 0.90f));

  private static final float ELEVATION_NOT_SET = -1f;

  private boolean drawDebugEnabled = false;
  private boolean holdAtEndEnabled = false;
  @IdRes private int drawingViewId = android.R.id.content;
  @IdRes private int startViewId = View.NO_ID;
  @IdRes private int endViewId = View.NO_ID;
  @ColorInt private int containerColor = Color.TRANSPARENT;
  @ColorInt private int startContainerColor = Color.TRANSPARENT;
  @ColorInt private int endContainerColor = Color.TRANSPARENT;
  @ColorInt private int scrimColor = 0x52000000;
  @TransitionDirection private int transitionDirection = TRANSITION_DIRECTION_AUTO;
  @FadeMode private int fadeMode = FADE_MODE_IN;
  @FitMode private int fitMode = FIT_MODE_AUTO;
  @Nullable private View startView;
  @Nullable private View endView;
  @Nullable private ShapeAppearanceModel startShapeAppearanceModel;
  @Nullable private ShapeAppearanceModel endShapeAppearanceModel;
  @Nullable private ProgressThresholds fadeProgressThresholds;
  @Nullable private ProgressThresholds scaleProgressThresholds;
  @Nullable private ProgressThresholds scaleMaskProgressThresholds;
  @Nullable private ProgressThresholds shapeMaskProgressThresholds;
  private boolean elevationShadowEnabled = VERSION.SDK_INT >= VERSION_CODES.P;
  private float startElevation = ELEVATION_NOT_SET;
  private float endElevation = ELEVATION_NOT_SET;

  public MaterialContainerTransform() {
    setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
  }

  
  @IdRes
  public int getStartViewId() {
    return startViewId;
  }

  
  public void setStartViewId(@IdRes int startViewId) {
    this.startViewId = startViewId;
  }

  
  @IdRes
  public int getEndViewId() {
    return endViewId;
  }

  
  public void setEndViewId(@IdRes int endViewId) {
    this.endViewId = endViewId;
  }

  
  @Nullable
  public View getStartView() {
    return startView;
  }

  
  public void setStartView(@Nullable View startView) {
    this.startView = startView;
  }

  
  @Nullable
  public View getEndView() {
    return endView;
  }

  
  public void setEndView(@Nullable View endView) {
    this.endView = endView;
  }

  
  @Nullable
  public ShapeAppearanceModel getStartShapeAppearanceModel() {
    return startShapeAppearanceModel;
  }

  
  public void setStartShapeAppearanceModel(
      @Nullable ShapeAppearanceModel startShapeAppearanceModel) {
    this.startShapeAppearanceModel = startShapeAppearanceModel;
  }

  
  @Nullable
  public ShapeAppearanceModel getEndShapeAppearanceModel() {
    return endShapeAppearanceModel;
  }

  
  public void setEndShapeAppearanceModel(@Nullable ShapeAppearanceModel endShapeAppearanceModel) {
    this.endShapeAppearanceModel = endShapeAppearanceModel;
  }

  
  public boolean isElevationShadowEnabled() {
    return elevationShadowEnabled;
  }

  
  public void setElevationShadowEnabled(boolean elevationShadowEnabled) {
    this.elevationShadowEnabled = elevationShadowEnabled;
  }

  
  public float getStartElevation() {
    return startElevation;
  }

  
  public void setStartElevation(float startElevation) {
    this.startElevation = startElevation;
  }

  
  public float getEndElevation() {
    return endElevation;
  }

  
  public void setEndElevation(float endElevation) {
    this.endElevation = endElevation;
  }

  
  @IdRes
  public int getDrawingViewId() {
    return drawingViewId;
  }

  
  public void setDrawingViewId(@IdRes int drawingViewId) {
    this.drawingViewId = drawingViewId;
  }

  
  @ColorInt
  public int getContainerColor() {
    return containerColor;
  }

  
  public void setContainerColor(@ColorInt int containerColor) {
    this.containerColor = containerColor;
  }

  
  @ColorInt
  public int getStartContainerColor() {
    return startContainerColor;
  }

  
  public void setStartContainerColor(@ColorInt int containerColor) {
    this.startContainerColor = containerColor;
  }

  
  @ColorInt
  public int getEndContainerColor() {
    return endContainerColor;
  }

  
  public void setEndContainerColor(@ColorInt int containerColor) {
    this.endContainerColor = containerColor;
  }

  
  public void setAllContainerColors(@ColorInt int containerColor) {
    this.containerColor = containerColor;
    this.startContainerColor = containerColor;
    this.endContainerColor = containerColor;
  }

  
  @ColorInt
  public int getScrimColor() {
    return scrimColor;
  }

  
  public void setScrimColor(@ColorInt int scrimColor) {
    this.scrimColor = scrimColor;
  }

  
  @TransitionDirection
  public int getTransitionDirection() {
    return transitionDirection;
  }

  
  public void setTransitionDirection(@TransitionDirection int transitionDirection) {
    this.transitionDirection = transitionDirection;
  }

  
  @FadeMode
  public int getFadeMode() {
    return fadeMode;
  }

  
  public void setFadeMode(@FadeMode int fadeMode) {
    this.fadeMode = fadeMode;
  }

  
  @FitMode
  public int getFitMode() {
    return fitMode;
  }

  
  public void setFitMode(@FitMode int fitMode) {
    this.fitMode = fitMode;
  }

  
  @Nullable
  public ProgressThresholds getFadeProgressThresholds() {
    return fadeProgressThresholds;
  }

  
  public void setFadeProgressThresholds(@Nullable ProgressThresholds fadeProgressThresholds) {
    this.fadeProgressThresholds = fadeProgressThresholds;
  }

  
  @Nullable
  public ProgressThresholds getScaleProgressThresholds() {
    return scaleProgressThresholds;
  }

  
  public void setScaleProgressThresholds(@Nullable ProgressThresholds scaleProgressThresholds) {
    this.scaleProgressThresholds = scaleProgressThresholds;
  }

  
  @Nullable
  public ProgressThresholds getScaleMaskProgressThresholds() {
    return scaleMaskProgressThresholds;
  }

  
  public void setScaleMaskProgressThresholds(
      @Nullable ProgressThresholds scaleMaskProgressThresholds) {
    this.scaleMaskProgressThresholds = scaleMaskProgressThresholds;
  }

  
  @Nullable
  public ProgressThresholds getShapeMaskProgressThresholds() {
    return shapeMaskProgressThresholds;
  }

  
  public void setShapeMaskProgressThresholds(
      @Nullable ProgressThresholds shapeMaskProgressThresholds) {
    this.shapeMaskProgressThresholds = shapeMaskProgressThresholds;
  }

  
  public boolean isHoldAtEndEnabled() {
    return holdAtEndEnabled;
  }

  
  public void setHoldAtEndEnabled(boolean holdAtEndEnabled) {
    this.holdAtEndEnabled = holdAtEndEnabled;
  }

  
  public boolean isDrawDebugEnabled() {
    return drawDebugEnabled;
  }

  
  public void setDrawDebugEnabled(boolean drawDebugEnabled) {
    this.drawDebugEnabled = drawDebugEnabled;
  }

  @Nullable
  @Override
  public String[] getTransitionProperties() {
    return TRANSITION_PROPS;
  }

  @Override
  public void captureStartValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues, startView, startViewId, startShapeAppearanceModel);
  }

  @Override
  public void captureEndValues(@NonNull TransitionValues transitionValues) {
    captureValues(transitionValues, endView, endViewId, endShapeAppearanceModel);
  }

  private static void captureValues(
      @NonNull TransitionValues transitionValues,
      @Nullable View viewOverride,
      @IdRes int viewIdOverride,
      @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    if (viewIdOverride != View.NO_ID) {
      transitionValues.view = TransitionUtils.findDescendantOrAncestorById(transitionValues.view, viewIdOverride);
    } else if (viewOverride != null) {
      transitionValues.view = viewOverride;
    } else if (transitionValues.view.getTag(R.id.mtrl_motion_snapshot_view) instanceof View) {
      View snapshotView = (View) transitionValues.view.getTag(R.id.mtrl_motion_snapshot_view);

      
      transitionValues.view.setTag(R.id.mtrl_motion_snapshot_view, null);

      
      transitionValues.view = snapshotView;
    }
    View view = transitionValues.view;

    if (ViewCompat.isLaidOut(view) || view.getWidth() != 0 || view.getHeight() != 0) {
      
      RectF bounds = view.getParent() == null ? TransitionUtils.getRelativeBounds(view) : TransitionUtils.getLocationOnScreen(view);
      transitionValues.values.put(PROP_BOUNDS, bounds);
      transitionValues.values.put(
          PROP_SHAPE_APPEARANCE,
          captureShapeAppearance(view, bounds, shapeAppearanceModelOverride));
    }
  }

  
  private static ShapeAppearanceModel captureShapeAppearance(
      @NonNull View view,
      @NonNull RectF bounds,
      @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    ShapeAppearanceModel shapeAppearanceModel =
        getShapeAppearance(view, shapeAppearanceModelOverride);
    return TransitionUtils.convertToRelativeCornerSizes(shapeAppearanceModel, bounds);
  }

  
  
  private static ShapeAppearanceModel getShapeAppearance(
      @NonNull View view, @Nullable ShapeAppearanceModel shapeAppearanceModelOverride) {
    if (shapeAppearanceModelOverride != null) {
      return shapeAppearanceModelOverride;
    }

    if (view.getTag(R.id.mtrl_motion_snapshot_view) instanceof ShapeAppearanceModel) {
      return (ShapeAppearanceModel) view.getTag(R.id.mtrl_motion_snapshot_view);
    }

    Context context = view.getContext();
    int transitionShapeAppearanceResId = getTransitionShapeAppearanceResId(context);
    if (transitionShapeAppearanceResId != -1) {
      return ShapeAppearanceModel.builder(context, transitionShapeAppearanceResId, 0).build();
    }

    if (view instanceof Shapeable) {
      return ((Shapeable) view).getShapeAppearanceModel();
    }

    return ShapeAppearanceModel.builder().build();
  }

  @StyleRes
  private static int getTransitionShapeAppearanceResId(Context context) {
    TypedArray a = context.obtainStyledAttributes(new int[] {R.attr.transitionShapeAppearance});
    int transitionShapeAppearanceResId = a.getResourceId(0, -1);
    a.recycle();
    return transitionShapeAppearanceResId;
  }

  @Nullable
  @Override
  public Animator createAnimator(
      @NonNull ViewGroup sceneRoot,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (startValues == null || endValues == null) {
      return null;
    }

    RectF startBounds = (RectF) startValues.values.get(PROP_BOUNDS);
    ShapeAppearanceModel startShapeAppearanceModel =
        (ShapeAppearanceModel) startValues.values.get(PROP_SHAPE_APPEARANCE);
    if (startBounds == null || startShapeAppearanceModel == null) {
      Log.w(TAG, "Skipping due to null start bounds. Ensure start view is laid out and measured.");
      return null;
    }

    RectF endBounds = (RectF) endValues.values.get(PROP_BOUNDS);
    ShapeAppearanceModel endShapeAppearanceModel =
        (ShapeAppearanceModel) endValues.values.get(PROP_SHAPE_APPEARANCE);
    if (endBounds == null || endShapeAppearanceModel == null) {
      Log.w(TAG, "Skipping due to null end bounds. Ensure end view is laid out and measured.");
      return null;
    }

    final View startView = startValues.view;
    final View endView = endValues.view;
    final View drawingView;
    View boundingView;
    View drawingBaseView = endView.getParent() != null ? endView : startView;
    if (drawingViewId == drawingBaseView.getId()) {
      drawingView = (View) drawingBaseView.getParent();
      boundingView = drawingBaseView;
    } else {
      drawingView = TransitionUtils.findAncestorById(drawingBaseView, drawingViewId);
      boundingView = null;
    }

    
    RectF drawingViewBounds = TransitionUtils.getLocationOnScreen(drawingView);
    float offsetX = -drawingViewBounds.left;
    float offsetY = -drawingViewBounds.top;
    RectF drawableBounds = calculateDrawableBounds(drawingView, boundingView, offsetX, offsetY);
    startBounds.offset(offsetX, offsetY);
    endBounds.offset(offsetX, offsetY);

    boolean entering = isEntering(startBounds, endBounds);

    final TransitionDrawable transitionDrawable =
        new TransitionDrawable(
            getPathMotion(),
            startView,
            startBounds,
            startShapeAppearanceModel,
            getElevationOrDefault(startElevation, startView),
            endView,
            endBounds,
            endShapeAppearanceModel,
            getElevationOrDefault(endElevation, endView),
            containerColor,
            startContainerColor,
            endContainerColor,
            scrimColor,
            entering,
            elevationShadowEnabled,
            FadeModeEvaluators.get(fadeMode, entering),
            FitModeEvaluators.get(fitMode, entering, startBounds, endBounds),
            buildThresholdsGroup(entering),
            drawDebugEnabled);

    
    transitionDrawable.setBounds(
        Math.round(drawableBounds.left),
        Math.round(drawableBounds.top),
        Math.round(drawableBounds.right),
        Math.round(drawableBounds.bottom));

    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            transitionDrawable.setProgress(animation.getAnimatedFraction());
          }
        });

    addListener(
        new TransitionListenerAdapter() {
          @Override
          public void onTransitionStart(@NonNull Transition transition) {
            
            ViewUtils.getOverlay(drawingView).add(transitionDrawable);

            
            startView.setAlpha(0);
            endView.setAlpha(0);
          }

          @Override
          public void onTransitionEnd(@NonNull Transition transition) {
            if (holdAtEndEnabled) {
              
              return;
            }
            
            startView.setAlpha(1);
            endView.setAlpha(1);

            
            ViewUtils.getOverlay(drawingView).remove(transitionDrawable);
          }
        });

    return animator;
  }

  private static float getElevationOrDefault(float elevation, View view) {
    return elevation != ELEVATION_NOT_SET ? elevation : ViewCompat.getElevation(view);
  }

  private static RectF calculateDrawableBounds(
      View drawingView, @Nullable View boundingView, float offsetX, float offsetY) {
    if (boundingView != null) {
      RectF drawableBounds = TransitionUtils.getLocationOnScreen(boundingView);
      drawableBounds.offset(offsetX, offsetY);
      return drawableBounds;
    } else {
      return new RectF(0, 0, drawingView.getWidth(), drawingView.getHeight());
    }
  }

  private boolean isEntering(@NonNull RectF startBounds, @NonNull RectF endBounds) {
    switch (transitionDirection) {
      case TRANSITION_DIRECTION_AUTO:
        return TransitionUtils.calculateArea(endBounds) > TransitionUtils.calculateArea(startBounds);
      case TRANSITION_DIRECTION_ENTER:
        return true;
      case TRANSITION_DIRECTION_RETURN:
        return false;
      default:
        throw new IllegalArgumentException("Invalid transition direction: " + transitionDirection);
    }
  }

  private ProgressThresholdsGroup buildThresholdsGroup(boolean entering) {
    PathMotion pathMotion = getPathMotion();
    if (pathMotion instanceof ArcMotion || pathMotion instanceof MaterialArcMotion) {
      return getThresholdsOrDefault(
          entering, DEFAULT_ENTER_THRESHOLDS_ARC, DEFAULT_RETURN_THRESHOLDS_ARC);
    } else {
      return getThresholdsOrDefault(entering, DEFAULT_ENTER_THRESHOLDS, DEFAULT_RETURN_THRESHOLDS);
    }
  }

  private ProgressThresholdsGroup getThresholdsOrDefault(
      boolean entering,
      ProgressThresholdsGroup defaultEnterThresholds,
      ProgressThresholdsGroup defaultReturnThresholds) {
    ProgressThresholdsGroup defaultThresholds =
        entering ? defaultEnterThresholds : defaultReturnThresholds;
    return new ProgressThresholdsGroup(
        TransitionUtils.defaultIfNull(fadeProgressThresholds, defaultThresholds.fade),
        TransitionUtils.defaultIfNull(scaleProgressThresholds, defaultThresholds.scale),
        TransitionUtils.defaultIfNull(scaleMaskProgressThresholds, defaultThresholds.scaleMask),
        TransitionUtils.defaultIfNull(shapeMaskProgressThresholds, defaultThresholds.shapeMask));
  }

  
  private static final class TransitionDrawable extends Drawable {

    
    private static final int SHADOW_COLOR = 0x2D000000;
    private static final int COMPAT_SHADOW_COLOR = 0xFF888888;
    private static final float COMPAT_SHADOW_OFFSET_MULTIPLIER = 0.75f;

    
    private final View startView;
    private final RectF startBounds;
    private final ShapeAppearanceModel startShapeAppearanceModel;
    private final float startElevation;

    
    private final View endView;
    private final RectF endBounds;
    private final ShapeAppearanceModel endShapeAppearanceModel;
    private final float endElevation;

    
    private final Paint containerPaint = new Paint();
    private final Paint startContainerPaint = new Paint();
    private final Paint endContainerPaint = new Paint();
    private final Paint shadowPaint = new Paint();
    private final Paint scrimPaint = new Paint();

    
    private final MaskEvaluator maskEvaluator = new MaskEvaluator();
    private final PathMeasure motionPathMeasure;
    private final float motionPathLength;
    private final float[] motionPathPosition = new float[2];

    
    private final boolean entering;
    private final boolean elevationShadowEnabled;
    private final MaterialShapeDrawable compatShadowDrawable = new MaterialShapeDrawable();
    private final RectF currentStartBounds;
    private final RectF currentStartBoundsMasked;
    private final RectF currentEndBounds;
    private final RectF currentEndBoundsMasked;
    private final ProgressThresholdsGroup progressThresholds;
    private final FadeModeEvaluator fadeModeEvaluator;
    private final FitModeEvaluator fitModeEvaluator;

    
    private final boolean drawDebugEnabled;
    private final Paint debugPaint = new Paint();
    private final Path debugPath = new Path();

    
    private FadeModeResult fadeModeResult;
    private FitModeResult fitModeResult;
    private RectF currentMaskBounds;
    private float currentElevation;
    private float progress;

    private TransitionDrawable(
        PathMotion pathMotion,
        View startView,
        RectF startBounds,
        ShapeAppearanceModel startShapeAppearanceModel,
        float startElevation,
        View endView,
        RectF endBounds,
        ShapeAppearanceModel endShapeAppearanceModel,
        float endElevation,
        @ColorInt int containerColor,
        @ColorInt int startContainerColor,
        @ColorInt int endContainerColor,
        int scrimColor,
        boolean entering,
        boolean elevationShadowEnabled,
        FadeModeEvaluator fadeModeEvaluator,
        FitModeEvaluator fitModeEvaluator,
        ProgressThresholdsGroup progressThresholds,
        boolean drawDebugEnabled) {
      this.startView = startView;
      this.startBounds = startBounds;
      this.startShapeAppearanceModel = startShapeAppearanceModel;
      this.startElevation = startElevation;
      this.endView = endView;
      this.endBounds = endBounds;
      this.endShapeAppearanceModel = endShapeAppearanceModel;
      this.endElevation = endElevation;
      this.entering = entering;
      this.elevationShadowEnabled = elevationShadowEnabled;
      this.fadeModeEvaluator = fadeModeEvaluator;
      this.fitModeEvaluator = fitModeEvaluator;
      this.progressThresholds = progressThresholds;
      this.drawDebugEnabled = drawDebugEnabled;

      containerPaint.setColor(containerColor);
      startContainerPaint.setColor(startContainerColor);
      endContainerPaint.setColor(endContainerColor);

      compatShadowDrawable.setFillColor(ColorStateList.valueOf(Color.TRANSPARENT));
      compatShadowDrawable.setShadowCompatibilityMode(
          MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
      compatShadowDrawable.setShadowBitmapDrawingEnable(false);
      compatShadowDrawable.setShadowColor(COMPAT_SHADOW_COLOR);

      currentStartBounds = new RectF(startBounds);
      currentStartBoundsMasked = new RectF(currentStartBounds);
      currentEndBounds = new RectF(currentStartBounds);
      currentEndBoundsMasked = new RectF(currentEndBounds);

      
      PointF startPoint = getMotionPathPoint(startBounds);
      PointF endPoint = getMotionPathPoint(endBounds);
      Path motionPath = pathMotion.getPath(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
      motionPathMeasure = new PathMeasure(motionPath, false);
      motionPathLength = motionPathMeasure.getLength();
      
      
      
      
      motionPathPosition[0] = startBounds.centerX();
      motionPathPosition[1] = startBounds.top;

      scrimPaint.setStyle(Paint.Style.FILL);
      scrimPaint.setShader(TransitionUtils.createColorShader(scrimColor));

      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setStrokeWidth(10);

      
      updateProgress(0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      if (scrimPaint.getAlpha() > 0) {
        canvas.drawRect(getBounds(), scrimPaint);
      }

      int debugCanvasSave = drawDebugEnabled ? canvas.save() : -1;

      if (elevationShadowEnabled && currentElevation > 0) {
        drawElevationShadow(canvas);
      }

      
      
      maskEvaluator.clip(canvas);

      maybeDrawContainerColor(canvas, containerPaint);

      if (fadeModeResult.endOnTop) {
        drawStartView(canvas);
        drawEndView(canvas);
      } else {
        drawEndView(canvas);
        drawStartView(canvas);
      }

      if (drawDebugEnabled) {
        canvas.restoreToCount(debugCanvasSave);
        drawDebugCumulativePath(canvas, currentStartBounds, debugPath, Color.MAGENTA);
        drawDebugRect(canvas, currentStartBoundsMasked, Color.YELLOW);
        drawDebugRect(canvas, currentStartBounds, Color.GREEN);
        drawDebugRect(canvas, currentEndBoundsMasked, Color.CYAN);
        drawDebugRect(canvas, currentEndBounds, Color.BLUE);
      }
    }

    
    private void drawElevationShadow(Canvas canvas) {
      canvas.save();
      canvas.clipPath(maskEvaluator.getPath(), Op.DIFFERENCE);

      if (VERSION.SDK_INT > VERSION_CODES.P) {
        drawElevationShadowWithPaintShadowLayer(canvas);
      } else {
        drawElevationShadowWithMaterialShapeDrawable(canvas);
      }

      canvas.restore();
    }

    private void drawElevationShadowWithPaintShadowLayer(Canvas canvas) {
      ShapeAppearanceModel currentShapeAppearanceModel =
          maskEvaluator.getCurrentShapeAppearanceModel();
      if (currentShapeAppearanceModel.isRoundRect(currentMaskBounds)) {
        
        float radius =
            currentShapeAppearanceModel.getTopLeftCornerSize().getCornerSize(currentMaskBounds);
        canvas.drawRoundRect(currentMaskBounds, radius, radius, shadowPaint);
      } else {
        
        canvas.drawPath(maskEvaluator.getPath(), shadowPaint);
      }
    }

    private void drawElevationShadowWithMaterialShapeDrawable(Canvas canvas) {
      compatShadowDrawable.setBounds(
          (int) currentMaskBounds.left,
          (int) currentMaskBounds.top,
          (int) currentMaskBounds.right,
          (int) currentMaskBounds.bottom);
      compatShadowDrawable.setElevation(currentElevation);
      compatShadowDrawable.setShadowVerticalOffset(
          (int) (currentElevation * COMPAT_SHADOW_OFFSET_MULTIPLIER));
      compatShadowDrawable.setShapeAppearanceModel(maskEvaluator.getCurrentShapeAppearanceModel());
      compatShadowDrawable.draw(canvas);
    }

    
    private void drawStartView(Canvas canvas) {
      maybeDrawContainerColor(canvas, startContainerPaint);
      TransitionUtils.transform(
          canvas,
          getBounds(),
          currentStartBounds.left,
          currentStartBounds.top,
          fitModeResult.startScale,
          fadeModeResult.startAlpha,
          new TransitionUtils.CanvasOperation() {
            @Override
            public void run(Canvas canvas) {
              startView.draw(canvas);
            }
          });
    }

    
    private void drawEndView(Canvas canvas) {
      maybeDrawContainerColor(canvas, endContainerPaint);
      TransitionUtils.transform(
          canvas,
          getBounds(),
          currentEndBounds.left,
          currentEndBounds.top,
          fitModeResult.endScale,
          fadeModeResult.endAlpha,
          new TransitionUtils.CanvasOperation() {
            @Override
            public void run(Canvas canvas) {
              endView.draw(canvas);
            }
          });
    }

    private void maybeDrawContainerColor(Canvas canvas, Paint containerPaint) {
      
      
      
      if (containerPaint.getColor() != Color.TRANSPARENT && containerPaint.getAlpha() > 0) {
        canvas.drawRect(getBounds(), containerPaint);
      }
    }

    @Override
    public void setAlpha(int alpha) {
      throw new UnsupportedOperationException("Setting alpha on is not supported");
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
      throw new UnsupportedOperationException("Setting a color filter is not supported");
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }

    private void setProgress(float progress) {
      if (this.progress != progress) {
        updateProgress(progress);
      }
    }

    private void updateProgress(float progress) {
      this.progress = progress;

      
      scrimPaint.setAlpha((int) (entering ? TransitionUtils.lerp(0, 255, progress) : TransitionUtils.lerp(255, 0, progress)));

      
      currentElevation = TransitionUtils.lerp(startElevation, endElevation, progress);
      shadowPaint.setShadowLayer(currentElevation, 0, currentElevation, SHADOW_COLOR);

      
      motionPathMeasure.getPosTan(motionPathLength * progress, motionPathPosition, null);
      float motionPathX = motionPathPosition[0];
      float motionPathY = motionPathPosition[1];

      
      float scaleStartFraction = checkNotNull(progressThresholds.scale.start);
      float scaleEndFraction = checkNotNull(progressThresholds.scale.end);
      fitModeResult =
          fitModeEvaluator.evaluate(
              progress,
              scaleStartFraction,
              scaleEndFraction,
              startBounds.width(),
              startBounds.height(),
              endBounds.width(),
              endBounds.height());
      currentStartBounds.set(
          motionPathX - fitModeResult.currentStartWidth / 2,
          motionPathY,
          motionPathX + fitModeResult.currentStartWidth / 2,
          motionPathY + fitModeResult.currentStartHeight);
      currentEndBounds.set(
          motionPathX - fitModeResult.currentEndWidth / 2,
          motionPathY,
          motionPathX + fitModeResult.currentEndWidth / 2,
          motionPathY + fitModeResult.currentEndHeight);

      
      currentStartBoundsMasked.set(currentStartBounds);
      currentEndBoundsMasked.set(currentEndBounds);
      float maskStartFraction = checkNotNull(progressThresholds.scaleMask.start);
      float maskEndFraction = checkNotNull(progressThresholds.scaleMask.end);
      boolean shouldMaskStartBounds = fitModeEvaluator.shouldMaskStartBounds(fitModeResult);
      RectF maskBounds = shouldMaskStartBounds ? currentStartBoundsMasked : currentEndBoundsMasked;
      float maskProgress = TransitionUtils.lerp(0f, 1f, maskStartFraction, maskEndFraction, progress);
      float maskMultiplier = shouldMaskStartBounds ? maskProgress : 1 - maskProgress;
      fitModeEvaluator.applyMask(maskBounds, maskMultiplier, fitModeResult);

      
      currentMaskBounds =
          new RectF(
              Math.min(currentStartBoundsMasked.left, currentEndBoundsMasked.left),
              Math.min(currentStartBoundsMasked.top, currentEndBoundsMasked.top),
              Math.max(currentStartBoundsMasked.right, currentEndBoundsMasked.right),
              Math.max(currentStartBoundsMasked.bottom, currentEndBoundsMasked.bottom));

      maskEvaluator.evaluate(
          progress,
          startShapeAppearanceModel,
          endShapeAppearanceModel,
          currentStartBounds,
          currentStartBoundsMasked,
          currentEndBoundsMasked,
          progressThresholds.shapeMask);

      
      float fadeStartFraction = checkNotNull(progressThresholds.fade.start);
      float fadeEndFraction = checkNotNull(progressThresholds.fade.end);
      fadeModeResult = fadeModeEvaluator.evaluate(progress, fadeStartFraction, fadeEndFraction);

      
      
      if (startContainerPaint.getColor() != Color.TRANSPARENT) {
        startContainerPaint.setAlpha(fadeModeResult.startAlpha);
      }
      if (endContainerPaint.getColor() != Color.TRANSPARENT) {
        endContainerPaint.setAlpha(fadeModeResult.endAlpha);
      }

      invalidateSelf();
    }

    private static PointF getMotionPathPoint(RectF bounds) {
      return new PointF(bounds.centerX(), bounds.top);
    }

    private void drawDebugCumulativePath(
        Canvas canvas, RectF bounds, Path path, @ColorInt int color) {
      PointF point = getMotionPathPoint(bounds);
      if (progress == 0) {
        path.reset();
        path.moveTo(point.x, point.y);
      } else {
        path.lineTo(point.x, point.y);
        debugPaint.setColor(color);
        canvas.drawPath(path, debugPaint);
      }
    }

    private void drawDebugRect(Canvas canvas, RectF bounds, @ColorInt int color) {
      debugPaint.setColor(color);
      canvas.drawRect(bounds, debugPaint);
    }
  }

  
  public static class ProgressThresholds {
    @FloatRange(from = 0.0, to = 1.0)
    private final float start;

    @FloatRange(from = 0.0, to = 1.0)
    private final float end;

    public ProgressThresholds(
        @FloatRange(from = 0.0, to = 1.0) float start,
        @FloatRange(from = 0.0, to = 1.0) float end) {
      this.start = start;
      this.end = end;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public float getStart() {
      return start;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public float getEnd() {
      return end;
    }
  }

  private static class ProgressThresholdsGroup {
    @NonNull private final ProgressThresholds fade;
    @NonNull private final ProgressThresholds scale;
    @NonNull private final ProgressThresholds scaleMask;
    @NonNull private final ProgressThresholds shapeMask;

    private ProgressThresholdsGroup(
        @NonNull ProgressThresholds fade,
        @NonNull ProgressThresholds scale,
        @NonNull ProgressThresholds scaleMask,
        @NonNull ProgressThresholds shapeMask) {
      this.fade = fade;
      this.scale = scale;
      this.scaleMask = scaleMask;
      this.shapeMask = shapeMask;
    }
  }
}
