

package com.zeoflow.material.elements.slider;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import com.zeoflow.material.elements.slider.Slider.OnChangeListener;
import com.zeoflow.material.elements.slider.Slider.OnSliderTouchListener;


public class Slider extends BaseSlider<Slider, OnChangeListener, OnSliderTouchListener> {

  
  public interface OnChangeListener extends BaseOnChangeListener<Slider> {}

  
  public interface OnSliderTouchListener extends BaseOnSliderTouchListener<Slider> {}

  public Slider(@NonNull Context context) {
    this(context, null);
  }

  public Slider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public Slider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, new int[] {android.R.attr.value});
    if (a.hasValue(0)) {
      setValue(a.getFloat(0, 0f));
    }
    a.recycle();
  }

  
  public float getValue() {
    return getValues().get(0);
  }

  
  public void setValue(float value) {
    setValues(value);
  }

  @Override
  protected boolean pickActiveThumb() {
    if (getActiveThumbIndex() != -1) {
      return true;
    }
    
    setActiveThumbIndex(0);
    return true;
  }
}
