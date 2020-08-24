

package com.zeoflow.material.elements.floatingactionbutton;

import com.google.android.material.R;

import static androidx.core.util.Preconditions.checkNotNull;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.view.View;
import com.zeoflow.material.elements.ripple.RippleUtils;
import com.zeoflow.material.elements.shadow.ShadowViewDelegate;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(21)
class FloatingActionButtonImplLollipop extends FloatingActionButtonImpl {

  FloatingActionButtonImplLollipop(
      FloatingActionButton view, ShadowViewDelegate shadowViewDelegate) {
    super(view, shadowViewDelegate);
  }

  @Override
  void initializeBackgroundDrawable(
      ColorStateList backgroundTint,
      @Nullable PorterDuff.Mode backgroundTintMode,
      ColorStateList rippleColor,
      int borderWidth) {
    
    shapeDrawable = createShapeDrawable();
    shapeDrawable.setTintList(backgroundTint);
    if (backgroundTintMode != null) {
      shapeDrawable.setTintMode(backgroundTintMode);
    }
    shapeDrawable.initializeElevationOverlay(view.getContext());

    final Drawable rippleContent;
    if (borderWidth > 0) {
      borderDrawable = createBorderDrawable(borderWidth, backgroundTint);
      rippleContent = new LayerDrawable(
          new Drawable[]{checkNotNull(borderDrawable), checkNotNull(shapeDrawable)});
    } else {
      borderDrawable = null;
      rippleContent = shapeDrawable;
    }

    rippleDrawable =
        new RippleDrawable(
            RippleUtils.sanitizeRippleDrawableColor(rippleColor), rippleContent, null);

    contentBackground = rippleDrawable;
  }

  @Override
  void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (rippleDrawable instanceof RippleDrawable) {
      ((RippleDrawable) rippleDrawable)
          .setColor(RippleUtils.sanitizeRippleDrawableColor(rippleColor));
    } else {
      super.setRippleColor(rippleColor);
    }
  }

  @Override
  void onElevationsChanged(
      final float elevation,
      final float hoveredFocusedTranslationZ,
      final float pressedTranslationZ) {

    if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
      
      
      view.refreshDrawableState();
    } else {
      final StateListAnimator stateListAnimator = new StateListAnimator();

      
      stateListAnimator.addState(
          PRESSED_ENABLED_STATE_SET, createElevationAnimator(elevation, pressedTranslationZ));
      stateListAnimator.addState(
          HOVERED_FOCUSED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));
      stateListAnimator.addState(
          FOCUSED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));
      stateListAnimator.addState(
          HOVERED_ENABLED_STATE_SET,
          createElevationAnimator(elevation, hoveredFocusedTranslationZ));

      
      AnimatorSet set = new AnimatorSet();
      List<Animator> animators = new ArrayList<>();
      animators.add(ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(0));
      if (Build.VERSION.SDK_INT >= 22 && Build.VERSION.SDK_INT <= 24) {
        
        
        
        
        animators.add(
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, view.getTranslationZ())
                .setDuration(ELEVATION_ANIM_DELAY));
      }
      animators.add(
          ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, 0f)
              .setDuration(ELEVATION_ANIM_DURATION));
      set.playSequentially(animators.toArray(new Animator[0]));
      set.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
      stateListAnimator.addState(ENABLED_STATE_SET, set);

      
      stateListAnimator.addState(EMPTY_STATE_SET, createElevationAnimator(0f, 0f));

      view.setStateListAnimator(stateListAnimator);
    }

    if (shouldAddPadding()) {
      updatePadding();
    }
  }

  @NonNull
  private Animator createElevationAnimator(float elevation, float translationZ) {
    AnimatorSet set = new AnimatorSet();
    set.play(ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(0))
        .with(
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, translationZ)
                .setDuration(ELEVATION_ANIM_DURATION));
    set.setInterpolator(ELEVATION_ANIM_INTERPOLATOR);
    return set;
  }

  @Override
  public float getElevation() {
    return view.getElevation();
  }

  @Override
  void onCompatShadowChanged() {
    updatePadding();
  }

  @Override
  boolean shouldAddPadding() {
    return shadowViewDelegate.isCompatPaddingEnabled() || !shouldExpandBoundsForA11y();
  }

  @Override
  void onDrawableStateChanged(int[] state) {
    if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
      if (view.isEnabled()) {
        view.setElevation(elevation);
        if (view.isPressed()) {
          view.setTranslationZ(pressedTranslationZ);
        } else if (view.isFocused() || view.isHovered()) {
          view.setTranslationZ(hoveredFocusedTranslationZ);
        } else {
          view.setTranslationZ(0);
        }
      } else {
        view.setElevation(0);
        view.setTranslationZ(0);
      }
    }
  }

  @Override
  void jumpDrawableToCurrentState() {
    
  }

  @Override
  void updateFromViewRotation() {
    
  }

  @Override
  boolean requirePreDrawListener() {
    return false;
  }

  @NonNull
  BorderDrawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
    final Context context = view.getContext();
    BorderDrawable borderDrawable =  new BorderDrawable(checkNotNull(shapeAppearance));
    borderDrawable.setGradientColors(
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_outer_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_top_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_inner_color),
        ContextCompat.getColor(context, R.color.design_fab_stroke_end_outer_color));
    borderDrawable.setBorderWidth(borderWidth);
    borderDrawable.setBorderTint(backgroundTint);
    return borderDrawable;
  }

  @NonNull
  @Override
  MaterialShapeDrawable createShapeDrawable() {
    ShapeAppearanceModel shapeAppearance = checkNotNull(this.shapeAppearance);
    return new AlwaysStatefulMaterialShapeDrawable(shapeAppearance);
  }

  @Override
  void getPadding(@NonNull Rect rect) {
    if (shadowViewDelegate.isCompatPaddingEnabled()) {
      super.getPadding(rect);
    } else if (!shouldExpandBoundsForA11y()) {
      int minPadding = (minTouchTargetSize - view.getSizeDimension()) / 2;
      rect.set(minPadding, minPadding, minPadding, minPadding);
    } else {
      rect.set(0, 0, 0, 0);
    }
  }

  
  static class AlwaysStatefulMaterialShapeDrawable extends MaterialShapeDrawable {

    AlwaysStatefulMaterialShapeDrawable(ShapeAppearanceModel shapeAppearanceModel) {
      super(shapeAppearanceModel);
    }

    @Override
    public boolean isStateful() {
      return true;
    }
  }
}
