
package com.zeoflow.material.elements.circularreveal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.GridLayout;


public class CircularRevealGridLayout extends GridLayout implements CircularRevealWidget {

  @NonNull private final CircularRevealHelper helper;

  public CircularRevealGridLayout(Context context) {
    this(context, null);
  }

  public CircularRevealGridLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    helper = new CircularRevealHelper(this);
  }

  @Override
  public void buildCircularRevealCache() {
    helper.buildCircularRevealCache();
  }

  @Override
  public void destroyCircularRevealCache() {
    helper.destroyCircularRevealCache();
  }

  @Nullable
  @Override
  public RevealInfo getRevealInfo() {
    return helper.getRevealInfo();
  }

  @Override
  public void setRevealInfo(@Nullable RevealInfo revealInfo) {
    helper.setRevealInfo(revealInfo);
  }

  @Override
  public int getCircularRevealScrimColor() {
    return helper.getCircularRevealScrimColor();
  }

  @Override
  public void setCircularRevealScrimColor(@ColorInt int color) {
    helper.setCircularRevealScrimColor(color);
  }

  @Nullable
  @Override
  public Drawable getCircularRevealOverlayDrawable() {
    return helper.getCircularRevealOverlayDrawable();
  }

  @Override
  public void setCircularRevealOverlayDrawable(@Nullable Drawable drawable) {
    helper.setCircularRevealOverlayDrawable(drawable);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (helper != null) {
      helper.draw(canvas);
    } else {
      super.draw(canvas);
    }
  }

  @Override
  public void actualDraw(Canvas canvas) {
    super.draw(canvas);
  }

  @Override
  public boolean isOpaque() {
    if (helper != null) {
      return helper.isOpaque();
    } else {
      return super.isOpaque();
    }
  }

  @Override
  public boolean actualIsOpaque() {
    return super.isOpaque();
  }
}
