
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import android.widget.TextView;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;


final class CalendarItemStyle {

  
  @NonNull private final Rect insets;

  private final ColorStateList textColor;
  private final ColorStateList backgroundColor;
  private final ColorStateList strokeColor;
  private final int strokeWidth;
  private final ShapeAppearanceModel itemShape;

  private CalendarItemStyle(
      ColorStateList backgroundColor,
      ColorStateList textColor,
      ColorStateList strokeColor,
      int strokeWidth,
      ShapeAppearanceModel itemShape,
      @NonNull Rect insets) {
    Preconditions.checkArgumentNonnegative(insets.left);
    Preconditions.checkArgumentNonnegative(insets.top);
    Preconditions.checkArgumentNonnegative(insets.right);
    Preconditions.checkArgumentNonnegative(insets.bottom);

    this.insets = insets;
    this.textColor = textColor;
    this.backgroundColor = backgroundColor;
    this.strokeColor = strokeColor;
    this.strokeWidth = strokeWidth;
    this.itemShape = itemShape;
  }

  
  @NonNull
  static CalendarItemStyle create(
      @NonNull Context context, @StyleRes int materialCalendarItemStyle) {
    Preconditions.checkArgument(
        materialCalendarItemStyle != 0, "Cannot create a CalendarItemStyle with a styleResId of 0");

    TypedArray styleableArray =
        context.obtainStyledAttributes(materialCalendarItemStyle, R.styleable.MaterialCalendarItem);
    int insetLeft =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetLeft, 0);
    int insetTop =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetTop, 0);
    int insetRight =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetRight, 0);
    int insetBottom =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetBottom, 0);
    Rect insets = new Rect(insetLeft, insetTop, insetRight, insetBottom);

    ColorStateList backgroundColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemFillColor);
    ColorStateList textColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemTextColor);
    ColorStateList strokeColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemStrokeColor);
    int strokeWidth =
        styleableArray.getDimensionPixelSize(R.styleable.MaterialCalendarItem_itemStrokeWidth, 0);

    int shapeAppearanceResId =
        styleableArray.getResourceId(R.styleable.MaterialCalendarItem_itemShapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        styleableArray.getResourceId(
            R.styleable.MaterialCalendarItem_itemShapeAppearanceOverlay, 0);

    ShapeAppearanceModel itemShape =
        ShapeAppearanceModel.builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId)
            .build();

    styleableArray.recycle();

    return new CalendarItemStyle(
        backgroundColor, textColor, strokeColor, strokeWidth, itemShape, insets);
  }

  
  void styleItem(@NonNull TextView item) {
    MaterialShapeDrawable backgroundDrawable = new MaterialShapeDrawable();
    MaterialShapeDrawable shapeMask = new MaterialShapeDrawable();
    backgroundDrawable.setShapeAppearanceModel(itemShape);
    shapeMask.setShapeAppearanceModel(itemShape);
    backgroundDrawable.setFillColor(backgroundColor);
    backgroundDrawable.setStroke(strokeWidth, strokeColor);
    item.setTextColor(textColor);
    Drawable d;
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      d = new RippleDrawable(textColor.withAlpha(30), backgroundDrawable, shapeMask);
    } else {
      d = backgroundDrawable;
    }
    ViewCompat.setBackground(
        item, new InsetDrawable(d, insets.left, insets.top, insets.right, insets.bottom));
  }

  int getLeftInset() {
    return insets.left;
  }

  int getRightInset() {
    return insets.right;
  }

  int getTopInset() {
    return insets.top;
  }

  int getBottomInset() {
    return insets.bottom;
  }
}
