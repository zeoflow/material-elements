

package com.zeoflow.material.elements.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


@RestrictTo(LIBRARY_GROUP)
public class ScrimInsetsFrameLayout extends FrameLayout {

  @Nullable Drawable insetForeground;

  Rect insets;

  private Rect tempRect = new Rect();
  private boolean drawTopInsetForeground = true;
  private boolean drawBottomInsetForeground = true;

  public ScrimInsetsFrameLayout(@NonNull Context context) {
    this(context, null);
  }

  public ScrimInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScrimInsetsFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.ScrimInsetsFrameLayout,
            defStyleAttr,
            R.style.Widget_Design_ScrimInsetsFrameLayout);
    insetForeground = a.getDrawable(R.styleable.ScrimInsetsFrameLayout_insetForeground);
    a.recycle();
    setWillNotDraw(true); 

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            if (null == ScrimInsetsFrameLayout.this.insets) {
              ScrimInsetsFrameLayout.this.insets = new Rect();
            }
            ScrimInsetsFrameLayout.this.insets.set(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom());
            onInsetsChanged(insets);
            setWillNotDraw(!insets.hasSystemWindowInsets() || insetForeground == null);
            ViewCompat.postInvalidateOnAnimation(ScrimInsetsFrameLayout.this);
            return insets.consumeSystemWindowInsets();
          }
        });
  }

  
  public void setScrimInsetForeground(@Nullable Drawable drawable) {
    insetForeground = drawable;
  }

  public void setDrawTopInsetForeground(boolean drawTopInsetForeground) {
    this.drawTopInsetForeground = drawTopInsetForeground;
  }

  public void setDrawBottomInsetForeground(boolean drawBottomInsetForeground) {
    this.drawBottomInsetForeground = drawBottomInsetForeground;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    int width = getWidth();
    int height = getHeight();
    if (insets != null && insetForeground != null) {
      int sc = canvas.save();
      canvas.translate(getScrollX(), getScrollY());

      
      if (drawTopInsetForeground) {
        tempRect.set(0, 0, width, insets.top);
        insetForeground.setBounds(tempRect);
        insetForeground.draw(canvas);
      }

      
      if (drawBottomInsetForeground) {
        tempRect.set(0, height - insets.bottom, width, height);
        insetForeground.setBounds(tempRect);
        insetForeground.draw(canvas);
      }

      
      tempRect.set(0, insets.top, insets.left, height - insets.bottom);
      insetForeground.setBounds(tempRect);
      insetForeground.draw(canvas);

      
      tempRect.set(width - insets.right, insets.top, width, height - insets.bottom);
      insetForeground.setBounds(tempRect);
      insetForeground.draw(canvas);

      canvas.restoreToCount(sc);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (insetForeground != null) {
      insetForeground.setCallback(this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (insetForeground != null) {
      insetForeground.setCallback(null);
    }
  }

  protected void onInsetsChanged(WindowInsetsCompat insets) {}
}
