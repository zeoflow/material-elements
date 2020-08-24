
package com.zeoflow.material.elements.circularreveal;

import android.animation.TypeEvaluator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Property;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.zeoflow.material.elements.math.MathUtils;


public interface CircularRevealWidget extends CircularRevealHelper.Delegate
{

  
  void draw(Canvas canvas);

  
  boolean isOpaque();

  
  void buildCircularRevealCache();

  
  void destroyCircularRevealCache();

  
  @Nullable
  RevealInfo getRevealInfo();

  
  void setRevealInfo(@Nullable RevealInfo revealInfo);

  
  @ColorInt
  int getCircularRevealScrimColor();

  
  void setCircularRevealScrimColor(@ColorInt int color);

  
  @Nullable
  Drawable getCircularRevealOverlayDrawable();

  
  void setCircularRevealOverlayDrawable(@Nullable Drawable drawable);

  
  class RevealInfo {

    
    public static final float INVALID_RADIUS = Float.MAX_VALUE;

    
    public float centerX;
    
    public float centerY;
    
    public float radius;

    private RevealInfo() {}

    public RevealInfo(float centerX, float centerY, float radius) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
    }

    public RevealInfo(@NonNull RevealInfo other) {
      this(other.centerX, other.centerY, other.radius);
    }

    public void set(float centerX, float centerY, float radius) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.radius = radius;
    }

    public void set(@NonNull RevealInfo other) {
      set(other.centerX, other.centerY, other.radius);
    }

    
    public boolean isInvalid() {
      return radius == INVALID_RADIUS;
    }
  }

  
  class CircularRevealProperty extends Property<CircularRevealWidget, RevealInfo> {

    public static final Property<CircularRevealWidget, RevealInfo> CIRCULAR_REVEAL =
        new CircularRevealProperty("circularReveal");

    private CircularRevealProperty(String name) {
      super(RevealInfo.class, name);
    }

    @Nullable
    @Override
    public RevealInfo get(@NonNull CircularRevealWidget object) {
      return object.getRevealInfo();
    }

    @Override
    public void set(@NonNull CircularRevealWidget object, @Nullable RevealInfo value) {
      object.setRevealInfo(value);
    }
  }

  
  class CircularRevealEvaluator implements TypeEvaluator<RevealInfo> {

    public static final TypeEvaluator<RevealInfo> CIRCULAR_REVEAL = new CircularRevealEvaluator();
    private final RevealInfo revealInfo = new RevealInfo();

    @NonNull
    @Override
    public RevealInfo evaluate(
        float fraction, @NonNull RevealInfo startValue, @NonNull RevealInfo endValue) {
      revealInfo.set(
          MathUtils.lerp(startValue.centerX, endValue.centerX, fraction),
          MathUtils.lerp(startValue.centerY, endValue.centerY, fraction),
          MathUtils.lerp(startValue.radius, endValue.radius, fraction));
      return revealInfo;
    }
  }

  
  class CircularRevealScrimColorProperty extends Property<CircularRevealWidget, Integer> {

    public static final Property<CircularRevealWidget, Integer> CIRCULAR_REVEAL_SCRIM_COLOR =
        new CircularRevealScrimColorProperty("circularRevealScrimColor");

    private CircularRevealScrimColorProperty(String name) {
      super(Integer.class, name);
    }

    @NonNull
    @Override
    public Integer get(@NonNull CircularRevealWidget object) {
      return object.getCircularRevealScrimColor();
    }

    @Override
    public void set(@NonNull CircularRevealWidget object, @NonNull Integer value) {
      object.setCircularRevealScrimColor(value);
    }
  }
}
