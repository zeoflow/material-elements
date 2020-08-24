

package com.zeoflow.material.elements.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;


@RestrictTo(LIBRARY_GROUP)
public class ViewUtils {

  private ViewUtils() {}

  public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
    switch (value) {
      case 3:
        return PorterDuff.Mode.SRC_OVER;
      case 5:
        return PorterDuff.Mode.SRC_IN;
      case 9:
        return PorterDuff.Mode.SRC_ATOP;
      case 14:
        return PorterDuff.Mode.MULTIPLY;
      case 15:
        return PorterDuff.Mode.SCREEN;
      case 16:
        return PorterDuff.Mode.ADD;
      default:
        return defaultMode;
    }
  }

  public static boolean isLayoutRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  public static float dpToPx(@NonNull Context context, @Dimension(unit = Dimension.DP) int dp) {
    Resources r = context.getResources();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
  }

  public static void requestFocusAndShowKeyboard(@NonNull final View view) {
    view.requestFocus();
    view.post(
        new Runnable() {
          @Override
          public void run() {
            InputMethodManager inputMethodManager =
                (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
          }
        });
  }

  
  public interface OnApplyWindowInsetsListener {

    
    WindowInsetsCompat onApplyWindowInsets(
        View view, WindowInsetsCompat insets, RelativePadding initialPadding);
  }

  
  public static class RelativePadding {
    public int start;
    public int top;
    public int end;
    public int bottom;

    public RelativePadding(int start, int top, int end, int bottom) {
      this.start = start;
      this.top = top;
      this.end = end;
      this.bottom = bottom;
    }

    public RelativePadding(@NonNull RelativePadding other) {
      this.start = other.start;
      this.top = other.top;
      this.end = other.end;
      this.bottom = other.bottom;
    }

    
    public void applyToView(View view) {
      ViewCompat.setPaddingRelative(view, start, top, end, bottom);
    }
  }

  
  public static void doOnApplyWindowInsets(
      @NonNull View view, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    doOnApplyWindowInsets(view, attrs, defStyleAttr, defStyleRes, null);
  }

  
  public static void doOnApplyWindowInsets(
      @NonNull View view,
      @Nullable AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes,
      @Nullable final OnApplyWindowInsetsListener listener) {
    TypedArray a =
        view.getContext()
            .obtainStyledAttributes(attrs, R.styleable.Insets, defStyleAttr, defStyleRes);

    final boolean paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingBottomSystemWindowInsets, false);
    final boolean paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingLeftSystemWindowInsets, false);
    final boolean paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.Insets_paddingRightSystemWindowInsets, false);

    a.recycle();

    doOnApplyWindowInsets(
        view,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            if (paddingBottomSystemWindowInsets) {
              initialPadding.bottom += insets.getSystemWindowInsetBottom();
            }
            boolean isRtl = isLayoutRtl(view);
            if (paddingLeftSystemWindowInsets) {
              if (isRtl) {
                initialPadding.end += insets.getSystemWindowInsetLeft();
              } else {
                initialPadding.start += insets.getSystemWindowInsetLeft();
              }
            }
            if (paddingRightSystemWindowInsets) {
              if (isRtl) {
                initialPadding.start += insets.getSystemWindowInsetRight();
              } else {
                initialPadding.end += insets.getSystemWindowInsetRight();
              }
            }
            initialPadding.applyToView(view);
            return listener != null
                ? listener.onApplyWindowInsets(view, insets, initialPadding)
                : insets;
          }
        });
  }

  
  public static void doOnApplyWindowInsets(
      @NonNull View view, @NonNull final OnApplyWindowInsetsListener listener) {
    
    final RelativePadding initialPadding =
        new RelativePadding(
            ViewCompat.getPaddingStart(view),
            view.getPaddingTop(),
            ViewCompat.getPaddingEnd(view),
            view.getPaddingBottom());
    
    
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
            return listener.onApplyWindowInsets(view, insets, new RelativePadding(initialPadding));
          }
        });
    
    requestApplyInsetsWhenAttached(view);
  }

  
  public static void requestApplyInsetsWhenAttached(@NonNull View view) {
    if (ViewCompat.isAttachedToWindow(view)) {
      
      ViewCompat.requestApplyInsets(view);
    } else {
      
      view.addOnAttachStateChangeListener(
          new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
              v.removeOnAttachStateChangeListener(this);
              ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {}
          });
    }
  }

  
  public static float getParentAbsoluteElevation(@NonNull View view) {
    float absoluteElevation = 0;
    ViewParent viewParent = view.getParent();
    while (viewParent instanceof View) {
      absoluteElevation += ViewCompat.getElevation((View) viewParent);
      viewParent = viewParent.getParent();
    }
    return absoluteElevation;
  }

  
  @Nullable
  public static ViewOverlayImpl getOverlay(@Nullable View view) {
    if (view == null) {
      return null;
    }
    if (Build.VERSION.SDK_INT >= 18) {
      return new ViewOverlayApi18(view);
    }
    return ViewOverlayApi14.createFrom(view);
  }

  
  @Nullable
  public static ViewGroup getContentView(@Nullable View view) {
    if (view == null) {
      return null;
    }

    View rootView = view.getRootView();
    ViewGroup contentView = rootView.findViewById(android.R.id.content);
    if (contentView != null) {
      return contentView;
    }

    
    
    
    
    if (rootView != view && rootView instanceof ViewGroup) {
      return (ViewGroup) rootView;
    }

    return null;
  }

  
  @Nullable
  public static ViewOverlayImpl getContentViewOverlay(@NonNull View view) {
    return getOverlay(getContentView(view));
  }
}
