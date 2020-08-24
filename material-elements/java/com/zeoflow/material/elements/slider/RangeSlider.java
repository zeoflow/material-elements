

package com.zeoflow.material.elements.slider;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import com.zeoflow.material.elements.slider.RangeSlider.OnChangeListener;
import com.zeoflow.material.elements.slider.RangeSlider.OnSliderTouchListener;
import java.util.ArrayList;
import java.util.List;


public class RangeSlider extends BaseSlider<RangeSlider, OnChangeListener, OnSliderTouchListener> {

  public RangeSlider(@NonNull Context context) {
    this(context, null);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, new int[] {R.attr.values});

    if (a.hasValue(0)) {
      int valuesId = a.getResourceId(0, 0);
      TypedArray values = a.getResources().obtainTypedArray(valuesId);
      setValues(convertToFloat(values));
    }
    a.recycle();
  }

  
  public interface OnChangeListener extends BaseOnChangeListener<RangeSlider> {}

  
  public interface OnSliderTouchListener extends BaseOnSliderTouchListener<RangeSlider> {}

  
  @Override
  public void setValues(@NonNull Float... values) {
    super.setValues(values);
  }

  
  @Override
  public void setValues(@NonNull List<Float> values) {
    super.setValues(values);
  }

  
  @NonNull
  @Override
  public List<Float> getValues() {
    return super.getValues();
  }

  private static List<Float> convertToFloat(TypedArray values) {
    List<Float> ret = new ArrayList<>();
    for (int i = 0; i < values.length(); ++i) {
      ret.add(values.getFloat(i, -1));
    }
    return ret;
  }
}
