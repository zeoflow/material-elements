

package com.zeoflow.material.elements.textfield;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.zeoflow.material.elements.internal.ManufacturerUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class TextInputEditText extends AppCompatEditText {

  private final Rect parentRect = new Rect();
  private boolean textInputLayoutFocusedRectEnabled;

  public TextInputEditText(@NonNull Context context) {
    this(context, null);
  }

  public TextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.editTextStyle);
  }

  public TextInputEditText(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, 0), attrs, defStyleAttr);
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.TextInputEditText,
            defStyleAttr,
            R.style.Widget_Design_TextInputEditText);

    setTextInputLayoutFocusedRectEnabled(
        attributes.getBoolean(R.styleable.TextInputEditText_textInputLayoutFocusedRectEnabled, false));

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    
    
    
    TextInputLayout layout = getTextInputLayout();
    if (layout != null
        && layout.isProvidingHint()
        && super.getHint() == null
        && ManufacturerUtils.isMeizuDevice()) {
      setHint("");
    }
  }

  @Nullable
  @Override
  public CharSequence getHint() {
    
    
    TextInputLayout layout = getTextInputLayout();
    if (layout != null && layout.isProvidingHint()) {
      return layout.getHint();
    }
    return super.getHint();
  }

  @Nullable
  @Override
  public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
    final InputConnection ic = super.onCreateInputConnection(outAttrs);
    if (ic != null && outAttrs.hintText == null) {
      
      
      outAttrs.hintText = getHintFromLayout();
    }
    return ic;
  }

  @Nullable
  private TextInputLayout getTextInputLayout() {
    ViewParent parent = getParent();
    while (parent instanceof View) {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  @Nullable
  private CharSequence getHintFromLayout() {
    TextInputLayout layout = getTextInputLayout();
    return (layout != null) ? layout.getHint() : null;
  }

  
  public void setTextInputLayoutFocusedRectEnabled(boolean textInputLayoutFocusedRectEnabled) {
    this.textInputLayoutFocusedRectEnabled = textInputLayoutFocusedRectEnabled;
  }

  
  public boolean isTextInputLayoutFocusedRectEnabled() {
    return textInputLayoutFocusedRectEnabled;
  }

  @Override
  public void getFocusedRect(@Nullable Rect r) {
    super.getFocusedRect(r);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null
        && textInputLayoutFocusedRectEnabled
        && r != null) {
      textInputLayout.getFocusedRect(parentRect);
      r.bottom = parentRect.bottom;
    }
  }

  @Override
  public boolean getGlobalVisibleRect(@Nullable Rect r, @Nullable Point globalOffset) {
    boolean result = super.getGlobalVisibleRect(r, globalOffset);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null
        && textInputLayoutFocusedRectEnabled
        && r != null) {
      textInputLayout.getGlobalVisibleRect(parentRect, globalOffset);
      r.bottom = parentRect.bottom;
    }
    return result;
  }

  @Override
  public boolean requestRectangleOnScreen(@Nullable Rect rectangle) {
    boolean result = super.requestRectangleOnScreen(rectangle);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null && textInputLayoutFocusedRectEnabled) {
      parentRect.set(
          0,
          textInputLayout.getHeight()
              - getResources().getDimensionPixelOffset(R.dimen.mtrl_edittext_rectangle_top_offset),
          textInputLayout.getWidth(),
          textInputLayout.getHeight());
      textInputLayout.requestRectangleOnScreen(parentRect, true);
    }
     return result;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    TextInputLayout layout = getTextInputLayout();

    
    
    if (VERSION.SDK_INT < 23 && layout != null) {
      info.setText(getAccessibilityNodeInfoText(layout));
    }
  }

  @NonNull
  private String getAccessibilityNodeInfoText(@NonNull TextInputLayout layout) {
    CharSequence inputText = getText();
    CharSequence hintText = layout.getHint();
    CharSequence helperText = layout.getHelperText();
    CharSequence errorText = layout.getError();
    boolean showingText = !TextUtils.isEmpty(inputText);
    boolean hasHint = !TextUtils.isEmpty(hintText);
    boolean hasHelperText = !TextUtils.isEmpty(helperText);
    boolean showingError = !TextUtils.isEmpty(errorText);

    String hint = hasHint ? hintText.toString() : "";
    hint += ((showingError || hasHelperText) && !TextUtils.isEmpty(hint)) ? ", " : "";
    hint += showingError ? errorText : (hasHelperText ? helperText : "");

    if (showingText) {
      return inputText + (!TextUtils.isEmpty(hint) ? (", " + hint) : "");
    } else if (!TextUtils.isEmpty(hint)) {
      return hint;
    } else {
      return "";
    }
  }
}
