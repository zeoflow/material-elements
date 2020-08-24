

package com.zeoflow.material.elements.card;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import androidx.cardview.widget.CardView;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialCardView extends CardView implements Checkable, Shapeable
{

  
  public interface OnCheckedChangeListener {
    
    void onCheckedChanged(MaterialCardView card, boolean isChecked);
  }

  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DRAGGED_STATE_SET = {R.attr.state_dragged};

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView;
  private static final String LOG_TAG = "MaterialCardView";
  private static final String ACCESSIBILITY_CLASS_NAME = "androidx.cardview.widget.CardView";

  @NonNull private final MaterialCardViewHelper cardViewHelper;

  
  private boolean isParentCardViewDoneInitializing;

  private boolean checked = false;
  private boolean dragged = false;
  private OnCheckedChangeListener onCheckedChangeListener;

  public MaterialCardView(Context context) {
    this(context, null );
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialCardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    isParentCardViewDoneInitializing = true;
    
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialCardView, defStyleAttr, DEF_STYLE_RES);

    
    cardViewHelper = new MaterialCardViewHelper(this, attrs, defStyleAttr, DEF_STYLE_RES);
    cardViewHelper.setCardBackgroundColor(super.getCardBackgroundColor());
    cardViewHelper.setUserContentPadding(
        super.getContentPaddingLeft(),
        super.getContentPaddingTop(),
        super.getContentPaddingRight(),
        super.getContentPaddingBottom());
    
    
    cardViewHelper.loadFromAttributes(attributes);

    attributes.recycle();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(ACCESSIBILITY_CLASS_NAME);
    info.setCheckable(isCheckable());
    info.setClickable(isClickable());
    info.setChecked(isChecked());
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent accessibilityEvent) {
    super.onInitializeAccessibilityEvent(accessibilityEvent);
    accessibilityEvent.setClassName(ACCESSIBILITY_CLASS_NAME);
    accessibilityEvent.setChecked(isChecked());
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    cardViewHelper.onMeasure(getMeasuredWidth(), getMeasuredHeight());
  }

  
  public void setStrokeColor(@ColorInt int strokeColor) {
    cardViewHelper.setStrokeColor(ColorStateList.valueOf(strokeColor));
  }

  
  public void setStrokeColor(ColorStateList strokeColor) {
    cardViewHelper.setStrokeColor(strokeColor);
  }

  
  @ColorInt
  @Deprecated
  public int getStrokeColor() {
    return cardViewHelper.getStrokeColor();
  }

  
  @Nullable
  public ColorStateList getStrokeColorStateList() {
    return cardViewHelper.getStrokeColorStateList();
  }

  
  public void setStrokeWidth(@Dimension int strokeWidth) {
    cardViewHelper.setStrokeWidth(strokeWidth);
  }

  
  @Dimension
  public int getStrokeWidth() {
    return cardViewHelper.getStrokeWidth();
  }

  @Override
  public void setRadius(float radius) {
    super.setRadius(radius);
    cardViewHelper.setCornerRadius(radius);
  }

  @Override
  public float getRadius() {
    return cardViewHelper.getCornerRadius();
  }

  float getCardViewRadius() {
    return MaterialCardView.super.getRadius();
  }


  
  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    cardViewHelper.setProgress(progress);
  }


  
  @FloatRange(from = 0f, to = 1f)
  public float getProgress() {
    return cardViewHelper.getProgress();
  }

  @Override
  public void setContentPadding(int left, int top, int right, int bottom) {
    cardViewHelper.setUserContentPadding(left, top, right, bottom);
  }

  void setAncestorContentPadding(int left, int top, int right, int bottom) {
    super.setContentPadding(left, top, right, bottom);
  }

  @Override
  public int getContentPaddingLeft() {
    return cardViewHelper.getUserContentPadding().left;
  }

  @Override
  public int getContentPaddingTop() {
    return cardViewHelper.getUserContentPadding().top;
  }

  @Override
  public int getContentPaddingRight() {
    return cardViewHelper.getUserContentPadding().right;
  }

  @Override
  public int getContentPaddingBottom() {
    return cardViewHelper.getUserContentPadding().bottom;
  }

  @Override
  public void setCardBackgroundColor(@ColorInt int color) {
    cardViewHelper.setCardBackgroundColor(ColorStateList.valueOf(color));
  }

  @Override
  public void setCardBackgroundColor(@Nullable ColorStateList color) {
    cardViewHelper.setCardBackgroundColor(color);
  }

  @NonNull
  @Override
  public ColorStateList getCardBackgroundColor() {
    return cardViewHelper.getCardBackgroundColor();
  }

  
  public void setCardForegroundColor(@Nullable ColorStateList foregroundColor) {
    cardViewHelper.setCardForegroundColor(foregroundColor);
  }

  
  @NonNull
  public ColorStateList getCardForegroundColor() {
    return cardViewHelper.getCardForegroundColor();
  }

  @Override
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    cardViewHelper.updateClickable();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, cardViewHelper.getBackground());
  }

  @Override
  public void setCardElevation(float elevation) {
    super.setCardElevation(elevation);
    cardViewHelper.updateElevation();
  }

  @Override
  public void setMaxCardElevation(float maxCardElevation) {
    super.setMaxCardElevation(maxCardElevation);
    cardViewHelper.updateInsets();
  }

  @Override
  public void setUseCompatPadding(boolean useCompatPadding) {
    super.setUseCompatPadding(useCompatPadding);
    cardViewHelper.updateInsets();
    cardViewHelper.updateContentPadding();
  }

  @Override
  public void setPreventCornerOverlap(boolean preventCornerOverlap) {
    super.setPreventCornerOverlap(preventCornerOverlap);
    cardViewHelper.updateInsets();
    cardViewHelper.updateContentPadding();
  }

  @Override
  public void setBackground(Drawable drawable) {
    setBackgroundDrawable(drawable);
  }

  @Override
  public void setBackgroundDrawable(Drawable drawable) {
    if (isParentCardViewDoneInitializing) {
      if (!cardViewHelper.isBackgroundOverwritten()) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.");
        cardViewHelper.setBackgroundOverwritten(true);
      }
      super.setBackgroundDrawable(drawable);
    }
    
  }

  
  void setBackgroundInternal(Drawable drawable) {
    super.setBackgroundDrawable(drawable);
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void setChecked(boolean checked) {
    if (this.checked != checked) {
      toggle();
    }
  }

  
  public void setDragged(boolean dragged) {
    if (this.dragged != dragged) {
      this.dragged = dragged;
      refreshDrawableState();
      forceRippleRedrawIfNeeded();
      invalidate();
    }
  }

  public boolean isDragged() {
    return dragged;
  }

  
  public boolean isCheckable() {
    return cardViewHelper != null && cardViewHelper.isCheckable();
  }

  
  public void setCheckable(boolean checkable) {
    cardViewHelper.setCheckable(checkable);
  }

  @Override
  public void toggle() {
    if (isCheckable() && isEnabled()) {
      checked = !checked;
      refreshDrawableState();
      forceRippleRedrawIfNeeded();
      if (onCheckedChangeListener != null) {
        onCheckedChangeListener.onCheckedChanged(this, checked);
      }
    }
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
    if (isCheckable()) {
      mergeDrawableStates(drawableState, CHECKABLE_STATE_SET);
    }

    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }

    if (isDragged()) {
      mergeDrawableStates(drawableState, DRAGGED_STATE_SET);
    }

    return drawableState;
  }

  
  public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
    onCheckedChangeListener = listener;
  }

  
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    cardViewHelper.setRippleColor(rippleColor);
  }

  
  public void setRippleColorResource(@ColorRes int rippleColorResourceId) {
    cardViewHelper.setRippleColor(
        AppCompatResources.getColorStateList(getContext(), rippleColorResourceId));
  }

  
  public ColorStateList getRippleColor() {
    return cardViewHelper.getRippleColor();
  }

  
  @Nullable
  public Drawable getCheckedIcon() {
    return cardViewHelper.getCheckedIcon();
  }

  
  public void setCheckedIconResource(@DrawableRes int id) {
    cardViewHelper.setCheckedIcon(AppCompatResources.getDrawable(getContext(), id));
  }

  
  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    cardViewHelper.setCheckedIcon(checkedIcon);
  }

  
  @Nullable
  public ColorStateList getCheckedIconTint() {
    return cardViewHelper.getCheckedIconTint();
  }

  
  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    cardViewHelper.setCheckedIconTint(checkedIconTint);
  }

  @NonNull
  private RectF getBoundsAsRectF() {
    RectF boundsRectF = new RectF();
    boundsRectF.set(cardViewHelper.getBackground().getBounds());
    return boundsRectF;
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setClipToOutline(shapeAppearanceModel.isRoundRect(getBoundsAsRectF()));
    }
    cardViewHelper.setShapeAppearanceModel(shapeAppearanceModel);
  }

  
  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return cardViewHelper.getShapeAppearanceModel();
  }

  private void forceRippleRedrawIfNeeded() {
    if (VERSION.SDK_INT > VERSION_CODES.O) {
      cardViewHelper.forceRippleRedraw();
    }
  }
}
