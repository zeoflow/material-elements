

package com.zeoflow.material.elements.chip;

import com.google.android.material.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build.VERSION_CODES;
import androidx.annotation.AnimatorRes;
import androidx.annotation.AttrRes;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.TintAwareDrawable;
import androidx.core.text.BidiFormatter;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import com.zeoflow.material.elements.animation.MotionSpec;
import com.zeoflow.material.elements.canvas.CanvasCompat;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.drawable.DrawableUtils;
import com.zeoflow.material.elements.internal.TextDrawableHelper;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.resources.TextAppearance;
import com.zeoflow.material.elements.ripple.RippleUtils;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;

import java.lang.ref.WeakReference;
import java.util.Arrays;


public class ChipDrawable extends MaterialShapeDrawable
    implements TintAwareDrawable, Callback, TextDrawableHelper.TextDrawableDelegate
{

  private static final boolean DEBUG = false;
  private static final int[] DEFAULT_STATE = new int[] {android.R.attr.state_enabled};
  private static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";

  private static final ShapeDrawable closeIconRippleMask = new ShapeDrawable(new OvalShape());


  @Nullable private ColorStateList chipSurfaceColor;
  @Nullable private ColorStateList chipBackgroundColor;
  private float chipMinHeight;
  private float chipCornerRadius = -1;
  @Nullable private ColorStateList chipStrokeColor;
  private float chipStrokeWidth;
  @Nullable private ColorStateList rippleColor;


  @Nullable private CharSequence text;


  private boolean chipIconVisible;
  @Nullable private Drawable chipIcon;
  @Nullable private ColorStateList chipIconTint;
  private float chipIconSize;
  private boolean hasChipIconTint;


  private boolean closeIconVisible;
  @Nullable private Drawable closeIcon;
  @Nullable private Drawable closeIconRipple;
  @Nullable private ColorStateList closeIconTint;
  private float closeIconSize;
  @Nullable private CharSequence closeIconContentDescription;


  private boolean checkable;
  private boolean checkedIconVisible;
  @Nullable private Drawable checkedIcon;
  @Nullable private ColorStateList checkedIconTint;


  @Nullable private MotionSpec showMotionSpec;
  @Nullable private MotionSpec hideMotionSpec;






  private float chipStartPadding;

  private float iconStartPadding;




  private float iconEndPadding;

  private float textStartPadding;




  private float textEndPadding;

  private float closeIconStartPadding;




  private float closeIconEndPadding;

  private float chipEndPadding;



  @NonNull private final Context context;
  private final Paint chipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  @Nullable private final Paint debugPaint;
  private final FontMetrics fontMetrics = new FontMetrics();
  private final RectF rectF = new RectF();
  private final PointF pointF = new PointF();
  private final Path shapePath = new Path();
  @NonNull private final TextDrawableHelper textDrawableHelper;

  @ColorInt private int currentChipSurfaceColor;
  @ColorInt private int currentChipBackgroundColor;
  @ColorInt private int currentCompositeSurfaceBackgroundColor;
  @ColorInt private int currentChipStrokeColor;
  @ColorInt private int currentCompatRippleColor;
  @ColorInt private int currentTextColor;
  private boolean currentChecked;
  @ColorInt private int currentTint;

  private int alpha = 255;
  @Nullable private ColorFilter colorFilter;
  @Nullable private PorterDuffColorFilter tintFilter;
  @Nullable private ColorStateList tint;
  @Nullable private Mode tintMode = Mode.SRC_IN;
  private int[] closeIconStateSet;
  private boolean useCompatRipple;
  @Nullable private ColorStateList compatRippleColor;
  @NonNull private WeakReference<Delegate> delegate = new WeakReference<>(null);
  private TruncateAt truncateAt;
  private boolean shouldDrawText;
  private int maxWidth;
  private boolean isShapeThemingEnabled;


  @NonNull
  public static ChipDrawable createFromAttributes(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    ChipDrawable chip = new ChipDrawable(context, attrs, defStyleAttr, defStyleRes);
    chip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return chip;
  }


  @NonNull
  public static ChipDrawable createFromResource(@NonNull Context context, @XmlRes int id) {
    AttributeSet attrs = DrawableUtils.parseDrawableXml(context, id, "chip");
    @StyleRes int style = attrs.getStyleAttribute();
    if (style == 0) {
      style = R.style.Widget_MaterialComponents_Chip_Entry;
    }
    return createFromAttributes(context, attrs, R.attr.chipStandaloneStyle, style);
  }

  private ChipDrawable(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initializeElevationOverlay(context);

    this.context = context;
    textDrawableHelper = new TextDrawableHelper( this);

    text = "";

    textDrawableHelper.getTextPaint().density = context.getResources().getDisplayMetrics().density;
    debugPaint = DEBUG ? new Paint(Paint.ANTI_ALIAS_FLAG) : null;
    if (debugPaint != null) {
      debugPaint.setStyle(Style.STROKE);
    }

    setState(DEFAULT_STATE);
    setCloseIconState(DEFAULT_STATE);
    shouldDrawText = true;

    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {

      closeIconRippleMask.setTint(Color.WHITE);
    }
  }

  private void loadFromAttributes(
      @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Chip, defStyleAttr, defStyleRes);

    isShapeThemingEnabled = a.hasValue(R.styleable.Chip_shapeAppearance);
    setChipSurfaceColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipSurfaceColor));
    setChipBackgroundColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipBackgroundColor));
    setChipMinHeight(a.getDimension(R.styleable.Chip_chipMinHeight, 0f));
    if (a.hasValue(R.styleable.Chip_chipCornerRadius)) {
      setChipCornerRadius(a.getDimension(R.styleable.Chip_chipCornerRadius, 0f));
    }
    setChipStrokeColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipStrokeColor));
    setChipStrokeWidth(a.getDimension(R.styleable.Chip_chipStrokeWidth, 0f));
    setRippleColor(MaterialResources.getColorStateList(context, a, R.styleable.Chip_rippleColor));

    setText(a.getText(R.styleable.Chip_android_text));
    setTextAppearance(
        MaterialResources.getTextAppearance(context, a, R.styleable.Chip_android_textAppearance));

    int ellipsize = a.getInt(R.styleable.Chip_android_ellipsize, 0);

    switch (ellipsize) {
      case 1:
        setEllipsize(TextUtils.TruncateAt.START);
        break;
      case 2:
        setEllipsize(TextUtils.TruncateAt.MIDDLE);
        break;
      case 3:
        setEllipsize(TextUtils.TruncateAt.END);
        break;
      case 4:

      default:
        break;
    }

    setChipIconVisible(a.getBoolean(R.styleable.Chip_chipIconVisible, false));


    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "chipIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "chipIconVisible") == null) {
      setChipIconVisible(a.getBoolean(R.styleable.Chip_chipIconEnabled, false));
    }
    setChipIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_chipIcon));
    if (a.hasValue(R.styleable.Chip_chipIconTint)) {
      setChipIconTint(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_chipIconTint));
    }
    setChipIconSize(a.getDimension(R.styleable.Chip_chipIconSize, 0f));

    setCloseIconVisible(a.getBoolean(R.styleable.Chip_closeIconVisible, false));



    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "closeIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "closeIconVisible") == null) {
      setCloseIconVisible(a.getBoolean(R.styleable.Chip_closeIconEnabled, false));
    }
    setCloseIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_closeIcon));
    setCloseIconTint(
        MaterialResources.getColorStateList(context, a, R.styleable.Chip_closeIconTint));
    setCloseIconSize(a.getDimension(R.styleable.Chip_closeIconSize, 0f));

    setCheckable(a.getBoolean(R.styleable.Chip_android_checkable, false));
    setCheckedIconVisible(a.getBoolean(R.styleable.Chip_checkedIconVisible, false));



    if (attrs != null
        && attrs.getAttributeValue(NAMESPACE_APP, "checkedIconEnabled") != null
        && attrs.getAttributeValue(NAMESPACE_APP, "checkedIconVisible") == null) {
      setCheckedIconVisible(a.getBoolean(R.styleable.Chip_checkedIconEnabled, false));
    }
    setCheckedIcon(MaterialResources.getDrawable(context, a, R.styleable.Chip_checkedIcon));
    if (a.hasValue(R.styleable.Chip_checkedIconTint)) {
      setCheckedIconTint(
          MaterialResources.getColorStateList(context, a, R.styleable.Chip_checkedIconTint));
    }

    setShowMotionSpec(MotionSpec.createFromAttribute(context, a, R.styleable.Chip_showMotionSpec));
    setHideMotionSpec(MotionSpec.createFromAttribute(context, a, R.styleable.Chip_hideMotionSpec));

    setChipStartPadding(a.getDimension(R.styleable.Chip_chipStartPadding, 0f));
    setIconStartPadding(a.getDimension(R.styleable.Chip_iconStartPadding, 0f));
    setIconEndPadding(a.getDimension(R.styleable.Chip_iconEndPadding, 0f));
    setTextStartPadding(a.getDimension(R.styleable.Chip_textStartPadding, 0f));
    setTextEndPadding(a.getDimension(R.styleable.Chip_textEndPadding, 0f));
    setCloseIconStartPadding(a.getDimension(R.styleable.Chip_closeIconStartPadding, 0f));
    setCloseIconEndPadding(a.getDimension(R.styleable.Chip_closeIconEndPadding, 0f));
    setChipEndPadding(a.getDimension(R.styleable.Chip_chipEndPadding, 0f));

    setMaxWidth(a.getDimensionPixelSize(R.styleable.Chip_android_maxWidth, Integer.MAX_VALUE));

    a.recycle();
  }


  public void setUseCompatRipple(boolean useCompatRipple) {
    if (this.useCompatRipple != useCompatRipple) {
      this.useCompatRipple = useCompatRipple;
      updateCompatRippleColor();
      onStateChange(getState());
    }
  }


  public boolean getUseCompatRipple() {
    return useCompatRipple;
  }


  public void setDelegate(@Nullable Delegate delegate) {
    this.delegate = new WeakReference<>(delegate);
  }


  protected void onSizeChange() {
    Delegate delegate = this.delegate.get();
    if (delegate != null) {
      delegate.onChipDrawableSizeChange();
    }
  }


  public void getChipTouchBounds(@NonNull RectF bounds) {
    calculateChipTouchBounds(getBounds(), bounds);
  }


  public void getCloseIconTouchBounds(@NonNull RectF bounds) {
    calculateCloseIconTouchBounds(getBounds(), bounds);
  }


  @Override
  public int getIntrinsicWidth() {
    int calculatedWidth =
        Math.round(
            (chipStartPadding
                + calculateChipIconWidth()
                + textStartPadding
                + textDrawableHelper.getTextWidth(getText().toString())
                + textEndPadding
                + calculateCloseIconWidth()
                + chipEndPadding));
    return Math.min(calculatedWidth, maxWidth);
  }


  @Override
  public int getIntrinsicHeight() {
    return (int) chipMinHeight;
  }


  private boolean showsChipIcon() {
    return chipIconVisible && chipIcon != null;
  }


  private boolean showsCheckedIcon() {
    return checkedIconVisible && checkedIcon != null && currentChecked;
  }


  private boolean showsCloseIcon() {
    return closeIconVisible && closeIcon != null;
  }


  private boolean canShowCheckedIcon() {
    return checkedIconVisible && checkedIcon != null && checkable;
  }


  float calculateChipIconWidth() {
    if (showsChipIcon() || (showsCheckedIcon())) {
      return iconStartPadding + chipIconSize + iconEndPadding;
    }
    return 0f;
  }


  float calculateCloseIconWidth() {
    if (showsCloseIcon()) {
      return closeIconStartPadding + closeIconSize + closeIconEndPadding;
    }
    return 0f;
  }

  boolean isShapeThemingEnabled() {
    return isShapeThemingEnabled;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0) {
      return;
    }

    int saveCount = 0;
    if (alpha < 255) {
      saveCount =
          CanvasCompat.saveLayerAlpha(
              canvas, bounds.left, bounds.top, bounds.right, bounds.bottom, alpha);
    }


    drawChipSurface(canvas, bounds);


    drawChipBackground(canvas, bounds);

    if (isShapeThemingEnabled) {
      super.draw(canvas);
    }

    drawChipStroke(canvas, bounds);


    drawCompatRipple(canvas, bounds);


    drawChipIcon(canvas, bounds);


    drawCheckedIcon(canvas, bounds);


    if (shouldDrawText) {
      drawText(canvas, bounds);
    }


    drawCloseIcon(canvas, bounds);


    drawDebug(canvas, bounds);

    if (alpha < 255) {
      canvas.restoreToCount(saveCount);
    }
  }

  private void drawChipSurface(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (!isShapeThemingEnabled) {
      chipPaint.setColor(currentChipSurfaceColor);
      chipPaint.setStyle(Style.FILL);
      rectF.set(bounds);
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    }
  }

  private void drawChipBackground(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (!isShapeThemingEnabled) {
      chipPaint.setColor(currentChipBackgroundColor);
      chipPaint.setStyle(Style.FILL);
      chipPaint.setColorFilter(getTintColorFilter());
      rectF.set(bounds);
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    }
  }


  private void drawChipStroke(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (chipStrokeWidth > 0 && !isShapeThemingEnabled) {
      chipPaint.setColor(currentChipStrokeColor);
      chipPaint.setStyle(Style.STROKE);
      if (!isShapeThemingEnabled) {
        chipPaint.setColorFilter(getTintColorFilter());
      }
      rectF.set(
          bounds.left + chipStrokeWidth / 2f,
          bounds.top + chipStrokeWidth / 2f,
          bounds.right - chipStrokeWidth / 2f,
          bounds.bottom - chipStrokeWidth / 2f);


      float strokeCornerRadius = chipCornerRadius - chipStrokeWidth / 2f;
      canvas.drawRoundRect(rectF, strokeCornerRadius, strokeCornerRadius, chipPaint);
    }
  }

  private void drawCompatRipple(@NonNull Canvas canvas, @NonNull Rect bounds) {
    chipPaint.setColor(currentCompatRippleColor);
    chipPaint.setStyle(Style.FILL);
    rectF.set(bounds);
    if (!isShapeThemingEnabled) {
      canvas.drawRoundRect(rectF, getChipCornerRadius(), getChipCornerRadius(), chipPaint);
    } else {
      calculatePathForSize(new RectF(bounds), shapePath);
      super.drawShape(canvas, chipPaint, shapePath, getBoundsAsRectF());
    }
  }

  private void drawChipIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsChipIcon()) {
      calculateChipIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      chipIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());
      chipIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }

  private void drawCheckedIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsCheckedIcon()) {
      calculateChipIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      checkedIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());
      checkedIcon.draw(canvas);

      canvas.translate(-tx, -ty);
    }
  }


  private void drawText(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (text != null) {
      Align align = calculateTextOriginAndAlignment(bounds, pointF);


      calculateTextBounds(bounds, rectF);

      if (textDrawableHelper.getTextAppearance() != null) {
        textDrawableHelper.getTextPaint().drawableState = getState();
        textDrawableHelper.updateTextPaintDrawState(context);
      }
      textDrawableHelper.getTextPaint().setTextAlign(align);

      boolean clip =
          Math.round(textDrawableHelper.getTextWidth(getText().toString()))
              > Math.round(rectF.width());
      int saveCount = 0;
      if (clip) {
        saveCount = canvas.save();
        canvas.clipRect(rectF);
      }

      CharSequence finalText = text;
      if (clip && truncateAt != null) {
        finalText =
            TextUtils.ellipsize(text, textDrawableHelper.getTextPaint(), rectF.width(), truncateAt);
      }
      canvas.drawText(
          finalText, 0, finalText.length(), pointF.x, pointF.y, textDrawableHelper.getTextPaint());
      if (clip) {
        canvas.restoreToCount(saveCount);
      }
    }
  }

  private void drawCloseIcon(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (showsCloseIcon()) {
      calculateCloseIconBounds(bounds, rectF);
      float tx = rectF.left;
      float ty = rectF.top;

      canvas.translate(tx, ty);

      closeIcon.setBounds(0, 0, (int) rectF.width(), (int) rectF.height());

      if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
        closeIconRipple.setBounds(closeIcon.getBounds());
        closeIconRipple.jumpToCurrentState();
        closeIconRipple.draw(canvas);
      } else {
        closeIcon.draw(canvas);
      }

      canvas.translate(-tx, -ty);
    }
  }

  private void drawDebug(@NonNull Canvas canvas, @NonNull Rect bounds) {
    if (debugPaint != null) {
      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 255 / 2));


      canvas.drawRect(bounds, debugPaint);


      if (showsChipIcon() || (showsCheckedIcon())) {
        calculateChipIconBounds(bounds, rectF);
        canvas.drawRect(rectF, debugPaint);
      }


      if (text != null) {
        canvas.drawLine(
            bounds.left, bounds.exactCenterY(), bounds.right, bounds.exactCenterY(), debugPaint);
      }


      if (showsCloseIcon()) {
        calculateCloseIconBounds(bounds, rectF);
        canvas.drawRect(rectF, debugPaint);
      }


      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.RED, 255 / 2));
      calculateChipTouchBounds(bounds, rectF);
      canvas.drawRect(rectF, debugPaint);


      debugPaint.setColor(ColorUtils.setAlphaComponent(Color.GREEN, 255 / 2));
      calculateCloseIconTouchBounds(bounds, rectF);
      canvas.drawRect(rectF, debugPaint);
    }
  }


  private void calculateChipIconBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsChipIcon() || showsCheckedIcon()) {
      float offsetFromStart = chipStartPadding + iconStartPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = outBounds.left + chipIconSize;
      } else {
        outBounds.right = bounds.right - offsetFromStart;
        outBounds.left = outBounds.right - chipIconSize;
      }

      outBounds.top = bounds.exactCenterY() - chipIconSize / 2f;
      outBounds.bottom = outBounds.top + chipIconSize;
    }
  }


  @NonNull
  Align calculateTextOriginAndAlignment(@NonNull Rect bounds, @NonNull PointF pointF) {
    pointF.set(0, 0);
    Align align = Align.LEFT;

    if (text != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        pointF.x = bounds.left + offsetFromStart;
        align = Align.LEFT;
      } else {
        pointF.x = bounds.right - offsetFromStart;
        align = Align.RIGHT;
      }

      pointF.y = bounds.centerY() - calculateTextCenterFromBaseline();
    }

    return align;
  }


  private float calculateTextCenterFromBaseline() {
    textDrawableHelper.getTextPaint().getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }


  private void calculateTextBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (text != null) {
      float offsetFromStart = chipStartPadding + calculateChipIconWidth() + textStartPadding;
      float offsetFromEnd = chipEndPadding + calculateCloseIconWidth() + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.left = bounds.left + offsetFromStart;
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
        outBounds.right = bounds.right - offsetFromStart;
      }



      outBounds.top = bounds.top;
      outBounds.bottom = bounds.bottom;
    }
  }


  private void calculateCloseIconBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd = chipEndPadding + closeIconEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right - offsetFromEnd;
        outBounds.left = outBounds.right - closeIconSize;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
        outBounds.right = outBounds.left + closeIconSize;
      }

      outBounds.top = bounds.exactCenterY() - closeIconSize / 2f;
      outBounds.bottom = outBounds.top + closeIconSize;
    }
  }

  private void calculateChipTouchBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.set(bounds);

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left + offsetFromEnd;
      }
    }
  }

  private void calculateCloseIconTouchBounds(@NonNull Rect bounds, @NonNull RectF outBounds) {
    outBounds.setEmpty();

    if (showsCloseIcon()) {
      float offsetFromEnd =
          chipEndPadding
              + closeIconEndPadding
              + closeIconSize
              + closeIconStartPadding
              + textEndPadding;

      if (DrawableCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) {
        outBounds.right = bounds.right;
        outBounds.left = outBounds.right - offsetFromEnd;
      } else {
        outBounds.left = bounds.left;
        outBounds.right = bounds.left + offsetFromEnd;
      }

      outBounds.top = bounds.top;
      outBounds.bottom = bounds.bottom;
    }
  }


  @Override
  public boolean isStateful() {

    return isStateful(chipSurfaceColor)
        || isStateful(chipBackgroundColor)
        || isStateful(chipStrokeColor)
        || (useCompatRipple && isStateful(compatRippleColor))
        || isStateful(textDrawableHelper.getTextAppearance())
        || canShowCheckedIcon()
        || isStateful(chipIcon)
        || isStateful(checkedIcon)
        || isStateful(tint);
  }


  public boolean isCloseIconStateful() {

    return isStateful(closeIcon);
  }


  public boolean setCloseIconState(@NonNull int[] stateSet) {
    if (!Arrays.equals(closeIconStateSet, stateSet)) {
      closeIconStateSet = stateSet;
      if (showsCloseIcon()) {
        return onStateChange(getState(), stateSet);
      }
    }
    return false;
  }


  @NonNull
  public int[] getCloseIconState() {
    return closeIconStateSet;
  }

  @Override
  public void onTextSizeChange() {
    onSizeChange();
    invalidateSelf();
  }

  @Override
  public boolean onStateChange(@NonNull int[] state) {
    if (isShapeThemingEnabled) {
      super.onStateChange(state);
    }
    return onStateChange(state, getCloseIconState());
  }


  private boolean onStateChange(@NonNull int[] chipState, @NonNull int[] closeIconState) {

    boolean invalidate = super.onStateChange(chipState);
    boolean sizeChanged = false;

    int newChipSurfaceColor =
        chipSurfaceColor != null
            ? chipSurfaceColor.getColorForState(chipState, currentChipSurfaceColor)
            : 0;
    if (currentChipSurfaceColor != newChipSurfaceColor) {
      currentChipSurfaceColor = newChipSurfaceColor;
      invalidate = true;
    }

    int newChipBackgroundColor =
        chipBackgroundColor != null
            ? chipBackgroundColor.getColorForState(chipState, currentChipBackgroundColor)
            : 0;
    if (currentChipBackgroundColor != newChipBackgroundColor) {
      currentChipBackgroundColor = newChipBackgroundColor;
      invalidate = true;
    }

    int newCompositeSurfaceBackgroundColor =
        MaterialColors.layer(newChipSurfaceColor, newChipBackgroundColor);
    boolean shouldUpdate =
        currentCompositeSurfaceBackgroundColor != newCompositeSurfaceBackgroundColor;
    shouldUpdate |= getFillColor() == null;
    if (shouldUpdate) {
      currentCompositeSurfaceBackgroundColor = newCompositeSurfaceBackgroundColor;
      setFillColor(ColorStateList.valueOf(currentCompositeSurfaceBackgroundColor));
      invalidate = true;
    }

    int newChipStrokeColor =
        chipStrokeColor != null
            ? chipStrokeColor.getColorForState(chipState, currentChipStrokeColor)
            : 0;
    if (currentChipStrokeColor != newChipStrokeColor) {
      currentChipStrokeColor = newChipStrokeColor;
      invalidate = true;
    }

    int newCompatRippleColor =
        compatRippleColor != null && RippleUtils.shouldDrawRippleCompat(chipState)
            ? compatRippleColor.getColorForState(chipState, currentCompatRippleColor)
            : 0;
    if (currentCompatRippleColor != newCompatRippleColor) {
      currentCompatRippleColor = newCompatRippleColor;
      if (useCompatRipple) {
        invalidate = true;
      }
    }

    int newTextColor =
        textDrawableHelper.getTextAppearance() != null
                && textDrawableHelper.getTextAppearance().textColor != null
            ? textDrawableHelper
                .getTextAppearance()
                .textColor
                .getColorForState(chipState, currentTextColor)
            : 0;
    if (currentTextColor != newTextColor) {
      currentTextColor = newTextColor;
      invalidate = true;
    }

    boolean newChecked = hasState(getState(), android.R.attr.state_checked) && checkable;
    if (currentChecked != newChecked && checkedIcon != null) {
      float oldChipIconWidth = calculateChipIconWidth();
      currentChecked = newChecked;
      float newChipIconWidth = calculateChipIconWidth();
      invalidate = true;

      if (oldChipIconWidth != newChipIconWidth) {
        sizeChanged = true;
      }
    }

    int newTint = tint != null ? tint.getColorForState(chipState, currentTint) : 0;
    if (currentTint != newTint) {
      currentTint = newTint;
      tintFilter = DrawableUtils.updateTintFilter(this, tint, tintMode);
      invalidate = true;
    }

    if (isStateful(chipIcon)) {
      invalidate |= chipIcon.setState(chipState);
    }
    if (isStateful(checkedIcon)) {
      invalidate |= checkedIcon.setState(chipState);
    }
    if (isStateful(closeIcon)) {




      int[] closeIconMergedState = new int[chipState.length + closeIconState.length];
      System.arraycopy(chipState, 0, closeIconMergedState, 0, chipState.length);
      System.arraycopy(
          closeIconState, 0, closeIconMergedState, chipState.length, closeIconState.length);
      invalidate |= closeIcon.setState(closeIconMergedState);
    }

    if (RippleUtils.USE_FRAMEWORK_RIPPLE && isStateful(closeIconRipple)) {
      invalidate |= closeIconRipple.setState(closeIconState);
    }

    if (invalidate) {
      invalidateSelf();
    }
    if (sizeChanged) {
      onSizeChange();
    }
    return invalidate;
  }

  private static boolean isStateful(@Nullable ColorStateList colorStateList) {
    return colorStateList != null && colorStateList.isStateful();
  }

  private static boolean isStateful(@Nullable Drawable drawable) {
    return drawable != null && drawable.isStateful();
  }

  private static boolean isStateful(@Nullable TextAppearance textAppearance) {
    return textAppearance != null
        && textAppearance.textColor != null
        && textAppearance.textColor.isStateful();
  }

  @Override
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    boolean invalidate = super.onLayoutDirectionChanged(layoutDirection);

    if (showsChipIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(chipIcon, layoutDirection);
    }
    if (showsCheckedIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(checkedIcon, layoutDirection);
    }
    if (showsCloseIcon()) {
      invalidate |= DrawableCompat.setLayoutDirection(closeIcon, layoutDirection);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return true;
  }

  @Override
  protected boolean onLevelChange(int level) {
    boolean invalidate = super.onLevelChange(level);

    if (showsChipIcon()) {
      invalidate |= chipIcon.setLevel(level);
    }
    if (showsCheckedIcon()) {
      invalidate |= checkedIcon.setLevel(level);
    }
    if (showsCloseIcon()) {
      invalidate |= closeIcon.setLevel(level);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return invalidate;
  }

  @Override
  public boolean setVisible(boolean visible, boolean restart) {
    boolean invalidate = super.setVisible(visible, restart);

    if (showsChipIcon()) {
      invalidate |= chipIcon.setVisible(visible, restart);
    }
    if (showsCheckedIcon()) {
      invalidate |= checkedIcon.setVisible(visible, restart);
    }
    if (showsCloseIcon()) {
      invalidate |= closeIcon.setVisible(visible, restart);
    }

    if (invalidate) {
      invalidateSelf();
    }
    return invalidate;
  }


  @Override
  public void setAlpha(int alpha) {
    if (this.alpha != alpha) {
      this.alpha = alpha;
      invalidateSelf();
    }
  }

  @Override
  public int getAlpha() {
    return alpha;
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    if (this.colorFilter != colorFilter) {
      this.colorFilter = colorFilter;
      invalidateSelf();
    }
  }

  @Nullable
  @Override
  public ColorFilter getColorFilter() {
    return colorFilter;
  }

  @Override
  public void setTintList(@Nullable ColorStateList tint) {
    if (this.tint != tint) {
      this.tint = tint;
      onStateChange(getState());
    }
  }

  @Override
  public void setTintMode(@NonNull Mode tintMode) {
    if (this.tintMode != tintMode) {
      this.tintMode = tintMode;
      tintFilter = DrawableUtils.updateTintFilter(this, tint, tintMode);
      invalidateSelf();
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  @TargetApi(VERSION_CODES.LOLLIPOP)
  public void getOutline(@NonNull Outline outline) {
    if (isShapeThemingEnabled) {
      super.getOutline(outline);
      return;
    }
    Rect bounds = getBounds();
    if (!bounds.isEmpty()) {
      outline.setRoundRect(bounds, chipCornerRadius);
    } else {
      outline.setRoundRect(0, 0, getIntrinsicWidth(), getIntrinsicHeight(), chipCornerRadius);
    }

    outline.setAlpha(getAlpha() / 255f);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.invalidateDrawable(this);
    }
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.scheduleDrawable(this, what, when);
    }
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    Callback callback = getCallback();
    if (callback != null) {
      callback.unscheduleDrawable(this, what);
    }
  }

  private void unapplyChildDrawable(@Nullable Drawable drawable) {
    if (drawable != null) {
      drawable.setCallback(null);
    }
  }


  private void applyChildDrawable(@Nullable Drawable drawable) {
    if (drawable == null) {
      return;
    }
    drawable.setCallback(this);
    DrawableCompat.setLayoutDirection(drawable, DrawableCompat.getLayoutDirection(this));
    drawable.setLevel(getLevel());
    drawable.setVisible(isVisible(), false);

    if (drawable == closeIcon) {
      if (drawable.isStateful()) {
        drawable.setState(getCloseIconState());
      }
      DrawableCompat.setTintList(drawable, closeIconTint);
      return;
    }
    if (drawable.isStateful()) {
      drawable.setState(getState());
    }
    if (drawable == chipIcon && hasChipIconTint) {
      DrawableCompat.setTintList(chipIcon, chipIconTint);
    }
  }


  @Nullable
  private ColorFilter getTintColorFilter() {
    return colorFilter != null ? colorFilter : tintFilter;
  }

  private void updateCompatRippleColor() {
    compatRippleColor =
        useCompatRipple ? RippleUtils.sanitizeRippleDrawableColor(rippleColor) : null;
  }

  private void setChipSurfaceColor(@Nullable ColorStateList chipSurfaceColor) {
    if (this.chipSurfaceColor != chipSurfaceColor) {
      this.chipSurfaceColor = chipSurfaceColor;
      onStateChange(getState());
    }
  }


  private static boolean hasState(@Nullable int[] stateSet, @AttrRes int state) {
    if (stateSet == null) {
      return false;
    }

    for (int s : stateSet) {
      if (s == state) {
        return true;
      }
    }
    return false;
  }


  public interface Delegate {


    void onChipDrawableSizeChange();
  }




  @Nullable
  public ColorStateList getChipBackgroundColor() {
    return chipBackgroundColor;
  }


  public void setChipBackgroundColorResource(@ColorRes int id) {
    setChipBackgroundColor(AppCompatResources.getColorStateList(context, id));
  }


  public void setChipBackgroundColor(@Nullable ColorStateList chipBackgroundColor) {
    if (this.chipBackgroundColor != chipBackgroundColor) {
      this.chipBackgroundColor = chipBackgroundColor;
      onStateChange(getState());
    }
  }


  public float getChipMinHeight() {
    return chipMinHeight;
  }


  public void setChipMinHeightResource(@DimenRes int id) {
    setChipMinHeight(context.getResources().getDimension(id));
  }


  public void setChipMinHeight(float chipMinHeight) {
    if (this.chipMinHeight != chipMinHeight) {
      this.chipMinHeight = chipMinHeight;
      invalidateSelf();
      onSizeChange();
    }
  }


  public float getChipCornerRadius() {
    return isShapeThemingEnabled ? getTopLeftCornerResolvedSize() : chipCornerRadius;
  }


  @Deprecated
  public void setChipCornerRadiusResource(@DimenRes int id) {
    setChipCornerRadius(context.getResources().getDimension(id));
  }


  @Deprecated
  public void setChipCornerRadius(float chipCornerRadius) {
    if (this.chipCornerRadius != chipCornerRadius) {
      this.chipCornerRadius = chipCornerRadius;

      setShapeAppearanceModel(getShapeAppearanceModel().withCornerSize(chipCornerRadius));
    }
  }


  @Nullable
  public ColorStateList getChipStrokeColor() {
    return chipStrokeColor;
  }


  public void setChipStrokeColorResource(@ColorRes int id) {
    setChipStrokeColor(AppCompatResources.getColorStateList(context, id));
  }


  public void setChipStrokeColor(@Nullable ColorStateList chipStrokeColor) {
    if (this.chipStrokeColor != chipStrokeColor) {
      this.chipStrokeColor = chipStrokeColor;
      if (isShapeThemingEnabled) {
        setStrokeColor(chipStrokeColor);
      }
      onStateChange(getState());
    }
  }


  public float getChipStrokeWidth() {
    return chipStrokeWidth;
  }


  public void setChipStrokeWidthResource(@DimenRes int id) {
    setChipStrokeWidth(context.getResources().getDimension(id));
  }


  public void setChipStrokeWidth(float chipStrokeWidth) {
    if (this.chipStrokeWidth != chipStrokeWidth) {
      this.chipStrokeWidth = chipStrokeWidth;

      chipPaint.setStrokeWidth(chipStrokeWidth);
      if (isShapeThemingEnabled) {
        super.setStrokeWidth(chipStrokeWidth);
      }
      invalidateSelf();
    }
  }


  @Nullable
  public ColorStateList getRippleColor() {
    return rippleColor;
  }


  public void setRippleColorResource(@ColorRes int id) {
    setRippleColor(AppCompatResources.getColorStateList(context, id));
  }


  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (this.rippleColor != rippleColor) {
      this.rippleColor = rippleColor;
      updateCompatRippleColor();
      onStateChange(getState());
    }
  }

  @Nullable
  public CharSequence getText() {
    return text;
  }

  public void setTextResource(@StringRes int id) {
    setText(context.getResources().getString(id));
  }

  public void setText(@Nullable CharSequence text) {
    if (text == null) {
      text = "";
    }
    if (!TextUtils.equals(this.text, text)) {
      this.text = text;
      textDrawableHelper.setTextWidthDirty(true);
      invalidateSelf();
      onSizeChange();
    }
  }

  @Nullable
  public TextAppearance getTextAppearance() {
    return textDrawableHelper.getTextAppearance();
  }

  public void setTextAppearanceResource(@StyleRes int id) {
    setTextAppearance(new TextAppearance(context, id));
  }

  public void setTextAppearance(@Nullable TextAppearance textAppearance) {
    textDrawableHelper.setTextAppearance(textAppearance, context);
  }

  public TruncateAt getEllipsize() {
    return truncateAt;
  }

  public void setEllipsize(@Nullable TruncateAt truncateAt) {
    this.truncateAt = truncateAt;
  }

  public boolean isChipIconVisible() {
    return chipIconVisible;
  }


  @Deprecated
  public boolean isChipIconEnabled() {
    return isChipIconVisible();
  }

  public void setChipIconVisible(@BoolRes int id) {
    setChipIconVisible(context.getResources().getBoolean(id));
  }

  public void setChipIconVisible(boolean chipIconVisible) {
    if (this.chipIconVisible != chipIconVisible) {
      boolean oldShowsChipIcon = showsChipIcon();
      this.chipIconVisible = chipIconVisible;
      boolean newShowsChipIcon = showsChipIcon();

      boolean changed = oldShowsChipIcon != newShowsChipIcon;
      if (changed) {
        if (newShowsChipIcon) {
          applyChildDrawable(chipIcon);
        } else {
          unapplyChildDrawable(chipIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }


  @Deprecated
  public void setChipIconEnabledResource(@BoolRes int id) {
    setChipIconVisible(id);
  }


  @Deprecated
  public void setChipIconEnabled(boolean chipIconEnabled) {
    setChipIconVisible(chipIconEnabled);
  }

  @Nullable
  public Drawable getChipIcon() {
    return chipIcon != null ? DrawableCompat.unwrap(chipIcon) : null;
  }

  public void setChipIconResource(@DrawableRes int id) {
    setChipIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setChipIcon(@Nullable Drawable chipIcon) {
    Drawable oldChipIcon = getChipIcon();
    if (oldChipIcon != chipIcon) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.chipIcon = chipIcon != null ? DrawableCompat.wrap(chipIcon).mutate() : null;
      float newChipIconWidth = calculateChipIconWidth();

      unapplyChildDrawable(oldChipIcon);
      if (showsChipIcon()) {
        applyChildDrawable(this.chipIcon);
      }

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }


  @Nullable
  public ColorStateList getChipIconTint() {
    return chipIconTint;
  }


  public void setChipIconTintResource(@ColorRes int id) {
    setChipIconTint(AppCompatResources.getColorStateList(context, id));
  }


  public void setChipIconTint(@Nullable ColorStateList chipIconTint) {
    hasChipIconTint = true;
    if (this.chipIconTint != chipIconTint) {
      this.chipIconTint = chipIconTint;
      if (showsChipIcon()) {
        DrawableCompat.setTintList(chipIcon, chipIconTint);
      }

      onStateChange(getState());
    }
  }

  public float getChipIconSize() {
    return chipIconSize;
  }

  public void setChipIconSizeResource(@DimenRes int id) {
    setChipIconSize(context.getResources().getDimension(id));
  }

  public void setChipIconSize(float chipIconSize) {
    if (this.chipIconSize != chipIconSize) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.chipIconSize = chipIconSize;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  public boolean isCloseIconVisible() {
    return closeIconVisible;
  }


  @Deprecated
  public boolean isCloseIconEnabled() {
    return isCloseIconVisible();
  }

  public void setCloseIconVisible(@BoolRes int id) {
    setCloseIconVisible(context.getResources().getBoolean(id));
  }

  public void setCloseIconVisible(boolean closeIconVisible) {
    if (this.closeIconVisible != closeIconVisible) {
      boolean oldShowsCloseIcon = showsCloseIcon();
      this.closeIconVisible = closeIconVisible;
      boolean newShowsCloseIcon = showsCloseIcon();

      boolean changed = oldShowsCloseIcon != newShowsCloseIcon;
      if (changed) {
        if (newShowsCloseIcon) {
          applyChildDrawable(closeIcon);
        } else {
          unapplyChildDrawable(closeIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }


  @Deprecated
  public void setCloseIconEnabledResource(@BoolRes int id) {
    setCloseIconVisible(id);
  }


  @Deprecated
  public void setCloseIconEnabled(boolean closeIconEnabled) {
    setCloseIconVisible(closeIconEnabled);
  }

  @Nullable
  public Drawable getCloseIcon() {
    return closeIcon != null ? DrawableCompat.unwrap(closeIcon) : null;
  }

  public void setCloseIconResource(@DrawableRes int id) {
    setCloseIcon(AppCompatResources.getDrawable(context, id));
  }

  public void setCloseIcon(@Nullable Drawable closeIcon) {
    Drawable oldCloseIcon = getCloseIcon();
    if (oldCloseIcon != closeIcon) {
      float oldCloseIconWidth = calculateCloseIconWidth();
      this.closeIcon = closeIcon != null ? DrawableCompat.wrap(closeIcon).mutate() : null;
      if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
        updateFrameworkCloseIconRipple();
      }
      float newCloseIconWidth = calculateCloseIconWidth();

      unapplyChildDrawable(oldCloseIcon);
      if (showsCloseIcon()) {
        applyChildDrawable(this.closeIcon);
      }

      invalidateSelf();
      if (oldCloseIconWidth != newCloseIconWidth) {
        onSizeChange();
      }
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  private void updateFrameworkCloseIconRipple() {
    closeIconRipple =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(getRippleColor()),
            closeIcon,


            closeIconRippleMask);
  }

  @Nullable
  public ColorStateList getCloseIconTint() {
    return closeIconTint;
  }

  public void setCloseIconTintResource(@ColorRes int id) {
    setCloseIconTint(AppCompatResources.getColorStateList(context, id));
  }

  public void setCloseIconTint(@Nullable ColorStateList closeIconTint) {
    if (this.closeIconTint != closeIconTint) {
      this.closeIconTint = closeIconTint;

      if (showsCloseIcon()) {
        DrawableCompat.setTintList(closeIcon, closeIconTint);
      }

      onStateChange(getState());
    }
  }

  public float getCloseIconSize() {
    return closeIconSize;
  }

  public void setCloseIconSizeResource(@DimenRes int id) {
    setCloseIconSize(context.getResources().getDimension(id));
  }

  public void setCloseIconSize(float closeIconSize) {
    if (this.closeIconSize != closeIconSize) {
      this.closeIconSize = closeIconSize;
      invalidateSelf();
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }

  public void setCloseIconContentDescription(@Nullable CharSequence closeIconContentDescription) {
    if (this.closeIconContentDescription != closeIconContentDescription) {
      this.closeIconContentDescription =
          BidiFormatter.getInstance().unicodeWrap(closeIconContentDescription);

      invalidateSelf();
    }
  }

  @Nullable
  public CharSequence getCloseIconContentDescription() {
    return closeIconContentDescription;
  }

  public boolean isCheckable() {
    return checkable;
  }

  public void setCheckableResource(@BoolRes int id) {
    setCheckable(context.getResources().getBoolean(id));
  }

  public void setCheckable(boolean checkable) {
    if (this.checkable != checkable) {
      this.checkable = checkable;

      float oldChipIconWidth = calculateChipIconWidth();
      if (!checkable && currentChecked) {
        currentChecked = false;
      }
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }

  public boolean isCheckedIconVisible() {
    return checkedIconVisible;
  }


  @Deprecated
  public boolean isCheckedIconEnabled() {
    return isCheckedIconVisible();
  }

  public void setCheckedIconVisible(@BoolRes int id) {
    setCheckedIconVisible(context.getResources().getBoolean(id));
  }

  public void setCheckedIconVisible(boolean checkedIconVisible) {
    if (this.checkedIconVisible != checkedIconVisible) {
      boolean oldShowsCheckedIcon = showsCheckedIcon();
      this.checkedIconVisible = checkedIconVisible;
      boolean newShowsCheckedIcon = showsCheckedIcon();

      boolean changed = oldShowsCheckedIcon != newShowsCheckedIcon;
      if (changed) {
        if (newShowsCheckedIcon) {
          applyChildDrawable(checkedIcon);
        } else {
          unapplyChildDrawable(checkedIcon);
        }

        invalidateSelf();
        onSizeChange();
      }
    }
  }


  @Deprecated
  public void setCheckedIconEnabledResource(@BoolRes int id) {
    setCheckedIconVisible(context.getResources().getBoolean(id));
  }


  @Deprecated
  public void setCheckedIconEnabled(boolean checkedIconEnabled) {
    setCheckedIconVisible(checkedIconEnabled);
  }


  @Nullable
  public Drawable getCheckedIcon() {
    return checkedIcon;
  }


  public void setCheckedIconResource(@DrawableRes int id) {
    setCheckedIcon(AppCompatResources.getDrawable(context, id));
  }


  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    Drawable oldCheckedIcon = this.checkedIcon;
    if (oldCheckedIcon != checkedIcon) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.checkedIcon = checkedIcon;
      float newChipIconWidth = calculateChipIconWidth();

      unapplyChildDrawable(this.checkedIcon);
      applyChildDrawable(this.checkedIcon);

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }


  @Nullable
  public ColorStateList getCheckedIconTint() {
    return checkedIconTint;
  }


  public void setCheckedIconTintResource(@ColorRes int id) {
    setCheckedIconTint(AppCompatResources.getColorStateList(context, id));
  }


  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    if (this.checkedIconTint != checkedIconTint) {
      this.checkedIconTint = checkedIconTint;

      if (canShowCheckedIcon()) {
        DrawableCompat.setTintList(checkedIcon, checkedIconTint);
      }

      onStateChange(getState());
    }
  }


  @Nullable
  public MotionSpec getShowMotionSpec() {
    return showMotionSpec;
  }


  public void setShowMotionSpecResource(@AnimatorRes int id) {
    setShowMotionSpec(MotionSpec.createFromResource(context, id));
  }


  public void setShowMotionSpec(@Nullable MotionSpec showMotionSpec) {
    this.showMotionSpec = showMotionSpec;
  }


  @Nullable
  public MotionSpec getHideMotionSpec() {
    return hideMotionSpec;
  }


  public void setHideMotionSpecResource(@AnimatorRes int id) {
    setHideMotionSpec(MotionSpec.createFromResource(context, id));
  }


  public void setHideMotionSpec(@Nullable MotionSpec hideMotionSpec) {
    this.hideMotionSpec = hideMotionSpec;
  }


  public float getChipStartPadding() {
    return chipStartPadding;
  }


  public void setChipStartPaddingResource(@DimenRes int id) {
    setChipStartPadding(context.getResources().getDimension(id));
  }


  public void setChipStartPadding(float chipStartPadding) {
    if (this.chipStartPadding != chipStartPadding) {
      this.chipStartPadding = chipStartPadding;
      invalidateSelf();
      onSizeChange();
    }
  }


  public float getIconStartPadding() {
    return iconStartPadding;
  }


  public void setIconStartPaddingResource(@DimenRes int id) {
    setIconStartPadding(context.getResources().getDimension(id));
  }


  public void setIconStartPadding(float iconStartPadding) {
    if (this.iconStartPadding != iconStartPadding) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconStartPadding = iconStartPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }


  public float getIconEndPadding() {
    return iconEndPadding;
  }


  public void setIconEndPaddingResource(@DimenRes int id) {
    setIconEndPadding(context.getResources().getDimension(id));
  }


  public void setIconEndPadding(float iconEndPadding) {
    if (this.iconEndPadding != iconEndPadding) {
      float oldChipIconWidth = calculateChipIconWidth();
      this.iconEndPadding = iconEndPadding;
      float newChipIconWidth = calculateChipIconWidth();

      invalidateSelf();
      if (oldChipIconWidth != newChipIconWidth) {
        onSizeChange();
      }
    }
  }


  public float getTextStartPadding() {
    return textStartPadding;
  }


  public void setTextStartPaddingResource(@DimenRes int id) {
    setTextStartPadding(context.getResources().getDimension(id));
  }


  public void setTextStartPadding(float textStartPadding) {
    if (this.textStartPadding != textStartPadding) {
      this.textStartPadding = textStartPadding;
      invalidateSelf();
      onSizeChange();
    }
  }


  public float getTextEndPadding() {
    return textEndPadding;
  }


  public void setTextEndPaddingResource(@DimenRes int id) {
    setTextEndPadding(context.getResources().getDimension(id));
  }


  public void setTextEndPadding(float textEndPadding) {
    if (this.textEndPadding != textEndPadding) {
      this.textEndPadding = textEndPadding;
      invalidateSelf();
      onSizeChange();
    }
  }


  public float getCloseIconStartPadding() {
    return closeIconStartPadding;
  }


  public void setCloseIconStartPaddingResource(@DimenRes int id) {
    setCloseIconStartPadding(context.getResources().getDimension(id));
  }


  public void setCloseIconStartPadding(float closeIconStartPadding) {
    if (this.closeIconStartPadding != closeIconStartPadding) {
      this.closeIconStartPadding = closeIconStartPadding;
      invalidateSelf();
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }


  public float getCloseIconEndPadding() {
    return closeIconEndPadding;
  }


  public void setCloseIconEndPaddingResource(@DimenRes int id) {
    setCloseIconEndPadding(context.getResources().getDimension(id));
  }


  public void setCloseIconEndPadding(float closeIconEndPadding) {
    if (this.closeIconEndPadding != closeIconEndPadding) {
      this.closeIconEndPadding = closeIconEndPadding;
      invalidateSelf();
      if (showsCloseIcon()) {
        onSizeChange();
      }
    }
  }


  public float getChipEndPadding() {
    return chipEndPadding;
  }


  public void setChipEndPaddingResource(@DimenRes int id) {
    setChipEndPadding(context.getResources().getDimension(id));
  }


  public void setChipEndPadding(float chipEndPadding) {
    if (this.chipEndPadding != chipEndPadding) {
      this.chipEndPadding = chipEndPadding;
      invalidateSelf();
      onSizeChange();
    }
  }


  @Px
  public int getMaxWidth() {
    return maxWidth;
  }


  public void setMaxWidth(@Px int maxWidth) {
    this.maxWidth = maxWidth;
  }

  boolean shouldDrawText() {
    return shouldDrawText;
  }


  void setShouldDrawText(boolean shouldDrawText) {
    this.shouldDrawText = shouldDrawText;
  }
}
