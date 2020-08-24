

package com.zeoflow.material.elements.tooltip;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.TextDrawableHelper;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.resources.TextAppearance;
import com.zeoflow.material.elements.shape.EdgeTreatment;
import com.zeoflow.material.elements.shape.MarkerEdgeTreatment;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.OffsetEdgeTreatment;


@RestrictTo(LIBRARY_GROUP)
public class TooltipDrawable extends MaterialShapeDrawable implements TextDrawableHelper.TextDrawableDelegate
{

  @StyleRes private static final int DEFAULT_STYLE = R.style.Widget_MaterialComponents_Tooltip;
  @AttrRes private static final int DEFAULT_THEME_ATTR = R.attr.tooltipStyle;

  @Nullable private CharSequence text;
  @NonNull private final Context context;
  @Nullable private final FontMetrics fontMetrics = new FontMetrics();

  @NonNull
  private final TextDrawableHelper textDrawableHelper =
      new TextDrawableHelper( this);

  @NonNull
  private final OnLayoutChangeListener attachedViewLayoutChangeListener =
      new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(
            View v,
            int left,
            int top,
            int right,
            int bottom,
            int oldLeft,
            int oldTop,
            int oldRight,
            int oldBottom) {
          updateLocationOnScreen(v);
        }
      };

  @NonNull private final Rect displayFrame = new Rect();

  private int padding;
  private int minWidth;
  private int minHeight;
  private int layoutMargin;
  private int arrowSize;
  private int locationOnScreenX;

  
  @NonNull
  public static TooltipDrawable createFromAttributes(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TooltipDrawable tooltip = new TooltipDrawable(context, attrs, defStyleAttr, defStyleRes);
    tooltip.loadFromAttributes(attrs, defStyleAttr, defStyleRes);

    return tooltip;
  }

  
  @NonNull
  public static TooltipDrawable createFromAttributes(
      @NonNull Context context, @Nullable AttributeSet attrs) {
    return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  @NonNull
  public static TooltipDrawable create(@NonNull Context context) {
    return createFromAttributes(context, null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  private TooltipDrawable(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.context = context;
    textDrawableHelper.getTextPaint().density = context.getResources().getDisplayMetrics().density;
    textDrawableHelper.getTextPaint().setTextAlign(Align.CENTER);
  }

  private void loadFromAttributes(
      @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Tooltip, defStyleAttr, defStyleRes);

    arrowSize = context.getResources().getDimensionPixelSize(R.dimen.mtrl_tooltip_arrowSize);
    setShapeAppearanceModel(
        getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());

    setText(a.getText(R.styleable.Tooltip_android_text));
    setTextAppearance(
        MaterialResources.getTextAppearance(
            context, a, R.styleable.Tooltip_android_textAppearance));

    int onBackground =
        MaterialColors.getColor(
            context, R.attr.colorOnBackground, TooltipDrawable.class.getCanonicalName());
    int background =
        MaterialColors.getColor(
            context, android.R.attr.colorBackground, TooltipDrawable.class.getCanonicalName());

    int backgroundTintDefault =
        MaterialColors.layer(
            ColorUtils.setAlphaComponent(background, (int) (0.9f * 255)),
            ColorUtils.setAlphaComponent(onBackground, (int) (0.6f * 255)));
    setFillColor(
        ColorStateList.valueOf(
            a.getColor(R.styleable.Tooltip_backgroundTint, backgroundTintDefault)));

    setStrokeColor(
        ColorStateList.valueOf(
            MaterialColors.getColor(
                context, R.attr.colorSurface, TooltipDrawable.class.getCanonicalName())));

    padding = a.getDimensionPixelSize(R.styleable.Tooltip_android_padding, 0);
    minWidth = a.getDimensionPixelSize(R.styleable.Tooltip_android_minWidth, 0);
    minHeight = a.getDimensionPixelSize(R.styleable.Tooltip_android_minHeight, 0);
    layoutMargin = a.getDimensionPixelSize(R.styleable.Tooltip_android_layout_margin, 0);

    a.recycle();
  }

  
  @Nullable
  public CharSequence getText() {
    return text;
  }

  
  public void setTextResource(@StringRes int id) {
    setText(context.getResources().getString(id));
  }

  
  public void setText(@Nullable CharSequence text) {
    if (!TextUtils.equals(this.text, text)) {
      this.text = text;
      textDrawableHelper.setTextWidthDirty(true);
      invalidateSelf();
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

  
  public int getMinWidth() {
    return minWidth;
  }

  
  public void setMinWidth(@Px int minWidth) {
    this.minWidth = minWidth;
    invalidateSelf();
  }

  
  public int getMinHeight() {
    return minHeight;
  }

  
  public void setMinHeight(@Px int minHeight) {
    this.minHeight = minHeight;
    invalidateSelf();
  }

  
  public int getTextPadding() {
    return padding;
  }

  
  public void setTextPadding(@Px int padding) {
    this.padding = padding;
    invalidateSelf();
  }

  
  public int getLayoutMargin() {
    return layoutMargin;
  }

  
  public void setLayoutMargin(@Px int layoutMargin) {
    this.layoutMargin = layoutMargin;
    invalidateSelf();
  }

  
  public void setRelativeToView(@Nullable View view) {
    if (view == null) {
      return;
    }
    updateLocationOnScreen(view);
    
    view.addOnLayoutChangeListener(attachedViewLayoutChangeListener);
  }

  
  public void detachView(@Nullable View view) {
    if (view == null) {
      return;
    }
    view.removeOnLayoutChangeListener(attachedViewLayoutChangeListener);
  }

  @Override
  public int getIntrinsicWidth() {
    return (int) Math.max(2 * padding + getTextWidth(), minWidth);
  }

  @Override
  public int getIntrinsicHeight() {
    return (int) Math.max(textDrawableHelper.getTextPaint().getTextSize(), minHeight);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.save();

    
    
    float translateX = calculatePointerOffset();

    
    
    float translateY = (float) -(arrowSize * Math.sqrt(2) - arrowSize);

    canvas.translate(translateX, translateY);

    
    super.draw(canvas);

    
    drawText(canvas);

    canvas.restore();
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);

    
    
    setShapeAppearanceModel(
        getShapeAppearanceModel().toBuilder().setBottomEdge(createMarkerEdge()).build());
  }

  @Override
  public boolean onStateChange(int[] state) {
    
    return super.onStateChange(state);
  }

  @Override
  public void onTextSizeChange() {
    invalidateSelf();
  }

  private void updateLocationOnScreen(@NonNull View v) {
    int[] locationOnScreen = new int[2];
    v.getLocationOnScreen(locationOnScreen);
    locationOnScreenX = locationOnScreen[0];
    v.getWindowVisibleDisplayFrame(displayFrame);
  }

  private float calculatePointerOffset() {
    float pointerOffset = 0;
    if (displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin < 0) {
      pointerOffset = displayFrame.right - getBounds().right - locationOnScreenX - layoutMargin;
    } else if (displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin > 0) {
      pointerOffset = displayFrame.left - getBounds().left - locationOnScreenX + layoutMargin;
    }
    return pointerOffset;
  }

  private EdgeTreatment createMarkerEdge() {
    float offset = -calculatePointerOffset();
    
    float maxArrowOffset = (float) (getBounds().width() - arrowSize * Math.sqrt(2)) / 2.0f;
    offset = Math.max(offset, -maxArrowOffset);
    offset = Math.min(offset, maxArrowOffset);
    return new OffsetEdgeTreatment(new MarkerEdgeTreatment(arrowSize), offset);
  }

  private void drawText(@NonNull Canvas canvas) {
    if (text == null) {
      
      return;
    }

    Rect bounds = getBounds();
    int y = (int) calculateTextOriginAndAlignment(bounds);

    if (textDrawableHelper.getTextAppearance() != null) {
      textDrawableHelper.getTextPaint().drawableState = getState();
      textDrawableHelper.updateTextPaintDrawState(context);
    }

    canvas.drawText(text, 0, text.length(), bounds.centerX(), y, textDrawableHelper.getTextPaint());
  }

  private float getTextWidth() {
    if (text == null) {
      return 0;
    }
    return textDrawableHelper.getTextWidth(text.toString());
  }

  
  private float calculateTextOriginAndAlignment(@NonNull Rect bounds) {
    return bounds.centerY() - calculateTextCenterFromBaseline();
  }

  
  private float calculateTextCenterFromBaseline() {
    textDrawableHelper.getTextPaint().getFontMetrics(fontMetrics);
    return (fontMetrics.descent + fontMetrics.ascent) / 2f;
  }
}
