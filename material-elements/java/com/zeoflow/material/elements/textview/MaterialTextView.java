

package com.zeoflow.material.elements.textview;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import com.zeoflow.material.elements.resources.MaterialAttributes;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialTextView extends AppCompatTextView {

  public MaterialTextView(@NonNull Context context) {
    this(context, null );
  }

  public MaterialTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, android.R.attr.textViewStyle);
  }

  public MaterialTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public MaterialTextView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    
    context = getContext();

    if (canApplyTextAppearanceLineHeight(context)) {
      final Resources.Theme theme = context.getTheme();

      if (!viewAttrsHasLineHeight(context, theme, attrs, defStyleAttr, defStyleRes)) {
        int resId = findViewAppearanceResourceId(theme, attrs, defStyleAttr, defStyleRes);
        if (resId != -1) {
          applyLineHeightFromViewAppearance(theme, resId);
        }
      }
    }
  }

  @Override
  public void setTextAppearance(@NonNull Context context, int resId) {
    super.setTextAppearance(context, resId);

    if (canApplyTextAppearanceLineHeight(context)) {
      applyLineHeightFromViewAppearance(context.getTheme(), resId);
    }
  }

  private void applyLineHeightFromViewAppearance(@NonNull Theme theme, int resId) {
    TypedArray attributes = theme.obtainStyledAttributes(resId, R.styleable.MaterialTextAppearance);
    int lineHeight =
        readFirstAvailableDimension(
            getContext(),
            attributes,
            R.styleable.MaterialTextAppearance_android_lineHeight,
            R.styleable.MaterialTextAppearance_lineHeight);
    attributes.recycle();

    if (lineHeight >= 0) {
      setLineHeight(lineHeight);
    }
  }

  private static boolean canApplyTextAppearanceLineHeight(Context context) {
    return MaterialAttributes.resolveBoolean(context, R.attr.textAppearanceLineHeightEnabled, true);
  }

  private static int readFirstAvailableDimension(
      @NonNull Context context,
      @NonNull TypedArray attributes,
      @NonNull @StyleableRes int... indices) {
    int lineHeight = -1;

    for (int index = 0; index < indices.length && lineHeight < 0; ++index) {
      lineHeight = MaterialResources.getDimensionPixelSize(context, attributes, indices[index], -1);
    }

    return lineHeight;
  }

  private static boolean viewAttrsHasLineHeight(
      @NonNull Context context,
      @NonNull Theme theme,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int lineHeight =
        readFirstAvailableDimension(
            context,
            attributes,
            R.styleable.MaterialTextView_android_lineHeight,
            R.styleable.MaterialTextView_lineHeight);
    attributes.recycle();

    return lineHeight != -1;
  }

  private static int findViewAppearanceResourceId(
      @NonNull Theme theme, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray attributes =
        theme.obtainStyledAttributes(
            attrs, R.styleable.MaterialTextView, defStyleAttr, defStyleRes);
    int appearanceAttrId =
        attributes.getResourceId(R.styleable.MaterialTextView_android_textAppearance, -1);
    attributes.recycle();
    return appearanceAttrId;
  }
}
