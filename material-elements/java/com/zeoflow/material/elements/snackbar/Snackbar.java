

package com.zeoflow.material.elements.snackbar;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_ICONS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


public class Snackbar extends BaseTransientBottomBar<Snackbar> {

  @Nullable private final AccessibilityManager accessibilityManager;
  private boolean hasAction;

  private static final int[] SNACKBAR_BUTTON_STYLE_ATTR = new int[] {R.attr.snackbarButtonStyle};
  private static final int[] SNACKBAR_CONTENT_STYLE_ATTRS =
      new int[] {R.attr.snackbarButtonStyle, R.attr.snackbarTextViewStyle};

  
  public static class Callback extends BaseCallback<Snackbar> {
    
    public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;
    
    public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;
    
    public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;
    
    public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;
    
    public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

    @Override
    public void onShown(Snackbar sb) {
      
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event) {
      
    }
  }

  @Nullable private BaseCallback<Snackbar> callback;

  private Snackbar(
      @NonNull ViewGroup parent,
      @NonNull View content,
      @NonNull com.zeoflow.material.elements.snackbar.ContentViewCallback contentViewCallback) {
    super(parent, content, contentViewCallback);
    accessibilityManager =
        (AccessibilityManager) parent.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  
  
  @Override
  public void show() {
    super.show();
  }

  
  
  @Override
  public void dismiss() {
    super.dismiss();
  }

  
  
  @Override
  public boolean isShown() {
    return super.isShown();
  }

  
  @NonNull
  public static Snackbar make(
      @NonNull View view, @NonNull CharSequence text, @Duration int duration) {
    final ViewGroup parent = findSuitableParent(view);
    if (parent == null) {
      throw new IllegalArgumentException(
          "No suitable parent found from the given view. Please provide a valid view.");
    }

    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final SnackbarContentLayout content =
        (SnackbarContentLayout)
            inflater.inflate(
                hasSnackbarContentStyleAttrs(parent.getContext())
                    ? R.layout.mtrl_layout_snackbar_include
                    : R.layout.design_layout_snackbar_include,
                parent,
                false);
    final Snackbar snackbar = new Snackbar(parent, content, content);
    snackbar.setText(text);
    snackbar.setDuration(duration);
    return snackbar;
  }

  
  @Deprecated
  protected static boolean hasSnackbarButtonStyleAttr(@NonNull Context context) {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_BUTTON_STYLE_ATTR);
    int snackbarButtonStyleResId = a.getResourceId(0, -1);
    a.recycle();
    return snackbarButtonStyleResId != -1;
  }

  private static boolean hasSnackbarContentStyleAttrs(@NonNull Context context) {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_CONTENT_STYLE_ATTRS);
    int snackbarButtonStyleResId = a.getResourceId(0, -1);
    int snackbarTextViewStyleResId = a.getResourceId(1, -1);
    a.recycle();
    return snackbarButtonStyleResId != -1 && snackbarTextViewStyleResId != -1;
  }

  
  @NonNull
  public static Snackbar make(@NonNull View view, @StringRes int resId, @Duration int duration) {
    return make(view, view.getResources().getText(resId), duration);
  }

  @Nullable
  private static ViewGroup findSuitableParent(View view) {
    ViewGroup fallback = null;
    do {
      if (view instanceof CoordinatorLayout) {
        
        return (ViewGroup) view;
      } else if (view instanceof FrameLayout) {
        if (view.getId() == android.R.id.content) {
          
          
          return (ViewGroup) view;
        } else {
          
          fallback = (ViewGroup) view;
        }
      }

      if (view != null) {
        
        final ViewParent parent = view.getParent();
        view = parent instanceof View ? (View) parent : null;
      }
    } while (view != null);

    
    return fallback;
  }

  
  @NonNull
  public Snackbar setText(@NonNull CharSequence message) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setText(message);
    return this;
  }

  
  @NonNull
  public Snackbar setText(@StringRes int resId) {
    return setText(getContext().getText(resId));
  }

  
  @NonNull
  public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
    return setAction(getContext().getText(resId), listener);
  }

  
  @NonNull
  public Snackbar setAction(
      @Nullable CharSequence text, @Nullable final View.OnClickListener listener) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) this.view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    if (TextUtils.isEmpty(text) || listener == null) {
      tv.setVisibility(View.GONE);
      tv.setOnClickListener(null);
      hasAction = false;
    } else {
      hasAction = true;
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
      tv.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              listener.onClick(view);
              
              dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
            }
          });
    }
    return this;
  }

  @Override
  @Duration
  public int getDuration() {
    int userSetDuration = super.getDuration();
    if (userSetDuration == LENGTH_INDEFINITE) {
      return LENGTH_INDEFINITE;
    }

    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      int controlsFlag = hasAction ? FLAG_CONTENT_CONTROLS : 0;
      return accessibilityManager.getRecommendedTimeoutMillis(
          userSetDuration, controlsFlag | FLAG_CONTENT_ICONS | FLAG_CONTENT_TEXT);
    }

    
    return hasAction && accessibilityManager.isTouchExplorationEnabled()
        ? LENGTH_INDEFINITE
        : userSetDuration;
  }

  
  @NonNull
  public Snackbar setTextColor(ColorStateList colors) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setTextColor(colors);
    return this;
  }

  
  @NonNull
  public Snackbar setTextColor(@ColorInt int color) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getMessageView();
    tv.setTextColor(color);
    return this;
  }

  
  @NonNull
  public Snackbar setActionTextColor(ColorStateList colors) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(colors);
    return this;
  }

  
  @NonNull
  public Snackbar setMaxInlineActionWidth(@Dimension int width) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    contentLayout.setMaxInlineActionWidth(width);
    return this;
  }

  
  @NonNull
  public Snackbar setActionTextColor(@ColorInt int color) {
    final SnackbarContentLayout contentLayout = (SnackbarContentLayout) view.getChildAt(0);
    final TextView tv = contentLayout.getActionView();
    tv.setTextColor(color);
    return this;
  }

  
  @NonNull
  public Snackbar setBackgroundTint(@ColorInt int color) {
    return setBackgroundTintList(ColorStateList.valueOf(color));
  }

  
  @NonNull
  public Snackbar setBackgroundTintList(@Nullable ColorStateList colorStateList) {
    view.setBackgroundTintList(colorStateList);
    return this;
  }

  @NonNull
  public Snackbar setBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
    view.setBackgroundTintMode(mode);
    return this;
  }

  
  @Deprecated
  @NonNull
  public Snackbar setCallback(@Nullable Callback callback) {
    
    
    if (this.callback != null) {
      removeCallback(this.callback);
    }
    if (callback != null) {
      addCallback(callback);
    }
    
    
    this.callback = callback;
    return this;
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public static final class SnackbarLayout extends BaseTransientBottomBar.SnackbarBaseLayout {
    public SnackbarLayout(Context context) {
      super(context);
    }

    public SnackbarLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      
      
      
      
      int childCount = getChildCount();
      int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
          child.measure(
              MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
              MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
      }
    }
  }
}
