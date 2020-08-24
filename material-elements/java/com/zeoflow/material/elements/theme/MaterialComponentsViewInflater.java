

package com.zeoflow.material.elements.theme;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.zeoflow.material.elements.button.MaterialButton;
import com.zeoflow.material.elements.checkbox.MaterialCheckBox;
import com.zeoflow.material.elements.radiobutton.MaterialRadioButton;
import com.zeoflow.material.elements.textfield.MaterialAutoCompleteTextView;
import com.zeoflow.material.elements.textview.MaterialTextView;


public class MaterialComponentsViewInflater extends AppCompatViewInflater {
  @NonNull
  @Override
  protected AppCompatButton createButton(@NonNull Context context, @NonNull AttributeSet attrs) {
    return new MaterialButton(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatCheckBox createCheckBox(Context context, AttributeSet attrs) {
    return new MaterialCheckBox(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatRadioButton createRadioButton(Context context, AttributeSet attrs) {
    return new MaterialRadioButton(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatTextView createTextView(Context context, AttributeSet attrs) {
    return new MaterialTextView(context, attrs);
  }

  @NonNull
  @Override
  protected AppCompatAutoCompleteTextView createAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attrs) {
    return new MaterialAutoCompleteTextView(context, attrs);
  }
}
