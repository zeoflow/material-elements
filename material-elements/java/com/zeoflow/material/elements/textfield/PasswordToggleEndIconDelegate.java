

package com.zeoflow.material.elements.textfield;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.zeoflow.material.elements.textfield.TextInputLayout.OnEditTextAttachedListener;
import com.zeoflow.material.elements.textfield.TextInputLayout.OnEndIconChangedListener;


class PasswordToggleEndIconDelegate extends EndIconDelegate {

  private final TextWatcher textWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          
          
          endIconView.setChecked(!hasPasswordTransformation());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {}
      };

  private final OnEditTextAttachedListener onEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached(@NonNull TextInputLayout textInputLayout) {
          EditText editText = textInputLayout.getEditText();
          textInputLayout.setEndIconVisible(true);
          textInputLayout.setEndIconCheckable(true);
          endIconView.setChecked(!hasPasswordTransformation());
          
          editText.removeTextChangedListener(textWatcher);
          editText.addTextChangedListener(textWatcher);
        }
      };
  private final OnEndIconChangedListener onEndIconChangedListener =
      new OnEndIconChangedListener() {
        @Override
        public void onEndIconChanged(@NonNull TextInputLayout textInputLayout, int previousIcon) {
          EditText editText = textInputLayout.getEditText();
          if (editText != null && previousIcon == TextInputLayout.END_ICON_PASSWORD_TOGGLE) {
            
            
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            
            editText.removeTextChangedListener(textWatcher);
          }
        }
      };

  PasswordToggleEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconDrawable(
        AppCompatResources.getDrawable(context, R.drawable.design_password_eye));
    textInputLayout.setEndIconContentDescription(
        textInputLayout.getResources().getText(R.string.password_toggle_content_description));
    textInputLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            EditText editText = textInputLayout.getEditText();
            if (editText == null) {
              return;
            }
            
            final int selection = editText.getSelectionEnd();
            if (hasPasswordTransformation()) {
              editText.setTransformationMethod(null);
            } else {
              editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            
            if (selection >= 0) {
              editText.setSelection(selection);
            }
          }
        });
    textInputLayout.addOnEditTextAttachedListener(onEditTextAttachedListener);
    textInputLayout.addOnEndIconChangedListener(onEndIconChangedListener);
    EditText editText = textInputLayout.getEditText();
    if (isInputTypePassword(editText)) {
      
      editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
  }

  private boolean hasPasswordTransformation() {
    EditText editText = textInputLayout.getEditText();
    return editText != null
        && editText.getTransformationMethod() instanceof PasswordTransformationMethod;
  }

  private static boolean isInputTypePassword(EditText editText) {
    return editText != null
        && (editText.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
  }
}
