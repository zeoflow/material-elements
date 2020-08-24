

package com.zeoflow.material.elements.tabs;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.badge.BadgeDrawable;
import com.zeoflow.material.elements.badge.BadgeUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.ripple.RippleUtils;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;


@ViewPager.DecorView
public class TabLayout extends HorizontalScrollView {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_TabLayout;

  @Dimension(unit = Dimension.DP)
  private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;

  @Dimension(unit = Dimension.DP)
  static final int DEFAULT_GAP_TEXT_ICON = 8;

  @Dimension(unit = Dimension.DP)
  private static final int DEFAULT_HEIGHT = 48;

  @Dimension(unit = Dimension.DP)
  private static final int TAB_MIN_WIDTH_MARGIN = 56;

  @Dimension(unit = Dimension.DP)
  private static final int MIN_INDICATOR_WIDTH = 24;

  @Dimension(unit = Dimension.DP)
  static final int FIXED_WRAP_GUTTER_MIN = 16;

  private static final int INVALID_WIDTH = -1;

  private static final int ANIMATION_DURATION = 300;

  private static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool<>(16);

  private static final String LOG_TAG = "TabLayout";

  
  public static final int MODE_SCROLLABLE = 0;

  
  public static final int MODE_FIXED = 1;

  
  public static final int MODE_AUTO = 2;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED, MODE_AUTO})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Mode {}

  
  public static final int TAB_LABEL_VISIBILITY_UNLABELED = 0;

  
  public static final int TAB_LABEL_VISIBILITY_LABELED = 1;

  
  @IntDef(value = {TAB_LABEL_VISIBILITY_UNLABELED, TAB_LABEL_VISIBILITY_LABELED})
  public @interface LabelVisibility {}

  
  public static final int GRAVITY_FILL = 0;

  
  public static final int GRAVITY_CENTER = 1;

  
  public static final int GRAVITY_START = 1 << 1;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      flag = true,
      value = {GRAVITY_FILL, GRAVITY_CENTER, GRAVITY_START})
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabGravity {}

  
  public static final int INDICATOR_GRAVITY_BOTTOM = 0;

  
  public static final int INDICATOR_GRAVITY_CENTER = 1;

  
  public static final int INDICATOR_GRAVITY_TOP = 2;

  
  public static final int INDICATOR_GRAVITY_STRETCH = 3;

  
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      value = {
        INDICATOR_GRAVITY_BOTTOM,
        INDICATOR_GRAVITY_CENTER,
        INDICATOR_GRAVITY_TOP,
        INDICATOR_GRAVITY_STRETCH
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabIndicatorGravity {}

  
  public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {}

  
  @Deprecated
  public interface BaseOnTabSelectedListener<T extends Tab> {
    
    public void onTabSelected(T tab);

    
    public void onTabUnselected(T tab);

    
    public void onTabReselected(T tab);
  }

  private final ArrayList<Tab> tabs = new ArrayList<>();
  @Nullable private Tab selectedTab;

  private final RectF tabViewContentBounds = new RectF();

  @NonNull final SlidingTabIndicator slidingTabIndicator;

  int tabPaddingStart;
  int tabPaddingTop;
  int tabPaddingEnd;
  int tabPaddingBottom;

  int tabTextAppearance;
  ColorStateList tabTextColors;
  ColorStateList tabIconTint;
  ColorStateList tabRippleColorStateList;
  @Nullable Drawable tabSelectedIndicator;

  android.graphics.PorterDuff.Mode tabIconTintMode;
  float tabTextSize;
  float tabTextMultiLineSize;

  final int tabBackgroundResId;

  int tabMaxWidth = Integer.MAX_VALUE;
  private final int requestedTabMinWidth;
  private final int requestedTabMaxWidth;
  private final int scrollableTabMinWidth;

  private int contentInsetStart;

  @TabGravity int tabGravity;
  int tabIndicatorAnimationDuration;
  @TabIndicatorGravity int tabIndicatorGravity;
  @Mode int mode;
  boolean inlineLabel;
  boolean tabIndicatorFullWidth;
  boolean unboundedRipple;

  @Nullable private BaseOnTabSelectedListener selectedListener;

  private final ArrayList<BaseOnTabSelectedListener> selectedListeners = new ArrayList<>();
  @Nullable private BaseOnTabSelectedListener currentVpSelectedListener;

  private ValueAnimator scrollAnimator;

  @Nullable ViewPager viewPager;
  @Nullable private PagerAdapter pagerAdapter;
  private DataSetObserver pagerAdapterObserver;
  private TabLayoutOnPageChangeListener pageChangeListener;
  private AdapterChangeListener adapterChangeListener;
  private boolean setupViewPagerImplicitly;

  
  private final Pools.Pool<TabView> tabViewPool = new Pools.SimplePool<>(12);

  public TabLayout(@NonNull Context context) {
    this(context, null);
  }

  public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.tabStyle);
  }

  public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    
    setHorizontalScrollBarEnabled(false);

    
    slidingTabIndicator = new SlidingTabIndicator(context);
    super.addView(
        slidingTabIndicator,
        0,
        new HorizontalScrollView.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.TabLayout,
            defStyleAttr,
            DEF_STYLE_RES,
            R.styleable.TabLayout_tabTextAppearance);

    if (getBackground() instanceof ColorDrawable) {
      ColorDrawable background = (ColorDrawable) getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
      materialShapeDrawable.initializeElevationOverlay(context);
      materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

    slidingTabIndicator.setSelectedIndicatorHeight(
        a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1));
    slidingTabIndicator.setSelectedIndicatorColor(
        a.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));
    setSelectedTabIndicator(
        MaterialResources.getDrawable(context, a, R.styleable.TabLayout_tabIndicator));
    setSelectedTabIndicatorGravity(
        a.getInt(R.styleable.TabLayout_tabIndicatorGravity, INDICATOR_GRAVITY_BOTTOM));
    setTabIndicatorFullWidth(a.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));

    tabPaddingStart =
        tabPaddingTop =
            tabPaddingEnd =
                tabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
    tabPaddingStart =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, tabPaddingStart);
    tabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, tabPaddingTop);
    tabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, tabPaddingEnd);
    tabPaddingBottom =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, tabPaddingBottom);

    tabTextAppearance =
        a.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);

    
    final TypedArray ta =
        context.obtainStyledAttributes(
            tabTextAppearance, androidx.appcompat.R.styleable.TextAppearance);
    try {
      tabTextSize =
          ta.getDimensionPixelSize(
              androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
      tabTextColors =
          MaterialResources.getColorStateList(
              context,
              ta,
              androidx.appcompat.R.styleable.TextAppearance_android_textColor);
    } finally {
      ta.recycle();
    }

    if (a.hasValue(R.styleable.TabLayout_tabTextColor)) {
      
      tabTextColors =
          MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabTextColor);
    }

    if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
      
      
      
      final int selected = a.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0);
      tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), selected);
    }

    tabIconTint =
        MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabIconTint);
    tabIconTintMode =
        ViewUtils.parseTintMode(a.getInt(R.styleable.TabLayout_tabIconTintMode, -1), null);

    tabRippleColorStateList =
        MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabRippleColor);

    tabIndicatorAnimationDuration =
        a.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);

    requestedTabMinWidth =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, INVALID_WIDTH);
    requestedTabMaxWidth =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, INVALID_WIDTH);
    tabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, 0);
    contentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
    
    mode = a.getInt(R.styleable.TabLayout_tabMode, MODE_FIXED);
    tabGravity = a.getInt(R.styleable.TabLayout_tabGravity, GRAVITY_FILL);
    inlineLabel = a.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
    unboundedRipple = a.getBoolean(R.styleable.TabLayout_tabUnboundedRipple, false);
    a.recycle();

    
    final Resources res = getResources();
    tabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
    scrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);

    
    applyModeAndGravity();
  }

  
  public void setSelectedTabIndicatorColor(@ColorInt int color) {
    slidingTabIndicator.setSelectedIndicatorColor(color);
  }

  
  @Deprecated
  public void setSelectedTabIndicatorHeight(int height) {
    slidingTabIndicator.setSelectedIndicatorHeight(height);
  }

  
  public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
    setScrollPosition(position, positionOffset, updateSelectedText, true);
  }

  
  public void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedText,
      boolean updateIndicatorPosition) {
    final int roundedPosition = Math.round(position + positionOffset);
    if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
      return;
    }

    
    if (updateIndicatorPosition) {
      slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
    }

    
    if (scrollAnimator != null && scrollAnimator.isRunning()) {
      scrollAnimator.cancel();
    }
    scrollTo(calculateScrollXForTab(position, positionOffset), 0);

    
    if (updateSelectedText) {
      setSelectedTabView(roundedPosition);
    }
  }

  
  public void addTab(@NonNull Tab tab) {
    addTab(tab, tabs.isEmpty());
  }

  
  public void addTab(@NonNull Tab tab, int position) {
    addTab(tab, position, tabs.isEmpty());
  }

  
  public void addTab(@NonNull Tab tab, boolean setSelected) {
    addTab(tab, tabs.size(), setSelected);
  }

  
  public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }
    configureTab(tab, position);
    addTabView(tab);

    if (setSelected) {
      tab.select();
    }
  }

  private void addTabFromItemView(@NonNull TabItem item) {
    final Tab tab = newTab();
    if (item.text != null) {
      tab.setText(item.text);
    }
    if (item.icon != null) {
      tab.setIcon(item.icon);
    }
    if (item.customLayout != 0) {
      tab.setCustomView(item.customLayout);
    }
    if (!TextUtils.isEmpty(item.getContentDescription())) {
      tab.setContentDescription(item.getContentDescription());
    }
    addTab(tab);
  }

  
  @Deprecated
  public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
    setOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  
  @Deprecated
  public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    
    
    if (selectedListener != null) {
      removeOnTabSelectedListener(selectedListener);
    }
    
    
    selectedListener = listener;
    if (listener != null) {
      addOnTabSelectedListener(listener);
    }
  }

  
  public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    addOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  
  @Deprecated
  public void addOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener);
    }
  }

  
  public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
    removeOnTabSelectedListener((BaseOnTabSelectedListener) listener);
  }

  
  @Deprecated
  public void removeOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    selectedListeners.remove(listener);
  }

  
  public void clearOnTabSelectedListeners() {
    selectedListeners.clear();
  }

  
  @NonNull
  public Tab newTab() {
    Tab tab = createTabFromPool();
    tab.parent = this;
    tab.view = createTabView(tab);
    return tab;
  }

  
  protected Tab createTabFromPool() {
    Tab tab = tabPool.acquire();
    if (tab == null) {
      tab = new Tab();
    }
    return tab;
  }

  
  protected boolean releaseFromTabPool(Tab tab) {
    return tabPool.release(tab);
  }

  
  public int getTabCount() {
    return tabs.size();
  }

  
  @Nullable
  public Tab getTabAt(int index) {
    return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
  }

  
  public int getSelectedTabPosition() {
    return selectedTab != null ? selectedTab.getPosition() : -1;
  }

  
  public void removeTab(@NonNull Tab tab) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    removeTabAt(tab.getPosition());
  }

  
  public void removeTabAt(int position) {
    final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
    removeTabViewAt(position);

    final Tab removedTab = tabs.remove(position);
    if (removedTab != null) {
      removedTab.reset();
      releaseFromTabPool(removedTab);
    }

    final int newTabCount = tabs.size();
    for (int i = position; i < newTabCount; i++) {
      tabs.get(i).setPosition(i);
    }

    if (selectedTabPosition == position) {
      selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
    }
  }

  
  public void removeAllTabs() {
    
    for (int i = slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
      removeTabViewAt(i);
    }

    for (final Iterator<Tab> i = tabs.iterator(); i.hasNext(); ) {
      final Tab tab = i.next();
      i.remove();
      tab.reset();
      releaseFromTabPool(tab);
    }

    selectedTab = null;
  }

  
  public void setTabMode(@Mode int mode) {
    if (mode != this.mode) {
      this.mode = mode;
      applyModeAndGravity();
    }
  }

  
  @Mode
  public int getTabMode() {
    return mode;
  }

  
  public void setTabGravity(@TabGravity int gravity) {
    if (tabGravity != gravity) {
      tabGravity = gravity;
      applyModeAndGravity();
    }
  }

  
  @TabGravity
  public int getTabGravity() {
    return tabGravity;
  }

  
  public void setSelectedTabIndicatorGravity(@TabIndicatorGravity int indicatorGravity) {
    if (tabIndicatorGravity != indicatorGravity) {
      tabIndicatorGravity = indicatorGravity;
      ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }
  }

  
  @TabIndicatorGravity
  public int getTabIndicatorGravity() {
    return tabIndicatorGravity;
  }

  
  public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
    this.tabIndicatorFullWidth = tabIndicatorFullWidth;
    ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
  }

  
  public boolean isTabIndicatorFullWidth() {
    return tabIndicatorFullWidth;
  }

  
  public void setInlineLabel(boolean inline) {
    if (inlineLabel != inline) {
      inlineLabel = inline;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateOrientation();
        }
      }
      applyModeAndGravity();
    }
  }

  
  public void setInlineLabelResource(@BoolRes int inlineResourceId) {
    setInlineLabel(getResources().getBoolean(inlineResourceId));
  }

  
  public boolean isInlineLabel() {
    return inlineLabel;
  }

  
  public void setUnboundedRipple(boolean unboundedRipple) {
    if (this.unboundedRipple != unboundedRipple) {
      this.unboundedRipple = unboundedRipple;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  
  public void setUnboundedRippleResource(@BoolRes int unboundedRippleResourceId) {
    setUnboundedRipple(getResources().getBoolean(unboundedRippleResourceId));
  }

  
  public boolean hasUnboundedRipple() {
    return unboundedRipple;
  }

  
  public void setTabTextColors(@Nullable ColorStateList textColor) {
    if (tabTextColors != textColor) {
      tabTextColors = textColor;
      updateAllTabs();
    }
  }

  
  @Nullable
  public ColorStateList getTabTextColors() {
    return tabTextColors;
  }

  
  public void setTabTextColors(int normalColor, int selectedColor) {
    setTabTextColors(createColorStateList(normalColor, selectedColor));
  }

  
  public void setTabIconTint(@Nullable ColorStateList iconTint) {
    if (tabIconTint != iconTint) {
      tabIconTint = iconTint;
      updateAllTabs();
    }
  }

  
  public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
    setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  
  @Nullable
  public ColorStateList getTabIconTint() {
    return tabIconTint;
  }

  
  @Nullable
  public ColorStateList getTabRippleColor() {
    return tabRippleColorStateList;
  }

  
  public void setTabRippleColor(@Nullable ColorStateList color) {
    if (tabRippleColorStateList != color) {
      tabRippleColorStateList = color;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  
  public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
    setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
  }

  
  @Nullable
  public Drawable getTabSelectedIndicator() {
    return tabSelectedIndicator;
  }

  
  public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
    if (this.tabSelectedIndicator != tabSelectedIndicator) {
      this.tabSelectedIndicator = tabSelectedIndicator;
      ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }
  }

  
  public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
    if (tabSelectedIndicatorResourceId != 0) {
      setSelectedTabIndicator(
          AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
    } else {
      setSelectedTabIndicator(null);
    }
  }

  
  public void setupWithViewPager(@Nullable ViewPager viewPager) {
    setupWithViewPager(viewPager, true);
  }

  
  public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
    setupWithViewPager(viewPager, autoRefresh, false);
  }

  private void setupWithViewPager(
      @Nullable final ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
    if (this.viewPager != null) {
      
      if (pageChangeListener != null) {
        this.viewPager.removeOnPageChangeListener(pageChangeListener);
      }
      if (adapterChangeListener != null) {
        this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
      }
    }

    if (currentVpSelectedListener != null) {
      
      removeOnTabSelectedListener(currentVpSelectedListener);
      currentVpSelectedListener = null;
    }

    if (viewPager != null) {
      this.viewPager = viewPager;

      
      if (pageChangeListener == null) {
        pageChangeListener = new TabLayoutOnPageChangeListener(this);
      }
      pageChangeListener.reset();
      viewPager.addOnPageChangeListener(pageChangeListener);

      
      currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
      addOnTabSelectedListener(currentVpSelectedListener);

      final PagerAdapter adapter = viewPager.getAdapter();
      if (adapter != null) {
        
        
        setPagerAdapter(adapter, autoRefresh);
      }

      
      if (adapterChangeListener == null) {
        adapterChangeListener = new AdapterChangeListener();
      }
      adapterChangeListener.setAutoRefresh(autoRefresh);
      viewPager.addOnAdapterChangeListener(adapterChangeListener);

      
      setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    } else {
      
      
      this.viewPager = null;
      setPagerAdapter(null, false);
    }

    setupViewPagerImplicitly = implicitSetup;
  }

  
  @Deprecated
  public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
    setPagerAdapter(adapter, false);
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    
    return getTabScrollRange() > 0;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);

    if (viewPager == null) {
      
      
      final ViewParent vp = getParent();
      if (vp instanceof ViewPager) {
        
        
        setupWithViewPager((ViewPager) vp, true, true);
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (setupViewPagerImplicitly) {
      
      setupWithViewPager(null);
      setupViewPagerImplicitly = false;
    }
  }

  private int getTabScrollRange() {
    return Math.max(
        0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
  }

  void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
    if (pagerAdapter != null && pagerAdapterObserver != null) {
      
      pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
    }

    pagerAdapter = adapter;

    if (addObserver && adapter != null) {
      
      if (pagerAdapterObserver == null) {
        pagerAdapterObserver = new PagerAdapterObserver();
      }
      adapter.registerDataSetObserver(pagerAdapterObserver);
    }

    
    populateFromPagerAdapter();
  }

  void populateFromPagerAdapter() {
    removeAllTabs();

    if (pagerAdapter != null) {
      final int adapterCount = pagerAdapter.getCount();
      for (int i = 0; i < adapterCount; i++) {
        addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
      }

      
      if (viewPager != null && adapterCount > 0) {
        final int curItem = viewPager.getCurrentItem();
        if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
          selectTab(getTabAt(curItem));
        }
      }
    }
  }

  private void updateAllTabs() {
    for (int i = 0, z = tabs.size(); i < z; i++) {
      tabs.get(i).updateView();
    }
  }

  @NonNull
  private TabView createTabView(@NonNull final Tab tab) {
    TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
    if (tabView == null) {
      tabView = new TabView(getContext());
    }
    tabView.setTab(tab);
    tabView.setFocusable(true);
    tabView.setMinimumWidth(getTabMinWidth());
    if (TextUtils.isEmpty(tab.contentDesc)) {
      tabView.setContentDescription(tab.text);
    } else {
      tabView.setContentDescription(tab.contentDesc);
    }
    return tabView;
  }

  private void configureTab(@NonNull Tab tab, int position) {
    tab.setPosition(position);
    tabs.add(position, tab);

    final int count = tabs.size();
    for (int i = position + 1; i < count; i++) {
      tabs.get(i).setPosition(i);
    }
  }

  private void addTabView(@NonNull Tab tab) {
    final TabView tabView = tab.view;
    tabView.setSelected(false);
    tabView.setActivated(false);
    slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
  }

  @Override
  public void addView(View child) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  private void addViewInternal(final View child) {
    if (child instanceof TabItem) {
      addTabFromItemView((TabItem) child);
    } else {
      throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
    }
  }

  @NonNull
  private LinearLayout.LayoutParams createLayoutParamsForTabs() {
    final LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    updateTabViewLayoutParams(lp);
    return lp;
  }

  private void updateTabViewLayoutParams(@NonNull LinearLayout.LayoutParams lp) {
    if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
      lp.width = 0;
      lp.weight = 1;
    } else {
      lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
      lp.weight = 0;
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
             1,
             getTabCount(),
             false,
             CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    
    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
      View tabView = slidingTabIndicator.getChildAt(i);
      if (tabView instanceof TabView) {
        ((TabView) tabView).drawBackground(canvas);
      }
    }

    super.onDraw(canvas);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    
    
    final int idealHeight = Math.round(ViewUtils.dpToPx(getContext(), getDefaultHeight()));
    switch (MeasureSpec.getMode(heightMeasureSpec)) {
      case MeasureSpec.AT_MOST:
        if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
          getChildAt(0).setMinimumHeight(idealHeight);
        }
        break;
      case MeasureSpec.UNSPECIFIED:
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                idealHeight + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        break;
      default:
        break;
    }

    final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
    if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
      
      
      tabMaxWidth =
          requestedTabMaxWidth > 0
              ? requestedTabMaxWidth
              : (int) (specWidth - ViewUtils.dpToPx(getContext(), TAB_MIN_WIDTH_MARGIN));
    }

    
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getChildCount() == 1) {
      
      
      final View child = getChildAt(0);
      boolean remeasure = false;

      switch (mode) {
        case MODE_AUTO:
        case MODE_SCROLLABLE:
          
          
          remeasure = child.getMeasuredWidth() < getMeasuredWidth();
          break;
        case MODE_FIXED:
          
          remeasure = child.getMeasuredWidth() != getMeasuredWidth();
          break;
      }

      if (remeasure) {
        
        int childHeightMeasureSpec =
            getChildMeasureSpec(
                heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(),
                child.getLayoutParams().height);

        int childWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      }
    }
  }

  private void removeTabViewAt(int position) {
    final TabView view = (TabView) slidingTabIndicator.getChildAt(position);
    slidingTabIndicator.removeViewAt(position);
    if (view != null) {
      view.reset();
      tabViewPool.release(view);
    }
    requestLayout();
  }

  private void animateToTab(int newPosition) {
    if (newPosition == Tab.INVALID_POSITION) {
      return;
    }

    if (getWindowToken() == null
        || !ViewCompat.isLaidOut(this)
        || slidingTabIndicator.childrenNeedLayout()) {
      
      
      setScrollPosition(newPosition, 0f, true);
      return;
    }

    final int startScrollX = getScrollX();
    final int targetScrollX = calculateScrollXForTab(newPosition, 0);

    if (startScrollX != targetScrollX) {
      ensureScrollAnimator();

      scrollAnimator.setIntValues(startScrollX, targetScrollX);
      scrollAnimator.start();
    }

    
    slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
  }

  private void ensureScrollAnimator() {
    if (scrollAnimator == null) {
      scrollAnimator = new ValueAnimator();
      scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      scrollAnimator.setDuration(tabIndicatorAnimationDuration);
      scrollAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              scrollTo((int) animator.getAnimatedValue(), 0);
            }
          });
    }
  }

  void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
    ensureScrollAnimator();
    scrollAnimator.addListener(listener);
  }

  
  private void setSelectedTabView(int position) {
    final int tabCount = slidingTabIndicator.getChildCount();
    if (position < tabCount) {
      for (int i = 0; i < tabCount; i++) {
        final View child = slidingTabIndicator.getChildAt(i);
        child.setSelected(i == position);
        child.setActivated(i == position);
      }
    }
  }

  
  public void selectTab(@Nullable Tab tab) {
    selectTab(tab, true);
  }

  
  public void selectTab(@Nullable final Tab tab, boolean updateIndicator) {
    final Tab currentTab = selectedTab;

    if (currentTab == tab) {
      if (currentTab != null) {
        dispatchTabReselected(tab);
        animateToTab(tab.getPosition());
      }
    } else {
      final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
      if (updateIndicator) {
        if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
            && newPosition != Tab.INVALID_POSITION) {
          
          setScrollPosition(newPosition, 0f, true);
        } else {
          animateToTab(newPosition);
        }
        if (newPosition != Tab.INVALID_POSITION) {
          setSelectedTabView(newPosition);
        }
      }
      
      
      selectedTab = tab;
      if (currentTab != null) {
        dispatchTabUnselected(currentTab);
      }
      if (tab != null) {
        dispatchTabSelected(tab);
      }
    }
  }

  private void dispatchTabSelected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabSelected(tab);
    }
  }

  private void dispatchTabUnselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabUnselected(tab);
    }
  }

  private void dispatchTabReselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabReselected(tab);
    }
  }

  private int calculateScrollXForTab(int position, float positionOffset) {
    if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
      final View selectedChild = slidingTabIndicator.getChildAt(position);
      final View nextChild =
          position + 1 < slidingTabIndicator.getChildCount()
              ? slidingTabIndicator.getChildAt(position + 1)
              : null;
      final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
      final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

      
      int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
      
      int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

      return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
          ? scrollBase + scrollOffset
          : scrollBase - scrollOffset;
    }
    return 0;
  }

  private void applyModeAndGravity() {
    int paddingStart = 0;
    if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
      
      paddingStart = Math.max(0, contentInsetStart - tabPaddingStart);
    }
    ViewCompat.setPaddingRelative(slidingTabIndicator, paddingStart, 0, 0, 0);

    switch (mode) {
      case MODE_AUTO:
      case MODE_FIXED:
        if (tabGravity == GRAVITY_START) {
          Log.w(
              LOG_TAG,
              "GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be"
                  + " used instead");
        }
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case MODE_SCROLLABLE:
        applyGravityForModeScrollable(tabGravity);
        break;
    }

    updateTabViews(true);
  }

  private void applyGravityForModeScrollable(int tabGravity) {
    switch (tabGravity) {
      case GRAVITY_CENTER:
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case GRAVITY_FILL:
        Log.w(
            LOG_TAG,
            "MODE_SCROLLABLE + GRAVITY_FILL is not supported, GRAVITY_START will be used"
                + " instead");
        
      case GRAVITY_START:
        slidingTabIndicator.setGravity(GravityCompat.START);
        break;
      default:
        break;
    }
  }

  void updateTabViews(final boolean requestLayout) {
    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
      View child = slidingTabIndicator.getChildAt(i);
      child.setMinimumWidth(getTabMinWidth());
      updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
      if (requestLayout) {
        child.requestLayout();
      }
    }
  }

  
  
  public static class Tab {

    
    public static final int INVALID_POSITION = -1;

    @Nullable private Object tag;
    @Nullable private Drawable icon;
    @Nullable private CharSequence text;
    
    
    
    @Nullable private CharSequence contentDesc;
    private int position = INVALID_POSITION;
    @Nullable private View customView;
    private @LabelVisibility int labelVisibilityMode = TAB_LABEL_VISIBILITY_LABELED;

    
    @Nullable public TabLayout parent;
    
    @NonNull public TabView view;

    
    public Tab() {
      
    }

    
    @Nullable
    public Object getTag() {
      return tag;
    }

    
    @NonNull
    public Tab setTag(@Nullable Object tag) {
      this.tag = tag;
      return this;
    }

    
    @Nullable
    public View getCustomView() {
      return customView;
    }

    
    @NonNull
    public Tab setCustomView(@Nullable View view) {
      customView = view;
      updateView();
      return this;
    }

    
    @NonNull
    public Tab setCustomView(@LayoutRes int resId) {
      final LayoutInflater inflater = LayoutInflater.from(view.getContext());
      return setCustomView(inflater.inflate(resId, view, false));
    }

    
    @Nullable
    public Drawable getIcon() {
      return icon;
    }

    
    public int getPosition() {
      return position;
    }

    void setPosition(int position) {
      this.position = position;
    }

    
    @Nullable
    public CharSequence getText() {
      return text;
    }

    
    @NonNull
    public Tab setIcon(@Nullable Drawable icon) {
      this.icon = icon;
      if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
        parent.updateTabViews(true);
      }
      updateView();
      if (BadgeUtils.USE_COMPAT_PARENT
          && view.hasBadgeDrawable()
          && view.badgeDrawable.isVisible()) {
        
        view.invalidate();
      }
      return this;
    }

    
    @NonNull
    public Tab setIcon(@DrawableRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setIcon(AppCompatResources.getDrawable(parent.getContext(), resId));
    }

    
    @NonNull
    public Tab setText(@Nullable CharSequence text) {
      if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
        
        
        view.setContentDescription(text);
      }

      this.text = text;
      updateView();
      return this;
    }

    
    @NonNull
    public Tab setText(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setText(parent.getResources().getText(resId));
    }

    
    @NonNull
    public BadgeDrawable getOrCreateBadge() {
      return view.getOrCreateBadge();
    }

    
    public void removeBadge() {
      view.removeBadge();
    }

    
    @Nullable
    public BadgeDrawable getBadge() {
      return view.getBadge();
    }

    
    @NonNull
    public Tab setTabLabelVisibility(@LabelVisibility int mode) {
      this.labelVisibilityMode = mode;
      if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
        parent.updateTabViews(true);
      }
      this.updateView();
      if (BadgeUtils.USE_COMPAT_PARENT
          && view.hasBadgeDrawable()
          && view.badgeDrawable.isVisible()) {
        
        view.invalidate();
      }
      return this;
    }

    
    @LabelVisibility
    public int getTabLabelVisibility() {
      return this.labelVisibilityMode;
    }

    
    public void select() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      parent.selectTab(this);
    }

    
    public boolean isSelected() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return parent.getSelectedTabPosition() == position;
    }

    
    @NonNull
    public Tab setContentDescription(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setContentDescription(parent.getResources().getText(resId));
    }

    
    @NonNull
    public Tab setContentDescription(@Nullable CharSequence contentDesc) {
      this.contentDesc = contentDesc;
      updateView();
      return this;
    }

    
    @Nullable
    public CharSequence getContentDescription() {
      
      
      return (view == null) ? null : view.getContentDescription();
    }

    void updateView() {
      if (view != null) {
        view.update();
      }
    }

    void reset() {
      parent = null;
      view = null;
      tag = null;
      icon = null;
      text = null;
      contentDesc = null;
      position = INVALID_POSITION;
      customView = null;
    }
  }

  
  public final class TabView extends LinearLayout {
    private Tab tab;
    private TextView textView;
    private ImageView iconView;
    @Nullable private View badgeAnchorView;
    @Nullable private BadgeDrawable badgeDrawable;

    @Nullable private View customView;
    @Nullable private TextView customTextView;
    @Nullable private ImageView customIconView;
    @Nullable private Drawable baseBackgroundDrawable;

    private int defaultMaxLines = 2;

    public TabView(@NonNull Context context) {
      super(context);
      updateBackgroundDrawable(context);
      ViewCompat.setPaddingRelative(
          this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom);
      setGravity(Gravity.CENTER);
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      setClickable(true);
      ViewCompat.setPointerIcon(
          this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
    }

    private void updateBackgroundDrawable(Context context) {
      if (tabBackgroundResId != 0) {
        baseBackgroundDrawable = AppCompatResources.getDrawable(context, tabBackgroundResId);
        if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
          baseBackgroundDrawable.setState(getDrawableState());
        }
      } else {
        baseBackgroundDrawable = null;
      }

      Drawable background;
      Drawable contentDrawable = new GradientDrawable();
      ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);

      if (tabRippleColorStateList != null) {
        GradientDrawable maskDrawable = new GradientDrawable();
        
        
        
        maskDrawable.setCornerRadius(0.00001F);
        maskDrawable.setColor(Color.WHITE);

        ColorStateList rippleColor =
            RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);

        
        
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          background =
              new RippleDrawable(
                  rippleColor,
                  unboundedRipple ? null : contentDrawable,
                  unboundedRipple ? null : maskDrawable);
        } else {
          Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
          DrawableCompat.setTintList(rippleDrawable, rippleColor);
          background = new LayerDrawable(new Drawable[] {contentDrawable, rippleDrawable});
        }
      } else {
        background = contentDrawable;
      }
      ViewCompat.setBackground(this, background);
      TabLayout.this.invalidate();
    }

    
    private void drawBackground(@NonNull Canvas canvas) {
      if (baseBackgroundDrawable != null) {
        baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
        baseBackgroundDrawable.draw(canvas);
      }
    }

    @Override
    protected void drawableStateChanged() {
      super.drawableStateChanged();
      boolean changed = false;
      int[] state = getDrawableState();
      if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
        changed |= baseBackgroundDrawable.setState(state);
      }

      if (changed) {
        invalidate();
        TabLayout.this.invalidate(); 
      }
    }

    @Override
    public boolean performClick() {
      final boolean handled = super.performClick();

      if (tab != null) {
        if (!handled) {
          playSoundEffect(SoundEffectConstants.CLICK);
        }
        tab.select();
        return true;
      } else {
        return handled;
      }
    }

    @Override
    public void setSelected(final boolean selected) {
      final boolean changed = isSelected() != selected;

      super.setSelected(selected);

      if (changed && selected && Build.VERSION.SDK_INT < 16) {
        
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
      }

      
      
      if (textView != null) {
        textView.setSelected(selected);
      }
      if (iconView != null) {
        iconView.setSelected(selected);
      }
      if (customView != null) {
        customView.setSelected(selected);
      }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
      super.onInitializeAccessibilityNodeInfo(info);
      if (badgeDrawable != null && badgeDrawable.isVisible()) {
        CharSequence customContentDescription = getContentDescription();
        info.setContentDescription(
            customContentDescription + ", " + badgeDrawable.getContentDescription());
      }
      AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
      infoCompat.setCollectionItemInfo(
          CollectionItemInfoCompat.obtain(
               0,
               1,
               tab.getPosition(),
               1,
               false,
               isSelected()));
      if (isSelected()) {
        infoCompat.setClickable(false);
        infoCompat.removeAction(AccessibilityActionCompat.ACTION_CLICK);
      }
      infoCompat.setRoleDescription("Tab");
    }

    @Override
    public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
      final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
      final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
      final int maxWidth = getTabMaxWidth();

      final int widthMeasureSpec;
      final int heightMeasureSpec = origHeightMeasureSpec;

      if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
        
        
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.AT_MOST);
      } else {
        
        widthMeasureSpec = origWidthMeasureSpec;
      }

      
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      
      if (textView != null) {
        float textSize = tabTextSize;
        int maxLines = defaultMaxLines;

        if (iconView != null && iconView.getVisibility() == VISIBLE) {
          
          maxLines = 1;
        } else if (textView != null && textView.getLineCount() > 1) {
          
          textSize = tabTextMultiLineSize;
        }

        final float curTextSize = textView.getTextSize();
        final int curLineCount = textView.getLineCount();
        final int curMaxLines = TextViewCompat.getMaxLines(textView);

        if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
          
          boolean updateTextView = true;

          if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
            
            
            
            
            
            final Layout layout = textView.getLayout();
            if (layout == null
                || approximateLineWidth(layout, 0, textSize)
                    > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
              updateTextView = false;
            }
          }

          if (updateTextView) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setMaxLines(maxLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
          }
        }
      }
    }

    void setTab(@Nullable final Tab tab) {
      if (tab != this.tab) {
        this.tab = tab;
        update();
      }
    }

    void reset() {
      setTab(null);
      setSelected(false);
    }

    final void update() {
      final Tab tab = this.tab;
      final View custom = tab != null ? tab.getCustomView() : null;
      if (custom != null) {
        final ViewParent customParent = custom.getParent();
        if (customParent != this) {
          if (customParent != null) {
            ((ViewGroup) customParent).removeView(custom);
          }
          addView(custom);
        }
        customView = custom;
        if (this.textView != null) {
          this.textView.setVisibility(GONE);
        }
        if (this.iconView != null) {
          this.iconView.setVisibility(GONE);
          this.iconView.setImageDrawable(null);
        }

        customTextView = custom.findViewById(android.R.id.text1);
        if (customTextView != null) {
          defaultMaxLines = TextViewCompat.getMaxLines(customTextView);
        }
        customIconView = custom.findViewById(android.R.id.icon);
      } else {
        
        if (customView != null) {
          removeView(customView);
          customView = null;
        }
        customTextView = null;
        customIconView = null;
      }

      if (customView == null) {
        
        if (this.iconView == null) {
          inflateAndAddDefaultIconView();
        }
        final Drawable icon =
            (tab != null && tab.getIcon() != null)
                ? DrawableCompat.wrap(tab.getIcon()).mutate()
                : null;
        if (icon != null) {
          DrawableCompat.setTintList(icon, tabIconTint);
          if (tabIconTintMode != null) {
            DrawableCompat.setTintMode(icon, tabIconTintMode);
          }
        }

        if (this.textView == null) {
          inflateAndAddDefaultTextView();
          defaultMaxLines = TextViewCompat.getMaxLines(this.textView);
        }
        TextViewCompat.setTextAppearance(this.textView, tabTextAppearance);
        if (tabTextColors != null) {
          this.textView.setTextColor(tabTextColors);
        }
        updateTextAndIcon(this.textView, this.iconView);

        tryUpdateBadgeAnchor();
        addOnLayoutChangeListener(iconView);
        addOnLayoutChangeListener(textView);
      } else {
        
        if (customTextView != null || customIconView != null) {
          updateTextAndIcon(customTextView, customIconView);
        }
      }

      if (tab != null && !TextUtils.isEmpty(tab.contentDesc)) {
        
        
        setContentDescription(tab.contentDesc);
      }
      
      setSelected(tab != null && tab.isSelected());
    }

    private void inflateAndAddDefaultIconView() {
      ViewGroup iconViewParent = this;
      if (BadgeUtils.USE_COMPAT_PARENT) {
        iconViewParent = createPreApi18BadgeAnchorRoot();
        addView(iconViewParent, 0);
      }
      this.iconView =
          (ImageView)
              LayoutInflater.from(getContext())
                  .inflate(R.layout.design_layout_tab_icon, iconViewParent, false);
      iconViewParent.addView(iconView, 0);
    }

    private void inflateAndAddDefaultTextView() {
      ViewGroup textViewParent = this;
      if (BadgeUtils.USE_COMPAT_PARENT) {
        textViewParent = createPreApi18BadgeAnchorRoot();
        addView(textViewParent);
      }
      this.textView =
          (TextView)
              LayoutInflater.from(getContext())
                  .inflate(R.layout.design_layout_tab_text, textViewParent, false);
      textViewParent.addView(textView);
    }

    @NonNull
    private FrameLayout createPreApi18BadgeAnchorRoot() {
      FrameLayout frameLayout = new FrameLayout(getContext());
      FrameLayout.LayoutParams layoutparams =
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      frameLayout.setLayoutParams(layoutparams);
      return frameLayout;
    }

    
    @NonNull
    private BadgeDrawable getOrCreateBadge() {
      
      if (badgeDrawable == null) {
        badgeDrawable = BadgeDrawable.create(getContext());
      }
      tryUpdateBadgeAnchor();
      if (badgeDrawable == null) {
        throw new IllegalStateException("Unable to create badge");
      }
      return badgeDrawable;
    }

    @Nullable
    private BadgeDrawable getBadge() {
      return badgeDrawable;
    }

    private void removeBadge() {
      if (badgeAnchorView != null) {
        tryRemoveBadgeFromAnchor();
      }
      badgeDrawable = null;
    }

    private void addOnLayoutChangeListener(@Nullable final View view) {
      if (view == null) {
        return;
      }
      view.addOnLayoutChangeListener(
          new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                View v,
                int left,
                int top,
                int right,
                int bottom,
                int oldLeft,
                int oldTop,
                int oldRight,
                int oldBottom) {
              if (view.getVisibility() == VISIBLE) {
                tryUpdateBadgeDrawableBounds(view);
              }
            }
          });
    }

    private void tryUpdateBadgeAnchor() {
      if (!hasBadgeDrawable()) {
        return;
      }
      if (customView != null) {
        
        tryRemoveBadgeFromAnchor();
      } else {
        if (iconView != null && tab != null && tab.getIcon() != null) {
          if (badgeAnchorView != iconView) {
            tryRemoveBadgeFromAnchor();
            
            tryAttachBadgeToAnchor(iconView);
          } else {
            tryUpdateBadgeDrawableBounds(iconView);
          }
        } else if (textView != null
            && tab != null
            && tab.getTabLabelVisibility() == TAB_LABEL_VISIBILITY_LABELED) {
          if (badgeAnchorView != textView) {
            tryRemoveBadgeFromAnchor();
            
            tryAttachBadgeToAnchor(textView);
          } else {
            tryUpdateBadgeDrawableBounds(textView);
          }
        } else {
          tryRemoveBadgeFromAnchor();
        }
      }
    }

    private void tryAttachBadgeToAnchor(@Nullable View anchorView) {
      if (!hasBadgeDrawable()) {
        return;
      }
      if (anchorView != null) {
        clipViewToPaddingForBadge(false);
        BadgeUtils.attachBadgeDrawable(
            badgeDrawable, anchorView, getCustomParentForBadge(anchorView));
        badgeAnchorView = anchorView;
      }
    }

    private void tryRemoveBadgeFromAnchor() {
      if (!hasBadgeDrawable()) {
        return;
      }
      clipViewToPaddingForBadge(true);
      if (badgeAnchorView != null) {
        BadgeUtils.detachBadgeDrawable(
            badgeDrawable, badgeAnchorView, getCustomParentForBadge(badgeAnchorView));
        badgeAnchorView = null;
      }
    }

    private void clipViewToPaddingForBadge(boolean flag) {
      
      
      setClipChildren(flag);
      setClipToPadding(flag);
      ViewGroup parent = (ViewGroup) getParent();
      if (parent != null) {
        parent.setClipChildren(flag);
        parent.setClipToPadding(flag);
      }
    }

    final void updateOrientation() {
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      if (customTextView != null || customIconView != null) {
        updateTextAndIcon(customTextView, customIconView);
      } else {
        updateTextAndIcon(textView, iconView);
      }
    }

    private void updateTextAndIcon(
        @Nullable final TextView textView, @Nullable final ImageView iconView) {
      final Drawable icon =
          (tab != null && tab.getIcon() != null)
              ? DrawableCompat.wrap(tab.getIcon()).mutate()
              : null;
      final CharSequence text = tab != null ? tab.getText() : null;

      if (iconView != null) {
        if (icon != null) {
          iconView.setImageDrawable(icon);
          iconView.setVisibility(VISIBLE);
          setVisibility(VISIBLE);
        } else {
          iconView.setVisibility(GONE);
          iconView.setImageDrawable(null);
        }
      }

      final boolean hasText = !TextUtils.isEmpty(text);
      if (textView != null) {
        if (hasText) {
          textView.setText(text);
          if (tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED) {
            textView.setVisibility(VISIBLE);
          } else {
            textView.setVisibility(GONE);
          }
          setVisibility(VISIBLE);
        } else {
          textView.setVisibility(GONE);
          textView.setText(null);
        }
      }

      if (iconView != null) {
        MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
        int iconMargin = 0;
        if (hasText && iconView.getVisibility() == VISIBLE) {
          
          iconMargin = (int) ViewUtils.dpToPx(getContext(), DEFAULT_GAP_TEXT_ICON);
        }
        if (inlineLabel) {
          if (iconMargin != MarginLayoutParamsCompat.getMarginEnd(lp)) {
            MarginLayoutParamsCompat.setMarginEnd(lp, iconMargin);
            lp.bottomMargin = 0;
            
            iconView.setLayoutParams(lp);
            iconView.requestLayout();
          }
        } else {
          if (iconMargin != lp.bottomMargin) {
            lp.bottomMargin = iconMargin;
            MarginLayoutParamsCompat.setMarginEnd(lp, 0);
            
            iconView.setLayoutParams(lp);
            iconView.requestLayout();
          }
        }
      }

      final CharSequence contentDesc = tab != null ? tab.contentDesc : null;
      TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
    }

    private void tryUpdateBadgeDrawableBounds(@NonNull View anchor) {
      
      if (hasBadgeDrawable() && anchor == badgeAnchorView) {
        BadgeUtils.setBadgeDrawableBounds(badgeDrawable, anchor, getCustomParentForBadge(anchor));
      }
    }

    private boolean hasBadgeDrawable() {
      return badgeDrawable != null;
    }

    @Nullable
    private FrameLayout getCustomParentForBadge(@NonNull View anchor) {
      if (anchor != iconView && anchor != textView) {
        return null;
      }
      return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout) anchor.getParent()) : null;
    }

    
    private int getContentWidth() {
      boolean initialized = false;
      int left = 0;
      int right = 0;

      for (View view : new View[] {textView, iconView, customView}) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
          left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
          right = initialized ? Math.max(right, view.getRight()) : view.getRight();
          initialized = true;
        }
      }

      return right - left;
    }

    @Nullable
    public Tab getTab() {
      return tab;
    }

    
    private float approximateLineWidth(@NonNull Layout layout, int line, float textSize) {
      return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
    }
  }

  class SlidingTabIndicator extends LinearLayout {
    private int selectedIndicatorHeight;
    @NonNull private final Paint selectedIndicatorPaint;
    @NonNull private final GradientDrawable defaultSelectionIndicator;

    int selectedPosition = -1;
    float selectionOffset;

    private int layoutDirection = -1;

    int indicatorLeft = -1;
    int indicatorRight = -1;

    ValueAnimator indicatorAnimator;
    private int animationStartLeft = -1;
    private int animationStartRight = -1;

    SlidingTabIndicator(Context context) {
      super(context);
      setWillNotDraw(false);
      selectedIndicatorPaint = new Paint();
      defaultSelectionIndicator = new GradientDrawable();
    }

    void setSelectedIndicatorColor(int color) {
      if (selectedIndicatorPaint.getColor() != color) {
        selectedIndicatorPaint.setColor(color);
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    void setSelectedIndicatorHeight(int height) {
      if (selectedIndicatorHeight != height) {
        selectedIndicatorHeight = height;
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    boolean childrenNeedLayout() {
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (child.getWidth() <= 0) {
          return true;
        }
      }
      return false;
    }

    void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        indicatorAnimator.cancel();
      }

      selectedPosition = position;
      selectionOffset = positionOffset;
      updateIndicatorPosition();
    }

    float getIndicatorPosition() {
      return selectedPosition + selectionOffset;
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
      super.onRtlPropertiesChanged(layoutDirection);

      
      
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        if (this.layoutDirection != layoutDirection) {
          requestLayout();
          this.layoutDirection = layoutDirection;
        }
      }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
        
        
        return;
      }

      
      
      
      if ((tabGravity == GRAVITY_CENTER) || mode == MODE_AUTO) {
        final int count = getChildCount();

        
        int largestTabWidth = 0;
        for (int i = 0, z = count; i < z; i++) {
          View child = getChildAt(i);
          if (child.getVisibility() == VISIBLE) {
            largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
          }
        }

        if (largestTabWidth <= 0) {
          
          return;
        }

        final int gutter = (int) ViewUtils.dpToPx(getContext(), FIXED_WRAP_GUTTER_MIN);
        boolean remeasure = false;

        if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
          
          
          for (int i = 0; i < count; i++) {
            final LinearLayout.LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.width != largestTabWidth || lp.weight != 0) {
              lp.width = largestTabWidth;
              lp.weight = 0;
              remeasure = true;
            }
          }
        } else {
          
          
          
          tabGravity = GRAVITY_FILL;
          updateTabViews(false);
          remeasure = true;
        }

        if (remeasure) {
          
          super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);

      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        
        
        
        
        updateOrRecreateIndicatorAnimation(
             false, selectedPosition,  -1);
      } else {
        
        updateIndicatorPosition();
      }
    }

    private void updateIndicatorPosition() {
      final View selectedTitle = getChildAt(selectedPosition);
      int left;
      int right;

      if (selectedTitle != null && selectedTitle.getWidth() > 0) {
        left = selectedTitle.getLeft();
        right = selectedTitle.getRight();

        if (!tabIndicatorFullWidth && selectedTitle instanceof TabView) {
          calculateTabViewContentBounds((TabView) selectedTitle, tabViewContentBounds);
          left = (int) tabViewContentBounds.left;
          right = (int) tabViewContentBounds.right;
        }

        if (selectionOffset > 0f && selectedPosition < getChildCount() - 1) {
          
          View nextTitle = getChildAt(selectedPosition + 1);
          int nextTitleLeft = nextTitle.getLeft();
          int nextTitleRight = nextTitle.getRight();

          if (!tabIndicatorFullWidth && nextTitle instanceof TabView) {
            calculateTabViewContentBounds((TabView) nextTitle, tabViewContentBounds);
            nextTitleLeft = (int) tabViewContentBounds.left;
            nextTitleRight = (int) tabViewContentBounds.right;
          }

          left = (int) (selectionOffset * nextTitleLeft + (1.0f - selectionOffset) * left);
          right = (int) (selectionOffset * nextTitleRight + (1.0f - selectionOffset) * right);
        }

      } else {
        left = right = -1;
      }

      setIndicatorPosition(left, right);
    }

    void setIndicatorPosition(int left, int right) {
      if (left != indicatorLeft || right != indicatorRight) {
        
        indicatorLeft = left;
        indicatorRight = right;
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    void animateIndicatorToPosition(final int position, int duration) {
      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        indicatorAnimator.cancel();
      }

      updateOrRecreateIndicatorAnimation( true, position, duration);
    }

    private void updateOrRecreateIndicatorAnimation(
        boolean recreateAnimation, final int position, int duration) {
      final View targetView = getChildAt(position);
      if (targetView == null) {
        
        updateIndicatorPosition();
        return;
      }

      int targetLeft = targetView.getLeft();
      int targetRight = targetView.getRight();

      if (!tabIndicatorFullWidth && targetView instanceof TabView) {
        calculateTabViewContentBounds((TabView) targetView, tabViewContentBounds);
        targetLeft = (int) tabViewContentBounds.left;
        targetRight = (int) tabViewContentBounds.right;
      }

      
      final int finalTargetLeft = targetLeft;
      final int finalTargetRight = targetRight;

      
      final int startLeft = indicatorLeft;
      final int startRight = indicatorRight;

      
      if (startLeft == finalTargetLeft && startRight == finalTargetRight) {
        return;
      }

      
      
      if (recreateAnimation) {
        animationStartLeft = startLeft;
        animationStartRight = startRight;
      }

      
      
      ValueAnimator.AnimatorUpdateListener updateListener =
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
              final float fraction = valueAnimator.getAnimatedFraction();
              setIndicatorPosition(
                  AnimationUtils.lerp(animationStartLeft, finalTargetLeft, fraction),
                  AnimationUtils.lerp(animationStartRight, finalTargetRight, fraction));
            }
          };

      if (recreateAnimation) {
        
        ValueAnimator animator = indicatorAnimator = new ValueAnimator();
        animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        animator.setDuration(duration);
        animator.setFloatValues(0, 1);
        animator.addUpdateListener(updateListener);
        animator.addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationStart(Animator animator) {
                selectedPosition = position;
              }

              @Override
              public void onAnimationEnd(Animator animator) {
                selectedPosition = position;
                selectionOffset = 0f;
              }
            });
        animator.start();
      } else {
        
        indicatorAnimator.removeAllUpdateListeners();
        indicatorAnimator.addUpdateListener(updateListener);
      }
    }

    
    private void calculateTabViewContentBounds(
        @NonNull TabView tabView, @NonNull RectF contentBounds) {
      int tabViewContentWidth = tabView.getContentWidth();
      int minIndicatorWidth = (int) ViewUtils.dpToPx(getContext(), MIN_INDICATOR_WIDTH);

      if (tabViewContentWidth < minIndicatorWidth) {
        tabViewContentWidth = minIndicatorWidth;
      }

      int tabViewCenter = (tabView.getLeft() + tabView.getRight()) / 2;
      int contentLeftBounds = tabViewCenter - (tabViewContentWidth / 2);
      int contentRightBounds = tabViewCenter + (tabViewContentWidth / 2);

      contentBounds.set(contentLeftBounds, 0, contentRightBounds, 0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      int indicatorHeight = 0;
      if (tabSelectedIndicator != null) {
        indicatorHeight = tabSelectedIndicator.getIntrinsicHeight();
      }
      if (selectedIndicatorHeight >= 0) {
        indicatorHeight = selectedIndicatorHeight;
      }

      int indicatorTop = 0;
      int indicatorBottom = 0;

      switch (tabIndicatorGravity) {
        case INDICATOR_GRAVITY_BOTTOM:
          indicatorTop = getHeight() - indicatorHeight;
          indicatorBottom = getHeight();
          break;
        case INDICATOR_GRAVITY_CENTER:
          indicatorTop = (getHeight() - indicatorHeight) / 2;
          indicatorBottom = (getHeight() + indicatorHeight) / 2;
          break;
        case INDICATOR_GRAVITY_TOP:
          indicatorTop = 0;
          indicatorBottom = indicatorHeight;
          break;
        case INDICATOR_GRAVITY_STRETCH:
          indicatorTop = 0;
          indicatorBottom = getHeight();
          break;
        default:
          break;
      }

      
      if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
        Drawable selectedIndicator;
        selectedIndicator =
            DrawableCompat.wrap(
                tabSelectedIndicator != null ? tabSelectedIndicator : defaultSelectionIndicator)
                .mutate();
        selectedIndicator.setBounds(indicatorLeft, indicatorTop, indicatorRight, indicatorBottom);
        if (selectedIndicatorPaint != null) {
          if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
            
            selectedIndicator.setColorFilter(
                selectedIndicatorPaint.getColor(), PorterDuff.Mode.SRC_IN);
          } else {
            DrawableCompat.setTint(selectedIndicator, selectedIndicatorPaint.getColor());
          }
        }
        selectedIndicator.draw(canvas);
      }

      
      super.draw(canvas);
    }
  }

  @NonNull
  private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
    final int[][] states = new int[2][];
    final int[] colors = new int[2];
    int i = 0;

    states[i] = SELECTED_STATE_SET;
    colors[i] = selectedColor;
    i++;

    
    states[i] = EMPTY_STATE_SET;
    colors[i] = defaultColor;
    i++;

    return new ColorStateList(states, colors);
  }

  @Dimension(unit = Dimension.DP)
  private int getDefaultHeight() {
    boolean hasIconAndText = false;
    for (int i = 0, count = tabs.size(); i < count; i++) {
      Tab tab = tabs.get(i);
      if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
        hasIconAndText = true;
        break;
      }
    }
    return (hasIconAndText && !inlineLabel) ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
  }

  private int getTabMinWidth() {
    if (requestedTabMinWidth != INVALID_WIDTH) {
      
      return requestedTabMinWidth;
    }
    
    return (mode == MODE_SCROLLABLE || mode == MODE_AUTO) ? scrollableTabMinWidth : 0;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    
    
    
    
    return generateDefaultLayoutParams();
  }

  int getTabMaxWidth() {
    return tabMaxWidth;
  }

  
  public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
    @NonNull private final WeakReference<TabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
      previousScrollState = scrollState;
      scrollState = state;
    }

    @Override
    public void onPageScrolled(
        final int position, final float positionOffset, final int positionOffsetPixels) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        
        
        final boolean updateText =
            scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
        
        
        
        final boolean updateIndicator =
            !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        
        
        final boolean updateIndicator =
            scrollState == SCROLL_STATE_IDLE
                || (scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
  }

  
  public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private final ViewPager viewPager;

    public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
      this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(@NonNull TabLayout.Tab tab) {
      viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
      
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
      
    }
  }

  private class PagerAdapterObserver extends DataSetObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateFromPagerAdapter();
    }

    @Override
    public void onInvalidated() {
      populateFromPagerAdapter();
    }
  }

  private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
    private boolean autoRefresh;

    AdapterChangeListener() {}

    @Override
    public void onAdapterChanged(
        @NonNull ViewPager viewPager,
        @Nullable PagerAdapter oldAdapter,
        @Nullable PagerAdapter newAdapter) {
      if (TabLayout.this.viewPager == viewPager) {
        setPagerAdapter(newAdapter, autoRefresh);
      }
    }

    void setAutoRefresh(boolean autoRefresh) {
      this.autoRefresh = autoRefresh;
    }
  }
}
