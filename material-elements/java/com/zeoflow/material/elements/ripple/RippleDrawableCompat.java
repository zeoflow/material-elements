

package com.zeoflow.material.elements.ripple;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.graphics.drawable.TintAwareDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.shape.Shapeable;


@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleDrawableCompat extends Drawable implements Shapeable, TintAwareDrawable {

  private RippleDrawableCompatState drawableState;

  
  public RippleDrawableCompat(ShapeAppearanceModel shapeAppearanceModel) {
    this(new RippleDrawableCompatState(new MaterialShapeDrawable(shapeAppearanceModel)));
  }

  private RippleDrawableCompat(RippleDrawableCompatState state) {
    super();
    this.drawableState = state;
  }

  @Override
  public void setTint(@ColorInt int tintColor) {
    drawableState.delegate.setTint(tintColor);
  }

  @Override
  public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
    drawableState.delegate.setTintMode(tintMode);
  }

  @Override
  public void setTintList(@Nullable ColorStateList tintList) {
    drawableState.delegate.setTintList(tintList);
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    drawableState.delegate.setShapeAppearanceModel(shapeAppearanceModel);
  }

  
  @Override
  @NonNull
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return drawableState.delegate.getShapeAppearanceModel();
  }

  
  @Override
  public boolean isStateful() {
    return true;
  }

  @Override
  protected boolean onStateChange(@NonNull int[] stateSet) {
    boolean changed = super.onStateChange(stateSet);
    if (drawableState.delegate.setState(stateSet)) {
      changed = true;
    }
    boolean shouldDrawRipple = RippleUtils.shouldDrawRippleCompat(stateSet);
    
    
    if (drawableState.shouldDrawDelegate != shouldDrawRipple) {
      drawableState.shouldDrawDelegate = shouldDrawRipple;
      changed = true;
    }
    return changed;
  }

  @Override
  public void draw(Canvas canvas) {
    
    if (drawableState.shouldDrawDelegate) {
      drawableState.delegate.draw(canvas);
    }
  }

  @Override
  protected void onBoundsChange(@NonNull Rect bounds) {
    super.onBoundsChange(bounds);
    drawableState.delegate.setBounds(bounds);
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return drawableState;
  }

  @NonNull
  @Override
  public RippleDrawableCompat mutate() {
    RippleDrawableCompatState newDrawableState = new RippleDrawableCompatState(drawableState);
    drawableState = newDrawableState;
    return this;
  }

  @Override
  public void setAlpha(int alpha) {
    drawableState.delegate.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    drawableState.delegate.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return drawableState.delegate.getOpacity();
  }

  
  static final class RippleDrawableCompatState extends ConstantState {

    @NonNull MaterialShapeDrawable delegate;
    boolean shouldDrawDelegate;

    public RippleDrawableCompatState(MaterialShapeDrawable delegate) {
      this.delegate = delegate;
      this.shouldDrawDelegate = false;
    }

    public RippleDrawableCompatState(@NonNull RippleDrawableCompatState orig) {
      this.delegate = (MaterialShapeDrawable) orig.delegate.getConstantState().newDrawable();
      this.shouldDrawDelegate = orig.shouldDrawDelegate;
    }

    @NonNull
    @Override
    public RippleDrawableCompat newDrawable() {
      return new RippleDrawableCompat(new RippleDrawableCompatState(this));
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
