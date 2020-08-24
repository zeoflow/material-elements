

package com.zeoflow.material.elements.appbar;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.internal.CollapsingTextHelper;
import com.zeoflow.material.elements.internal.DescendantOffsetUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class CollapsingToolbarLayout extends FrameLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_CollapsingToolbar;
  private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

  private boolean refreshToolbar = true;
  private int toolbarId;
  @Nullable private Toolbar toolbar;
  @Nullable private View toolbarDirectChild;
  private View dummyView;

  private int expandedMarginStart;
  private int expandedMarginTop;
  private int expandedMarginEnd;
  private int expandedMarginBottom;

  private final Rect tmpRect = new Rect();
  @NonNull final CollapsingTextHelper collapsingTextHelper;
  private boolean collapsingTitleEnabled;
  private boolean drawCollapsingTitle;

  @Nullable private Drawable contentScrim;
  @Nullable Drawable statusBarScrim;
  private int scrimAlpha;
  private boolean scrimsAreShown;
  private ValueAnimator scrimAnimator;
  private long scrimAnimationDuration;
  private int scrimVisibleHeightTrigger = -1;

  private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;

  int currentOffset;

  @Nullable WindowInsetsCompat lastInsets;

  public CollapsingToolbarLayout(@NonNull Context context) {
    this(context, null);
  }

  public CollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    collapsingTextHelper = new CollapsingTextHelper(this);
    collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.CollapsingToolbarLayout,
            defStyleAttr,
            DEF_STYLE_RES);

    collapsingTextHelper.setExpandedTextGravity(
        a.getInt(
            R.styleable.CollapsingToolbarLayout_expandedTitleGravity,
            GravityCompat.START | Gravity.BOTTOM));
    collapsingTextHelper.setCollapsedTextGravity(
        a.getInt(
            R.styleable.CollapsingToolbarLayout_collapsedTitleGravity,
            GravityCompat.START | Gravity.CENTER_VERTICAL));

    expandedMarginStart =
        expandedMarginTop =
            expandedMarginEnd =
                expandedMarginBottom =
                    a.getDimensionPixelSize(
                        R.styleable.CollapsingToolbarLayout_expandedTitleMargin, 0);

    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart)) {
      expandedMarginStart =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd)) {
      expandedMarginEnd =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop)) {
      expandedMarginTop =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0);
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
      expandedMarginBottom =
          a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);
    }

    collapsingTitleEnabled = a.getBoolean(R.styleable.CollapsingToolbarLayout_titleEnabled, true);
    setTitle(a.getText(R.styleable.CollapsingToolbarLayout_title));

    
    collapsingTextHelper.setExpandedTextAppearance(
        R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
    collapsingTextHelper.setCollapsedTextAppearance(
        androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

    
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance)) {
      collapsingTextHelper.setExpandedTextAppearance(
          a.getResourceId(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, 0));
    }
    if (a.hasValue(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance)) {
      collapsingTextHelper.setCollapsedTextAppearance(
          a.getResourceId(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, 0));
    }

    scrimVisibleHeightTrigger =
        a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

    if (a.hasValue(R.styleable.CollapsingToolbarLayout_maxLines)) {
      collapsingTextHelper.setMaxLines(a.getInt(R.styleable.CollapsingToolbarLayout_maxLines, 1));
    }

    scrimAnimationDuration =
        a.getInt(
            R.styleable.CollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION);

    setContentScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim));
    setStatusBarScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim));

    toolbarId = a.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1);

    a.recycle();

    setWillNotDraw(false);

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View v, @NonNull WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    
    final ViewParent parent = getParent();
    if (parent instanceof AppBarLayout) {
      
      ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

      if (onOffsetChangedListener == null) {
        onOffsetChangedListener = new OffsetUpdateListener();
      }
      ((AppBarLayout) parent).addOnOffsetChangedListener(onOffsetChangedListener);

      
      ViewCompat.requestApplyInsets(this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    
    final ViewParent parent = getParent();
    if (onOffsetChangedListener != null && parent instanceof AppBarLayout) {
      ((AppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    super.onDetachedFromWindow();
  }

  WindowInsetsCompat onWindowInsetChanged(@NonNull final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      
      newInsets = insets;
    }

    
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      requestLayout();
    }

    
    
    return insets.consumeSystemWindowInsets();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    
    
    ensureToolbar();
    if (toolbar == null && contentScrim != null && scrimAlpha > 0) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
    }

    
    if (collapsingTitleEnabled && drawCollapsingTitle) {
      collapsingTextHelper.draw(canvas);
    }

    
    if (statusBarScrim != null && scrimAlpha > 0) {
      final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
      if (topInset > 0) {
        statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
        statusBarScrim.mutate().setAlpha(scrimAlpha);
        statusBarScrim.draw(canvas);
      }
    }
  }

  @Override
  protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    
    
    
    boolean invalidated = false;
    if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
      contentScrim.mutate().setAlpha(scrimAlpha);
      contentScrim.draw(canvas);
      invalidated = true;
    }
    return super.drawChild(canvas, child, drawingTime) || invalidated;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (contentScrim != null) {
      contentScrim.setBounds(0, 0, w, h);
    }
  }

  private void ensureToolbar() {
    if (!refreshToolbar) {
      return;
    }

    
    this.toolbar = null;
    toolbarDirectChild = null;

    if (toolbarId != -1) {
      
      this.toolbar = findViewById(toolbarId);
      if (this.toolbar != null) {
        toolbarDirectChild = findDirectChild(this.toolbar);
      }
    }

    if (this.toolbar == null) {
      
      
      Toolbar toolbar = null;
      for (int i = 0, count = getChildCount(); i < count; i++) {
        final View child = getChildAt(i);
        if (child instanceof Toolbar) {
          toolbar = (Toolbar) child;
          break;
        }
      }
      this.toolbar = toolbar;
    }

    updateDummyView();
    refreshToolbar = false;
  }

  private boolean isToolbarChild(View child) {
    return (toolbarDirectChild == null || toolbarDirectChild == this)
        ? child == toolbar
        : child == toolbarDirectChild;
  }

  
  @NonNull
  private View findDirectChild(@NonNull final View descendant) {
    View directChild = descendant;
    for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
      if (p instanceof View) {
        directChild = (View) p;
      }
    }
    return directChild;
  }

  private void updateDummyView() {
    if (!collapsingTitleEnabled && dummyView != null) {
      
      final ViewParent parent = dummyView.getParent();
      if (parent instanceof ViewGroup) {
        ((ViewGroup) parent).removeView(dummyView);
      }
    }
    if (collapsingTitleEnabled && toolbar != null) {
      if (dummyView == null) {
        dummyView = new View(getContext());
      }
      if (dummyView.getParent() == null) {
        toolbar.addView(dummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    ensureToolbar();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    final int mode = MeasureSpec.getMode(heightMeasureSpec);
    final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
    if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
      
      
      heightMeasureSpec =
          MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (lastInsets != null) {
      
      final int insetTop = lastInsets.getSystemWindowInsetTop();
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (!ViewCompat.getFitsSystemWindows(child)) {
          if (child.getTop() < insetTop) {
            
            
            ViewCompat.offsetTopAndBottom(child, insetTop);
          }
        }
      }
    }

    
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).onViewLayout();
    }

    
    if (collapsingTitleEnabled && dummyView != null) {
      
      
      drawCollapsingTitle =
          ViewCompat.isAttachedToWindow(dummyView) && dummyView.getVisibility() == VISIBLE;

      if (drawCollapsingTitle) {
        final boolean isRtl =
            ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

        
        final int maxOffset =
            getMaxOffsetForPinChild(toolbarDirectChild != null ? toolbarDirectChild : toolbar);
        DescendantOffsetUtils.getDescendantRect(this, dummyView, tmpRect);
        collapsingTextHelper.setCollapsedBounds(
            tmpRect.left + (isRtl ? toolbar.getTitleMarginEnd() : toolbar.getTitleMarginStart()),
            tmpRect.top + maxOffset + toolbar.getTitleMarginTop(),
            tmpRect.right - (isRtl ? toolbar.getTitleMarginStart() : toolbar.getTitleMarginEnd()),
            tmpRect.bottom + maxOffset - toolbar.getTitleMarginBottom());

        
        collapsingTextHelper.setExpandedBounds(
            isRtl ? expandedMarginEnd : expandedMarginStart,
            tmpRect.top + expandedMarginTop,
            right - left - (isRtl ? expandedMarginStart : expandedMarginEnd),
            bottom - top - expandedMarginBottom);
        
        collapsingTextHelper.recalculate();
      }
    }

    
    if (toolbar != null) {
      if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.getText())) {
        
        setTitle(toolbar.getTitle());
      }
      if (toolbarDirectChild == null || toolbarDirectChild == this) {
        setMinimumHeight(getHeightWithMargins(toolbar));
      } else {
        setMinimumHeight(getHeightWithMargins(toolbarDirectChild));
      }
    }

    updateScrimVisibility();

    
    for (int i = 0, z = getChildCount(); i < z; i++) {
      getViewOffsetHelper(getChildAt(i)).applyOffsets();
    }
  }

  private static int getHeightWithMargins(@NonNull final View view) {
    final ViewGroup.LayoutParams lp = view.getLayoutParams();
    if (lp instanceof MarginLayoutParams) {
      final MarginLayoutParams mlp = (MarginLayoutParams) lp;
      return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
    }
    return view.getHeight();
  }

  @NonNull
  static ViewOffsetHelper getViewOffsetHelper(@NonNull View view) {
    ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
    if (offsetHelper == null) {
      offsetHelper = new ViewOffsetHelper(view);
      view.setTag(R.id.view_offset_helper, offsetHelper);
    }
    return offsetHelper;
  }

  
  public void setTitle(@Nullable CharSequence title) {
    collapsingTextHelper.setText(title);
    updateContentDescriptionFromTitle();
  }

  
  @Nullable
  public CharSequence getTitle() {
    return collapsingTitleEnabled ? collapsingTextHelper.getText() : null;
  }

  
  public void setTitleEnabled(boolean enabled) {
    if (enabled != collapsingTitleEnabled) {
      collapsingTitleEnabled = enabled;
      updateContentDescriptionFromTitle();
      updateDummyView();
      requestLayout();
    }
  }

  
  public boolean isTitleEnabled() {
    return collapsingTitleEnabled;
  }

  
  public void setScrimsShown(boolean shown) {
    setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
  }

  
  public void setScrimsShown(boolean shown, boolean animate) {
    if (scrimsAreShown != shown) {
      if (animate) {
        animateScrim(shown ? 0xFF : 0x0);
      } else {
        setScrimAlpha(shown ? 0xFF : 0x0);
      }
      scrimsAreShown = shown;
    }
  }

  private void animateScrim(int targetAlpha) {
    ensureToolbar();
    if (scrimAnimator == null) {
      scrimAnimator = new ValueAnimator();
      scrimAnimator.setDuration(scrimAnimationDuration);
      scrimAnimator.setInterpolator(
          targetAlpha > scrimAlpha
              ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
              : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
      scrimAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              setScrimAlpha((int) animator.getAnimatedValue());
            }
          });
    } else if (scrimAnimator.isRunning()) {
      scrimAnimator.cancel();
    }

    scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
    scrimAnimator.start();
  }

  void setScrimAlpha(int alpha) {
    if (alpha != scrimAlpha) {
      final Drawable contentScrim = this.contentScrim;
      if (contentScrim != null && toolbar != null) {
        ViewCompat.postInvalidateOnAnimation(toolbar);
      }
      scrimAlpha = alpha;
      ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
    }
  }

  int getScrimAlpha() {
    return scrimAlpha;
  }

  
  public void setContentScrim(@Nullable Drawable drawable) {
    if (contentScrim != drawable) {
      if (contentScrim != null) {
        contentScrim.setCallback(null);
      }
      contentScrim = drawable != null ? drawable.mutate() : null;
      if (contentScrim != null) {
        contentScrim.setBounds(0, 0, getWidth(), getHeight());
        contentScrim.setCallback(this);
        contentScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  
  public void setContentScrimColor(@ColorInt int color) {
    setContentScrim(new ColorDrawable(color));
  }

  
  public void setContentScrimResource(@DrawableRes int resId) {
    setContentScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  
  @Nullable
  public Drawable getContentScrim() {
    return contentScrim;
  }

  
  public void setStatusBarScrim(@Nullable Drawable drawable) {
    if (statusBarScrim != drawable) {
      if (statusBarScrim != null) {
        statusBarScrim.setCallback(null);
      }
      statusBarScrim = drawable != null ? drawable.mutate() : null;
      if (statusBarScrim != null) {
        if (statusBarScrim.isStateful()) {
          statusBarScrim.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
        statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
        statusBarScrim.setCallback(this);
        statusBarScrim.setAlpha(scrimAlpha);
      }
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    Drawable d = statusBarScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    d = contentScrim;
    if (d != null && d.isStateful()) {
      changed |= d.setState(state);
    }
    if (collapsingTextHelper != null) {
      changed |= collapsingTextHelper.setState(state);
    }

    if (changed) {
      invalidate();
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
      statusBarScrim.setVisible(visible, false);
    }
    if (contentScrim != null && contentScrim.isVisible() != visible) {
      contentScrim.setVisible(visible, false);
    }
  }

  
  public void setStatusBarScrimColor(@ColorInt int color) {
    setStatusBarScrim(new ColorDrawable(color));
  }

  
  public void setStatusBarScrimResource(@DrawableRes int resId) {
    setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
  }

  
  @Nullable
  public Drawable getStatusBarScrim() {
    return statusBarScrim;
  }

  
  public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setCollapsedTextAppearance(resId);
  }

  
  public void setCollapsedTitleTextColor(@ColorInt int color) {
    setCollapsedTitleTextColor(ColorStateList.valueOf(color));
  }

  
  public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
    collapsingTextHelper.setCollapsedTextColor(colors);
  }

  
  public void setCollapsedTitleGravity(int gravity) {
    collapsingTextHelper.setCollapsedTextGravity(gravity);
  }

  
  public int getCollapsedTitleGravity() {
    return collapsingTextHelper.getCollapsedTextGravity();
  }

  
  public void setExpandedTitleTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setExpandedTextAppearance(resId);
  }

  
  public void setExpandedTitleColor(@ColorInt int color) {
    setExpandedTitleTextColor(ColorStateList.valueOf(color));
  }

  
  public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
    collapsingTextHelper.setExpandedTextColor(colors);
  }

  
  public void setExpandedTitleGravity(int gravity) {
    collapsingTextHelper.setExpandedTextGravity(gravity);
  }

  
  public int getExpandedTitleGravity() {
    return collapsingTextHelper.getExpandedTextGravity();
  }

  
  public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
    collapsingTextHelper.setCollapsedTypeface(typeface);
  }

  
  @NonNull
  public Typeface getCollapsedTitleTypeface() {
    return collapsingTextHelper.getCollapsedTypeface();
  }

  
  public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
    collapsingTextHelper.setExpandedTypeface(typeface);
  }

  
  @NonNull
  public Typeface getExpandedTitleTypeface() {
    return collapsingTextHelper.getExpandedTypeface();
  }

  
  public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
    expandedMarginStart = start;
    expandedMarginTop = top;
    expandedMarginEnd = end;
    expandedMarginBottom = bottom;
    requestLayout();
  }

  
  public int getExpandedTitleMarginStart() {
    return expandedMarginStart;
  }

  
  public void setExpandedTitleMarginStart(int margin) {
    expandedMarginStart = margin;
    requestLayout();
  }

  
  public int getExpandedTitleMarginTop() {
    return expandedMarginTop;
  }

  
  public void setExpandedTitleMarginTop(int margin) {
    expandedMarginTop = margin;
    requestLayout();
  }

  
  public int getExpandedTitleMarginEnd() {
    return expandedMarginEnd;
  }

  
  public void setExpandedTitleMarginEnd(int margin) {
    expandedMarginEnd = margin;
    requestLayout();
  }

  
  public int getExpandedTitleMarginBottom() {
    return expandedMarginBottom;
  }

  
  public void setExpandedTitleMarginBottom(int margin) {
    expandedMarginBottom = margin;
    requestLayout();
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public void setMaxLines(int maxLines) {
    collapsingTextHelper.setMaxLines(maxLines);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  public int getMaxLines() {
    return collapsingTextHelper.getMaxLines();
  }

  
  public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
    if (scrimVisibleHeightTrigger != height) {
      scrimVisibleHeightTrigger = height;
      
      updateScrimVisibility();
    }
  }

  
  public int getScrimVisibleHeightTrigger() {
    if (scrimVisibleHeightTrigger >= 0) {
      
      return scrimVisibleHeightTrigger;
    }

    
    final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight > 0) {
      
      return Math.min((minHeight * 2) + insetTop, getHeight());
    }

    
    
    return getHeight() / 3;
  }

  
  public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
    scrimAnimationDuration = duration;
  }

  
  public long getScrimAnimationDuration() {
    return scrimAnimationDuration;
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  public static class LayoutParams extends FrameLayout.LayoutParams {

    private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

    
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
    @Retention(RetentionPolicy.SOURCE)
    @interface CollapseMode {}

    
    public static final int COLLAPSE_MODE_OFF = 0;

    
    public static final int COLLAPSE_MODE_PIN = 1;

    
    public static final int COLLAPSE_MODE_PARALLAX = 2;

    int collapseMode = COLLAPSE_MODE_OFF;
    float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);

      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CollapsingToolbarLayout_Layout);
      collapseMode =
          a.getInt(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF);
      setParallaxMultiplier(
          a.getFloat(
              R.styleable.CollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier,
              DEFAULT_PARALLAX_MULTIPLIER));
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      super(width, height, gravity);
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
      
      super(source);
    }

    
    public void setCollapseMode(@CollapseMode int collapseMode) {
      this.collapseMode = collapseMode;
    }

    
    @CollapseMode
    public int getCollapseMode() {
      return collapseMode;
    }

    
    public void setParallaxMultiplier(float multiplier) {
      parallaxMult = multiplier;
    }

    
    public float getParallaxMultiplier() {
      return parallaxMult;
    }
  }

  
  final void updateScrimVisibility() {
    if (contentScrim != null || statusBarScrim != null) {
      setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
    }
  }

  final int getMaxOffsetForPinChild(@NonNull View child) {
    final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
    return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
  }

  private void updateContentDescriptionFromTitle() {
    
    setContentDescription(getTitle());
  }

  private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
    OffsetUpdateListener() {}

    @Override
    public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
      currentOffset = verticalOffset;

      final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

        switch (lp.collapseMode) {
          case LayoutParams.COLLAPSE_MODE_PIN:
            offsetHelper.setTopAndBottomOffset(
                MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
            break;
          case LayoutParams.COLLAPSE_MODE_PARALLAX:
            offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
            break;
          default:
            break;
        }
      }

      
      updateScrimVisibility();

      if (statusBarScrim != null && insetTop > 0) {
        ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
      }

      
      final int expandRange =
          getHeight() - ViewCompat.getMinimumHeight(CollapsingToolbarLayout.this) - insetTop;
      collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);
    }
  }
}
