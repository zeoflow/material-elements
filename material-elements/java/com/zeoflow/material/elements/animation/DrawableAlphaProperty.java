
package com.zeoflow.material.elements.animation;

import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Property;
import java.util.WeakHashMap;


public class DrawableAlphaProperty extends Property<Drawable, Integer> {

  
  public static final Property<Drawable, Integer> DRAWABLE_ALPHA_COMPAT =
      new DrawableAlphaProperty();

  private final WeakHashMap<Drawable, Integer> alphaCache = new WeakHashMap<>();

  private DrawableAlphaProperty() {
    super(Integer.class, "drawableAlphaCompat");
  }

  @Nullable
  @Override
  public Integer get(@NonNull Drawable object) {
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      return object.getAlpha();
    }
    if (alphaCache.containsKey(object)) {
      return alphaCache.get(object);
    }
    return 0xFF;
  }

  @Override
  public void set(@NonNull Drawable object, @NonNull Integer value) {
    if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
      alphaCache.put(object, value);
    }

    object.setAlpha(value);
  }
}
