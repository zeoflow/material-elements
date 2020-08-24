

package com.zeoflow.material.elements.snackbar;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.zeoflow.material.elements.behavior.SwipeDismissBehavior;
import com.zeoflow.material.elements.color.MaterialColors;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseTransientBottomBar<B extends BaseTransientBottomBar<B>> {

  
  public static final int ANIMATION_MODE_SLIDE = 0;

  
  public static final int ANIMATION_MODE_FADE = 1;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({ANIMATION_MODE_SLIDE, ANIMATION_MODE_FADE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface AnimationMode {}

  
  public abstract static class BaseCallback<B> {
    
    public static final int DISMISS_EVENT_SWIPE = 0;
    
    public static final int DISMISS_EVENT_ACTION = 1;
    
    public static final int DISMISS_EVENT_TIMEOUT = 2;
    
    public static final int DISMISS_EVENT_MANUAL = 3;
    
    public static final int DISMISS_EVENT_CONSECUTIVE = 4;

    
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({
      DISMISS_EVENT_SWIPE,
      DISMISS_EVENT_ACTION,
      DISMISS_EVENT_TIMEOUT,
      DISMISS_EVENT_MANUAL,
      DISMISS_EVENT_CONSECUTIVE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DismissEvent {}

    
    public void onDismissed(B transientBottomBar, @DismissEvent int event) {
      
    }

    
    public void onShown(B transientBottomBar) {
      
    }
  }

  
  @Deprecated
  public interface ContentViewCallback
      extends com.zeoflow.material.elements.snackbar.ContentViewCallback
  {}

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
  @IntRange(from = 1)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Duration {}

  
  public static final int LENGTH_INDEFINITE = -2;

  
  public static final int LENGTH_SHORT = -1;

  
  public static final int LENGTH_LONG = 0;

  
  static final int ANIMATION_DURATION = 250;
  
  static final int ANIMATION_FADE_DURATION = 180;

  
  private static final int ANIMATION_FADE_IN_DURATION = 150;
  private static final int ANIMATION_FADE_OUT_DURATION = 75;
  private static final float ANIMATION_SCALE_FROM_VALUE = 0.8f;

  @NonNull static final Handler handler;
  static final int MSG_SHOW = 0;
  static final int MSG_DISMISS = 1;

  
  
  
  private static final boolean USE_OFFSET_API =
      (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
          && (Build.VERSION.SDK_INT <= VERSION_CODES.KITKAT);

  private static final int[] SNACKBAR_STYLE_ATTR = new int[] {R.attr.snackbarStyle};

  private static final String TAG = BaseTransientBottomBar.class.getSimpleName();

  static {
    handler =
        new Handler(
            Looper.getMainLooper(),
            new Handler.Callback() {
              @Override
              public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                  case MSG_SHOW:
                    ((BaseTransientBottomBar) message.obj).showView();
                    return true;
                  case MSG_DISMISS:
                    ((BaseTransientBottomBar) message.obj).hideView(message.arg1);
                    return true;
                  default:
                    return false;
                }
              }
            });
  }

  @NonNull private final ViewGroup targetParent;
  private final Context context;
  @NonNull protected final SnackbarBaseLayout view;

  @NonNull
  private final com.zeoflow.material.elements.snackbar.ContentViewCallback contentViewCallback;

  private int duration;
  private boolean gestureInsetBottomIgnored;
  @Nullable private View anchorView;

  @RequiresApi(VERSION_CODES.Q)
  private final Runnable bottomMarginGestureInsetRunnable =
      new Runnable() {
        @Override
        public void run() {
          if (view == null || context == null) {
            return;
          }
          
          
          int currentInsetBottom =
              getScreenHeight() - getViewAbsoluteBottom() + (int) view.getTranslationY();
          if (currentInsetBottom >= extraBottomMarginGestureInset) {
            
            return;
          }

          LayoutParams layoutParams = view.getLayoutParams();
          if (!(layoutParams instanceof MarginLayoutParams)) {
            Log.w(
                TAG,
                "Unable to apply gesture inset because layout params are not MarginLayoutParams");
            return;
          }

          
          MarginLayoutParams marginParams = (MarginLayoutParams) layoutParams;
          marginParams.bottomMargin += extraBottomMarginGestureInset - currentInsetBottom;
          view.requestLayout();
        }
      };

  @Nullable private Rect originalMargins;
  private int extraBottomMarginWindowInset;
  private int extraLeftMarginWindowInset;
  private int extraRightMarginWindowInset;
  private int extraBottomMarginGestureInset;
  private int extraBottomMarginAnchorView;

  private List<BaseCallback<B>> callbacks;

  private BaseTransientBottomBar.Behavior behavior;

  @Nullable private final AccessibilityManager accessibilityManager;

  
  
  @RestrictTo(LIBRARY_GROUP)
  protected interface OnLayoutChangeListener {
    void onLayoutChange(View view, int left, int top, int right, int bottom);
  }

  
  
  @RestrictTo(LIBRARY_GROUP)
  protected interface OnAttachStateChangeListener {
    void onViewAttachedToWindow(View v);

    void onViewDetachedFromWindow(View v);
  }

  
  protected BaseTransientBottomBar(
      @NonNull ViewGroup parent,
      @NonNull View content,
      @NonNull com.zeoflow.material.elements.snackbar.ContentViewCallback contentViewCallback) {
    if (parent == null) {
      throw new IllegalArgumentException("Transient bottom bar must have non-null parent");
    }
    if (content == null) {
      throw new IllegalArgumentException("Transient bottom bar must have non-null content");
    }
    if (contentViewCallback == null) {
      throw new IllegalArgumentException("Transient bottom bar must have non-null callback");
    }

    targetParent = parent;
    this.contentViewCallback = contentViewCallback;
    context = parent.getContext();

    ThemeEnforcement.checkAppCompatTheme(context);

    LayoutInflater inflater = LayoutInflater.from(context);
    
    
    
    view = (SnackbarBaseLayout) inflater.inflate(getSnackbarBaseLayoutResId(), targetParent, false);
    if (content instanceof SnackbarContentLayout) {
      ((SnackbarContentLayout) content)
          .updateActionTextColorAlphaIfNeeded(view.getActionTextColorAlpha());
    }
    view.addView(content);

    LayoutParams layoutParams = view.getLayoutParams();
    if (layoutParams instanceof MarginLayoutParams) {
      MarginLayoutParams marginParams = (MarginLayoutParams) layoutParams;
      originalMargins =
          new Rect(
              marginParams.leftMargin,
              marginParams.topMargin,
              marginParams.rightMargin,
              marginParams.bottomMargin);
    }

    ViewCompat.setAccessibilityLiveRegion(view, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
    ViewCompat.setImportantForAccessibility(view, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

    
    ViewCompat.setFitsSystemWindows(view, true);
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        new OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            
            extraBottomMarginWindowInset = insets.getSystemWindowInsetBottom();
            extraLeftMarginWindowInset = insets.getSystemWindowInsetLeft();
            extraRightMarginWindowInset = insets.getSystemWindowInsetRight();
            updateMargins();
            return insets;
          }
        });

    
    ViewCompat.setAccessibilityDelegate(
        view,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS);
            info.setDismissable(true);
          }

          @Override
          public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS) {
              dismiss();
              return true;
            }
            return super.performAccessibilityAction(host, action, args);
          }
        });

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  private void updateMargins() {
    LayoutParams layoutParams = view.getLayoutParams();
    if (!(layoutParams instanceof MarginLayoutParams) || originalMargins == null) {
      Log.w(TAG, "Unable to update margins because layout params are not MarginLayoutParams");
      return;
    }

    int extraBottomMargin =
        anchorView != null ? extraBottomMarginAnchorView : extraBottomMarginWindowInset;
    MarginLayoutParams marginParams = (MarginLayoutParams) layoutParams;
    marginParams.bottomMargin = originalMargins.bottom + extraBottomMargin;
    marginParams.leftMargin = originalMargins.left + extraLeftMarginWindowInset;
    marginParams.rightMargin = originalMargins.right + extraRightMarginWindowInset;
    view.requestLayout();

    if (VERSION.SDK_INT >= VERSION_CODES.Q && shouldUpdateGestureInset()) {
      
      view.removeCallbacks(bottomMarginGestureInsetRunnable);
      view.post(bottomMarginGestureInsetRunnable);
    }
  }

  private boolean shouldUpdateGestureInset() {
    return extraBottomMarginGestureInset > 0 && !gestureInsetBottomIgnored && isSwipeDismissable();
  }

  private boolean isSwipeDismissable() {
    LayoutParams layoutParams = view.getLayoutParams();
    return layoutParams instanceof CoordinatorLayout.LayoutParams
        && ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior()
            instanceof SwipeDismissBehavior;
  }

  @LayoutRes
  protected int getSnackbarBaseLayoutResId() {
    return hasSnackbarStyleAttr() ? R.layout.mtrl_layout_snackbar : R.layout.design_layout_snackbar;
  }

  
  protected boolean hasSnackbarStyleAttr() {
    TypedArray a = context.obtainStyledAttributes(SNACKBAR_STYLE_ATTR);
    int snackbarStyleResId = a.getResourceId(0, -1);
    a.recycle();
    return snackbarStyleResId != -1;
  }

  
  @NonNull
  public B setDuration(@Duration int duration) {
    this.duration = duration;
    return (B) this;
  }

  
  @Duration
  public int getDuration() {
    return duration;
  }

  
  @NonNull
  public B setGestureInsetBottomIgnored(boolean gestureInsetBottomIgnored) {
    this.gestureInsetBottomIgnored = gestureInsetBottomIgnored;
    return (B) this;
  }

  
  public boolean isGestureInsetBottomIgnored() {
    return gestureInsetBottomIgnored;
  }

  
  @AnimationMode
  public int getAnimationMode() {
    return view.getAnimationMode();
  }

  
  @NonNull
  public B setAnimationMode(@AnimationMode int animationMode) {
    view.setAnimationMode(animationMode);
    return (B) this;
  }

  
  @Nullable
  public View getAnchorView() {
    return anchorView;
  }

  
  @NonNull
  public B setAnchorView(@Nullable View anchorView) {
    this.anchorView = anchorView;
    return (B) this;
  }

  
  @NonNull
  public B setAnchorView(@IdRes int anchorViewId) {
    this.anchorView = targetParent.findViewById(anchorViewId);
    if (this.anchorView == null) {
      throw new IllegalArgumentException("Unable to find anchor view with id: " + anchorViewId);
    }
    return (B) this;
  }

  
  @NonNull
  public B setBehavior(BaseTransientBottomBar.Behavior behavior) {
    this.behavior = behavior;
    return (B) this;
  }

  
  public BaseTransientBottomBar.Behavior getBehavior() {
    return behavior;
  }

  
  @NonNull
  public Context getContext() {
    return context;
  }

  
  @NonNull
  public View getView() {
    return view;
  }

  
  public void show() {
    SnackbarManager.getInstance().show(getDuration(), managerCallback);
  }

  
  public void dismiss() {
    dispatchDismiss(BaseCallback.DISMISS_EVENT_MANUAL);
  }

  protected void dispatchDismiss(@BaseCallback.DismissEvent int event) {
    SnackbarManager.getInstance().dismiss(managerCallback, event);
  }

  
  @NonNull
  public B addCallback(@Nullable BaseCallback<B> callback) {
    if (callback == null) {
      return (B) this;
    }
    if (callbacks == null) {
      callbacks = new ArrayList<BaseCallback<B>>();
    }
    callbacks.add(callback);
    return (B) this;
  }

  
  @NonNull
  public B removeCallback(@Nullable BaseCallback<B> callback) {
    if (callback == null) {
      return (B) this;
    }
    if (callbacks == null) {
      
      return (B) this;
    }
    callbacks.remove(callback);
    return (B) this;
  }

  
  public boolean isShown() {
    return SnackbarManager.getInstance().isCurrent(managerCallback);
  }

  
  public boolean isShownOrQueued() {
    return SnackbarManager.getInstance().isCurrentOrNext(managerCallback);
  }

  @NonNull
  SnackbarManager.Callback managerCallback =
      new SnackbarManager.Callback() {
        @Override
        public void show() {
          handler.sendMessage(handler.obtainMessage(MSG_SHOW, BaseTransientBottomBar.this));
        }

        @Override
        public void dismiss(int event) {
          handler.sendMessage(
              handler.obtainMessage(MSG_DISMISS, event, 0, BaseTransientBottomBar.this));
        }
      };

  @NonNull
  protected SwipeDismissBehavior<? extends View> getNewBehavior() {
    return new Behavior();
  }

  final void showView() {
    this.view.setOnAttachStateChangeListener(
        new BaseTransientBottomBar.OnAttachStateChangeListener() {
          @Override
          public void onViewAttachedToWindow(View v) {
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
              WindowInsets insets = view.getRootWindowInsets();
              if (insets != null) {
                extraBottomMarginGestureInset = insets.getMandatorySystemGestureInsets().bottom;
                updateMargins();
              }
            }
          }

          @Override
          public void onViewDetachedFromWindow(View v) {
            if (isShownOrQueued()) {
              
              
              
              
              handler.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      onViewHidden(BaseCallback.DISMISS_EVENT_MANUAL);
                    }
                  });
            }
          }
        });

    if (this.view.getParent() == null) {
      ViewGroup.LayoutParams lp = this.view.getLayoutParams();

      if (lp instanceof CoordinatorLayout.LayoutParams) {
        setUpBehavior((CoordinatorLayout.LayoutParams) lp);
      }

      extraBottomMarginAnchorView = calculateBottomMarginForAnchorView();
      updateMargins();

      
      
      view.setVisibility(View.INVISIBLE);
      targetParent.addView(this.view);
    }

    if (ViewCompat.isLaidOut(this.view)) {
      showViewImpl();
      return;
    }

    
    this.view.setOnLayoutChangeListener(
        new OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(View view, int left, int top, int right, int bottom) {
            BaseTransientBottomBar.this.view.setOnLayoutChangeListener(null);
            BaseTransientBottomBar.this.showViewImpl();
          }
        });
  }

  private void showViewImpl() {
    if (shouldAnimate()) {
      
      animateViewIn();
    } else {
      
      view.setVisibility(View.VISIBLE);
      onViewShown();
    }
  }

  private int getViewAbsoluteBottom() {
    int[] absoluteLocation = new int[2];
    view.getLocationOnScreen(absoluteLocation);
    return absoluteLocation[1] + view.getHeight();
  }

  @RequiresApi(VERSION_CODES.JELLY_BEAN_MR1)
  private int getScreenHeight() {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
    return displayMetrics.heightPixels;
  }

  private void setUpBehavior(CoordinatorLayout.LayoutParams lp) {
    
    CoordinatorLayout.LayoutParams clp = lp;

    SwipeDismissBehavior<? extends View> behavior =
        this.behavior == null ? getNewBehavior() : this.behavior;

    if (behavior instanceof BaseTransientBottomBar.Behavior) {
      ((Behavior) behavior).setBaseTransientBottomBar(this);
    }

    behavior.setListener(
        new SwipeDismissBehavior.OnDismissListener() {
          @Override
          public void onDismiss(@NonNull View view) {
            view.setVisibility(View.GONE);
            dispatchDismiss(BaseCallback.DISMISS_EVENT_SWIPE);
          }

          @Override
          public void onDragStateChanged(int state) {
            switch (state) {
              case SwipeDismissBehavior.STATE_DRAGGING:
              case SwipeDismissBehavior.STATE_SETTLING:
                
                SnackbarManager.getInstance().pauseTimeout(managerCallback);
                break;
              case SwipeDismissBehavior.STATE_IDLE:
                
                SnackbarManager.getInstance().restoreTimeoutIfPaused(managerCallback);
                break;
              default:
                
            }
          }
        });
    clp.setBehavior(behavior);
    
    
    if (anchorView == null) {
      clp.insetEdge = Gravity.BOTTOM;
    }
  }

  private int calculateBottomMarginForAnchorView() {
    if (anchorView == null) {
      return 0;
    }

    int[] anchorViewLocation = new int[2];
    anchorView.getLocationOnScreen(anchorViewLocation);
    int anchorViewAbsoluteYTop = anchorViewLocation[1];

    int[] targetParentLocation = new int[2];
    targetParent.getLocationOnScreen(targetParentLocation);
    int targetParentAbsoluteYBottom = targetParentLocation[1] + targetParent.getHeight();

    return targetParentAbsoluteYBottom - anchorViewAbsoluteYTop;
  }

  void animateViewIn() {
    
    view.post(
        new Runnable() {
          @Override
          public void run() {
            if (view == null) {
              return;
            }
            
            view.setVisibility(View.VISIBLE);
            if (view.getAnimationMode() == ANIMATION_MODE_FADE) {
              startFadeInAnimation();
            } else {
              startSlideInAnimation();
            }
          }
        });
  }

  private void animateViewOut(int event) {
    if (view.getAnimationMode() == ANIMATION_MODE_FADE) {
      startFadeOutAnimation(event);
    } else {
      startSlideOutAnimation(event);
    }
  }

  private void startFadeInAnimation() {
    ValueAnimator alphaAnimator = getAlphaAnimator(0, 1);
    ValueAnimator scaleAnimator = getScaleAnimator(ANIMATION_SCALE_FROM_VALUE, 1);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(alphaAnimator, scaleAnimator);
    animatorSet.setDuration(ANIMATION_FADE_IN_DURATION);
    animatorSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animator) {
            onViewShown();
          }
        });
    animatorSet.start();
  }

  private void startFadeOutAnimation(final int event) {
    ValueAnimator animator = getAlphaAnimator(1, 0);
    animator.setDuration(ANIMATION_FADE_OUT_DURATION);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animator) {
            onViewHidden(event);
          }
        });
    animator.start();
  }

  private ValueAnimator getAlphaAnimator(float... alphaValues) {
    ValueAnimator animator = ValueAnimator.ofFloat(alphaValues);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            view.setAlpha((Float) valueAnimator.getAnimatedValue());
          }
        });
    return animator;
  }

  private ValueAnimator getScaleAnimator(float... scaleValues) {
    ValueAnimator animator = ValueAnimator.ofFloat(scaleValues);
    animator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            float scale = (float) valueAnimator.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
          }
        });
    return animator;
  }

  private void startSlideInAnimation() {
    final int translationYBottom = getTranslationYBottom();
    if (USE_OFFSET_API) {
      ViewCompat.offsetTopAndBottom(view, translationYBottom);
    } else {
      view.setTranslationY(translationYBottom);
    }

    ValueAnimator animator = new ValueAnimator();
    animator.setIntValues(translationYBottom, 0);
    animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    animator.setDuration(ANIMATION_DURATION);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animator) {
            contentViewCallback.animateContentIn(
                ANIMATION_DURATION - ANIMATION_FADE_DURATION, ANIMATION_FADE_DURATION);
          }

          @Override
          public void onAnimationEnd(Animator animator) {
            onViewShown();
          }
        });
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          private int previousAnimatedIntValue = translationYBottom;

          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            int currentAnimatedIntValue = (int) animator.getAnimatedValue();
            if (USE_OFFSET_API) {
              
              
              ViewCompat.offsetTopAndBottom(
                  view, currentAnimatedIntValue - previousAnimatedIntValue);
            } else {
              view.setTranslationY(currentAnimatedIntValue);
            }
            previousAnimatedIntValue = currentAnimatedIntValue;
          }
        });
    animator.start();
  }

  private void startSlideOutAnimation(final int event) {
    ValueAnimator animator = new ValueAnimator();
    animator.setIntValues(0, getTranslationYBottom());
    animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
    animator.setDuration(ANIMATION_DURATION);
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animator) {
            contentViewCallback.animateContentOut(0, ANIMATION_FADE_DURATION);
          }

          @Override
          public void onAnimationEnd(Animator animator) {
            onViewHidden(event);
          }
        });
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          private int previousAnimatedIntValue = 0;

          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            int currentAnimatedIntValue = (int) animator.getAnimatedValue();
            if (USE_OFFSET_API) {
              
              
              ViewCompat.offsetTopAndBottom(
                  view, currentAnimatedIntValue - previousAnimatedIntValue);
            } else {
              view.setTranslationY(currentAnimatedIntValue);
            }
            previousAnimatedIntValue = currentAnimatedIntValue;
          }
        });
    animator.start();
  }

  private int getTranslationYBottom() {
    int translationY = view.getHeight();
    LayoutParams layoutParams = view.getLayoutParams();
    if (layoutParams instanceof MarginLayoutParams) {
      translationY += ((MarginLayoutParams) layoutParams).bottomMargin;
    }
    return translationY;
  }

  final void hideView(@BaseCallback.DismissEvent int event) {
    if (shouldAnimate() && view.getVisibility() == View.VISIBLE) {
      animateViewOut(event);
    } else {
      
      onViewHidden(event);
    }
  }

  void onViewShown() {
    SnackbarManager.getInstance().onShown(managerCallback);
    if (callbacks != null) {
      
      
      int callbackCount = callbacks.size();
      for (int i = callbackCount - 1; i >= 0; i--) {
        callbacks.get(i).onShown((B) this);
      }
    }
  }

  void onViewHidden(int event) {
    
    SnackbarManager.getInstance().onDismissed(managerCallback);
    if (callbacks != null) {
      
      
      int callbackCount = callbacks.size();
      for (int i = callbackCount - 1; i >= 0; i--) {
        callbacks.get(i).onDismissed((B) this, event);
      }
    }
    
    ViewParent parent = view.getParent();
    if (parent instanceof ViewGroup) {
      ((ViewGroup) parent).removeView(view);
    }
  }

  
  boolean shouldAnimate() {
    int feedbackFlags = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
    List<AccessibilityServiceInfo> serviceList =
        accessibilityManager.getEnabledAccessibilityServiceList(feedbackFlags);
    return serviceList != null && serviceList.isEmpty();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  protected static class SnackbarBaseLayout extends FrameLayout {

    private static final OnTouchListener consumeAllTouchListener =
        new OnTouchListener() {
          @SuppressLint("ClickableViewAccessibility")
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            
            return true;
          }
        };

    private BaseTransientBottomBar.OnLayoutChangeListener onLayoutChangeListener;
    private BaseTransientBottomBar.OnAttachStateChangeListener onAttachStateChangeListener;
    @AnimationMode private int animationMode;
    private final float backgroundOverlayColorAlpha;
    private final float actionTextColorAlpha;
    private ColorStateList backgroundTint;
    private PorterDuff.Mode backgroundTintMode;

    protected SnackbarBaseLayout(@NonNull Context context) {
      this(context, null);
    }

    protected SnackbarBaseLayout(@NonNull Context context, AttributeSet attrs) {
      super(MaterialThemeOverlay.wrap(context, attrs, 0, 0), attrs);
      
      
      context = getContext();
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
      if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
        ViewCompat.setElevation(
            this, a.getDimensionPixelSize(R.styleable.SnackbarLayout_elevation, 0));
      }
      animationMode = a.getInt(R.styleable.SnackbarLayout_animationMode, ANIMATION_MODE_SLIDE);
      backgroundOverlayColorAlpha =
          a.getFloat(R.styleable.SnackbarLayout_backgroundOverlayColorAlpha, 1);
      setBackgroundTintList(
          MaterialResources.getColorStateList(
              context, a, R.styleable.SnackbarLayout_backgroundTint));
      setBackgroundTintMode(
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.SnackbarLayout_backgroundTintMode, -1), PorterDuff.Mode.SRC_IN));
      actionTextColorAlpha = a.getFloat(R.styleable.SnackbarLayout_actionTextColorAlpha, 1);
      a.recycle();

      setOnTouchListener(consumeAllTouchListener);
      setFocusable(true);

      if (getBackground() == null) {
        ViewCompat.setBackground(this, createThemedBackground());
      }
    }

    @Override
    public void setBackground(@Nullable Drawable drawable) {
      setBackgroundDrawable(drawable);
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable drawable) {
      if (drawable != null && backgroundTint != null) {
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, backgroundTint);
        DrawableCompat.setTintMode(drawable, backgroundTintMode);
      }
      super.setBackgroundDrawable(drawable);
    }

    @Override
    public void setBackgroundTintList(@Nullable ColorStateList backgroundTint) {
      this.backgroundTint = backgroundTint;
      if (getBackground() != null) {
        Drawable wrappedBackground = DrawableCompat.wrap(getBackground().mutate());
        DrawableCompat.setTintList(wrappedBackground, backgroundTint);
        DrawableCompat.setTintMode(wrappedBackground, backgroundTintMode);
        if (wrappedBackground != getBackground()) {
          super.setBackgroundDrawable(wrappedBackground);
        }
      }
    }

    @Override
    public void setBackgroundTintMode(@Nullable PorterDuff.Mode backgroundTintMode) {
      this.backgroundTintMode = backgroundTintMode;
      if (getBackground() != null) {
        Drawable wrappedBackground = DrawableCompat.wrap(getBackground().mutate());
        DrawableCompat.setTintMode(wrappedBackground, backgroundTintMode);
        if (wrappedBackground != getBackground()) {
          super.setBackgroundDrawable(wrappedBackground);
        }
      }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener onClickListener) {
      
      setOnTouchListener(onClickListener != null ? null : consumeAllTouchListener);
      super.setOnClickListener(onClickListener);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);
      if (onLayoutChangeListener != null) {
        onLayoutChangeListener.onLayoutChange(this, l, t, r, b);
      }
    }

    @Override
    protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      if (onAttachStateChangeListener != null) {
        onAttachStateChangeListener.onViewAttachedToWindow(this);
      }

      ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      if (onAttachStateChangeListener != null) {
        onAttachStateChangeListener.onViewDetachedFromWindow(this);
      }
    }

    void setOnLayoutChangeListener(
        BaseTransientBottomBar.OnLayoutChangeListener onLayoutChangeListener) {
      this.onLayoutChangeListener = onLayoutChangeListener;
    }

    void setOnAttachStateChangeListener(
        BaseTransientBottomBar.OnAttachStateChangeListener listener) {
      onAttachStateChangeListener = listener;
    }

    @AnimationMode
    int getAnimationMode() {
      return animationMode;
    }

    void setAnimationMode(@AnimationMode int animationMode) {
      this.animationMode = animationMode;
    }

    float getBackgroundOverlayColorAlpha() {
      return backgroundOverlayColorAlpha;
    }

    float getActionTextColorAlpha() {
      return actionTextColorAlpha;
    }

    @NonNull
    private Drawable createThemedBackground() {
      float cornerRadius =
          getResources().getDimension(R.dimen.mtrl_snackbar_background_corner_radius);

      GradientDrawable background = new GradientDrawable();
      background.setShape(GradientDrawable.RECTANGLE);
      background.setCornerRadius(cornerRadius);

      int backgroundColor =
          MaterialColors.layer(
              this, R.attr.colorSurface, R.attr.colorOnSurface, getBackgroundOverlayColorAlpha());
      background.setColor(backgroundColor);
      if (backgroundTint != null) {
        Drawable wrappedDrawable = DrawableCompat.wrap(background);
        DrawableCompat.setTintList(wrappedDrawable, backgroundTint);
        return wrappedDrawable;
      } else {
        return DrawableCompat.wrap(background);
      }
    }
  }

  
  public static class Behavior extends SwipeDismissBehavior<View> {
    @NonNull private final BehaviorDelegate delegate;

    public Behavior() {
      delegate = new BehaviorDelegate(this);
    }

    private void setBaseTransientBottomBar(
        @NonNull BaseTransientBottomBar<?> baseTransientBottomBar) {
      delegate.setBaseTransientBottomBar(baseTransientBottomBar);
    }

    @Override
    public boolean canSwipeDismissView(View child) {
      return delegate.canSwipeDismissView(child);
    }

    @Override
    public boolean onInterceptTouchEvent(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent event) {
      delegate.onInterceptTouchEvent(parent, child, event);
      return super.onInterceptTouchEvent(parent, child, event);
    }
  }

  
  @RestrictTo(LIBRARY_GROUP)
  
  public static class BehaviorDelegate {
    private SnackbarManager.Callback managerCallback;

    public BehaviorDelegate(@NonNull SwipeDismissBehavior<?> behavior) {
      behavior.setStartAlphaSwipeDistance(0.1f);
      behavior.setEndAlphaSwipeDistance(0.6f);
      behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
    }

    public void setBaseTransientBottomBar(
        @NonNull BaseTransientBottomBar<?> baseTransientBottomBar) {
      this.managerCallback = baseTransientBottomBar.managerCallback;
    }

    public boolean canSwipeDismissView(View child) {
      return child instanceof SnackbarBaseLayout;
    }

    public void onInterceptTouchEvent(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent event) {
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
          
          
          if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
            SnackbarManager.getInstance().pauseTimeout(managerCallback);
          }
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          SnackbarManager.getInstance().restoreTimeoutIfPaused(managerCallback);
          break;
        default:
          break;
      }
    }
  }
}
