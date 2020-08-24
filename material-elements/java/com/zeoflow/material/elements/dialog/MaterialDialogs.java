
package com.zeoflow.material.elements.dialog;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.view.ViewCompat;
import com.zeoflow.material.elements.internal.ThemeEnforcement;


@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDialogs {

  private MaterialDialogs() {};

  @NonNull
  public static InsetDrawable insetDrawable(
      @Nullable Drawable drawable, @NonNull Rect backgroundInsets) {
    return new InsetDrawable(
        drawable,
        backgroundInsets.left,
        backgroundInsets.top,
        backgroundInsets.right,
        backgroundInsets.bottom);
  }

  @NonNull
  public static Rect getDialogBackgroundInsets(
      @NonNull Context context, @AttrRes int defaultStyleAttribute, int defaultStyleResource) {
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            null,
            R.styleable.MaterialAlertDialog,
            defaultStyleAttribute,
            defaultStyleResource);

    int backgroundInsetStart =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetStart,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_start));
    int backgroundInsetTop =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetTop,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_top));

    int backgroundInsetEnd =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetEnd,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_end));
    int backgroundInsetBottom =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialAlertDialog_backgroundInsetBottom,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_alert_dialog_background_inset_bottom));

    attributes.recycle();

    int backgroundInsetLeft = backgroundInsetStart;
    int backgroundInsetRight = backgroundInsetEnd;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      int layoutDirection = context.getResources().getConfiguration().getLayoutDirection();
      if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
        backgroundInsetLeft = backgroundInsetEnd;
        backgroundInsetRight = backgroundInsetStart;
      }
    }

    return new Rect(
        backgroundInsetLeft, backgroundInsetTop, backgroundInsetRight, backgroundInsetBottom);
  }
}
