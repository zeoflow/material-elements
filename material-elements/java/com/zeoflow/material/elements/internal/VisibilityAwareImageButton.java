

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.RestrictTo;
import android.util.AttributeSet;
import android.widget.ImageButton;


@RestrictTo(LIBRARY_GROUP)
@SuppressLint("AppCompatCustomView")
public class VisibilityAwareImageButton extends ImageButton {

  private int userSetVisibility;

  public VisibilityAwareImageButton(Context context) {
    this(context, null);
  }

  public VisibilityAwareImageButton(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VisibilityAwareImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    userSetVisibility = getVisibility();
  }

  @Override
  public void setVisibility(int visibility) {
    internalSetVisibility(visibility, true);
  }

  public final void internalSetVisibility(int visibility, boolean fromUser) {
    super.setVisibility(visibility);
    if (fromUser) {
      userSetVisibility = visibility;
    }
  }

  public final int getUserSetVisibility() {
    return userSetVisibility;
  }
}
