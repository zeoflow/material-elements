

package com.zeoflow.material.elements.appbar;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialToolbar extends Toolbar {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Toolbar;

  public MaterialToolbar(@NonNull Context context) {
    this(context, null);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.toolbarStyle);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    initBackground(context);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  private void initBackground(Context context) {
    Drawable background = getBackground();
    if (background != null && !(background instanceof ColorDrawable)) {
      return;
    }
    MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    int backgroundColor =
        background != null ? ((ColorDrawable) background).getColor() : Color.TRANSPARENT;
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(backgroundColor));
    materialShapeDrawable.initializeElevationOverlay(context);
    materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
    ViewCompat.setBackground(this, materialShapeDrawable);
  }
}
