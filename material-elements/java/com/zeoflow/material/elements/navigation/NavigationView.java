

package com.zeoflow.material.elements.navigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.zeoflow.material.elements.internal.NavigationMenu;
import com.zeoflow.material.elements.internal.NavigationMenuPresenter;
import com.zeoflow.material.elements.internal.ScrimInsetsFrameLayout;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class NavigationView extends ScrimInsetsFrameLayout {

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  private static final int DEF_STYLE_RES = R.style.Widget_Design_NavigationView;
  private static final int PRESENTER_NAVIGATION_VIEW_ID = 1;

  @NonNull private final NavigationMenu menu;
  private final NavigationMenuPresenter presenter = new NavigationMenuPresenter();

  OnNavigationItemSelectedListener listener;
  private final int maxWidth;

  private final int[] tmpLocation = new int[2];

  private MenuInflater menuInflater;
  private OnGlobalLayoutListener onGlobalLayoutListener;

  public NavigationView(@NonNull Context context) {
    this(context, null);
  }

  public NavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.navigationViewStyle);
  }

  public NavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    
    this.menu = new NavigationMenu(context);

    
    TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.NavigationView,
            defStyleAttr,
            DEF_STYLE_RES);

    if (a.hasValue(R.styleable.NavigationView_android_background)) {
      ViewCompat.setBackground(this, a.getDrawable(R.styleable.NavigationView_android_background));
    }

    
    
    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      Drawable orig = getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      if (orig instanceof ColorDrawable) {
        materialShapeDrawable.setFillColor(
            ColorStateList.valueOf(((ColorDrawable) orig).getColor()));
      }
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

    if (a.hasValue(R.styleable.NavigationView_elevation)) {
      setElevation(a.getDimensionPixelSize(R.styleable.NavigationView_elevation, 0));
    }
    setFitsSystemWindows(a.getBoolean(R.styleable.NavigationView_android_fitsSystemWindows, false));

    maxWidth = a.getDimensionPixelSize(R.styleable.NavigationView_android_maxWidth, 0);

    final ColorStateList itemIconTint;
    if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
      itemIconTint = a.getColorStateList(R.styleable.NavigationView_itemIconTint);
    } else {
      itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
    }

    boolean textAppearanceSet = false;
    int textAppearance = 0;
    if (a.hasValue(R.styleable.NavigationView_itemTextAppearance)) {
      textAppearance = a.getResourceId(R.styleable.NavigationView_itemTextAppearance, 0);
      textAppearanceSet = true;
    }

    if (a.hasValue(R.styleable.NavigationView_itemIconSize)) {
      setItemIconSize(a.getDimensionPixelSize(R.styleable.NavigationView_itemIconSize, 0));
    }

    ColorStateList itemTextColor = null;
    if (a.hasValue(R.styleable.NavigationView_itemTextColor)) {
      itemTextColor = a.getColorStateList(R.styleable.NavigationView_itemTextColor);
    }

    if (!textAppearanceSet && itemTextColor == null) {
      
      itemTextColor = createDefaultColorStateList(android.R.attr.textColorPrimary);
    }

    Drawable itemBackground = a.getDrawable(R.styleable.NavigationView_itemBackground);
    
    
    if (itemBackground == null && hasShapeAppearance(a)) {
      itemBackground = createDefaultItemBackground(a);
    }

    if (a.hasValue(R.styleable.NavigationView_itemHorizontalPadding)) {
      final int itemHorizontalPadding =
          a.getDimensionPixelSize(R.styleable.NavigationView_itemHorizontalPadding, 0);
      presenter.setItemHorizontalPadding(itemHorizontalPadding);
    }
    final int itemIconPadding =
        a.getDimensionPixelSize(R.styleable.NavigationView_itemIconPadding, 0);

    setItemMaxLines(a.getInt(R.styleable.NavigationView_itemMaxLines, 1));

    this.menu.setCallback(
        new MenuBuilder.Callback() {
          @Override
          public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            return listener != null && listener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });
    presenter.setId(PRESENTER_NAVIGATION_VIEW_ID);
    presenter.initForMenu(context, this.menu);
    presenter.setItemIconTintList(itemIconTint);
    presenter.setOverScrollMode(getOverScrollMode());
    if (textAppearanceSet) {
      presenter.setItemTextAppearance(textAppearance);
    }
    presenter.setItemTextColor(itemTextColor);
    presenter.setItemBackground(itemBackground);
    presenter.setItemIconPadding(itemIconPadding);
    this.menu.addMenuPresenter(presenter);
    addView((View) presenter.getMenuView(this));

    if (a.hasValue(R.styleable.NavigationView_menu)) {
      inflateMenu(a.getResourceId(R.styleable.NavigationView_menu, 0));
    }

    if (a.hasValue(R.styleable.NavigationView_headerLayout)) {
      inflateHeaderView(a.getResourceId(R.styleable.NavigationView_headerLayout, 0));
    }

    a.recycle();

    setupInsetScrimsListener();
  }

  @Override
  public void setOverScrollMode(int overScrollMode) {
    super.setOverScrollMode(overScrollMode);
    if (presenter != null) {
      presenter.setOverScrollMode(overScrollMode);
    }
  }

  private boolean hasShapeAppearance(@NonNull TintTypedArray a) {
    return a.hasValue(R.styleable.NavigationView_itemShapeAppearance)
        || a.hasValue(R.styleable.NavigationView_itemShapeAppearanceOverlay);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  public void setElevation(float elevation) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      super.setElevation(elevation);
    }
    MaterialShapeUtils.setElevation(this, elevation);
  }

  
  @NonNull
  private final Drawable createDefaultItemBackground(@NonNull TintTypedArray a) {
    int shapeAppearanceResId = a.getResourceId(R.styleable.NavigationView_itemShapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.NavigationView_itemShapeAppearanceOverlay, 0);
    MaterialShapeDrawable materialShapeDrawable =
        new MaterialShapeDrawable(
            ShapeAppearanceModel.builder(
                    getContext(), shapeAppearanceResId, shapeAppearanceOverlayResId)
                .build());
    materialShapeDrawable.setFillColor(
        MaterialResources.getColorStateList(
            getContext(), a, R.styleable.NavigationView_itemShapeFillColor));

    int insetLeft = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetStart, 0);
    int insetTop = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetTop, 0);
    int insetRight = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetEnd, 0);
    int insetBottom = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetBottom, 0);
    return new InsetDrawable(materialShapeDrawable, insetLeft, insetTop, insetRight, insetBottom);
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState state = new SavedState(superState);
    state.menuState = new Bundle();
    menu.savePresenterStates(state.menuState);
    return state;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable savedState) {
    if (!(savedState instanceof SavedState)) {
      super.onRestoreInstanceState(savedState);
      return;
    }
    SavedState state = (SavedState) savedState;
    super.onRestoreInstanceState(state.getSuperState());
    menu.restorePresenterStates(state.menuState);
  }

  
  public void setNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    this.listener = listener;
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    switch (MeasureSpec.getMode(widthSpec)) {
      case MeasureSpec.EXACTLY:
        
        break;
      case MeasureSpec.AT_MOST:
        widthSpec =
            MeasureSpec.makeMeasureSpec(
                Math.min(MeasureSpec.getSize(widthSpec), maxWidth), MeasureSpec.EXACTLY);
        break;
      case MeasureSpec.UNSPECIFIED:
        widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
        break;
    }
    
    super.onMeasure(widthSpec, heightSpec);
  }

  
  @RestrictTo(LIBRARY_GROUP)
  @Override
  protected void onInsetsChanged(@NonNull WindowInsetsCompat insets) {
    presenter.dispatchApplyWindowInsets(insets);
  }

  
  public void inflateMenu(int resId) {
    presenter.setUpdateSuspended(true);
    getMenuInflater().inflate(resId, menu);
    presenter.setUpdateSuspended(false);
    presenter.updateMenuView(false);
  }

  
  @NonNull
  public Menu getMenu() {
    return menu;
  }

  
  public View inflateHeaderView(@LayoutRes int res) {
    return presenter.inflateHeaderView(res);
  }

  
  public void addHeaderView(@NonNull View view) {
    presenter.addHeaderView(view);
  }

  
  public void removeHeaderView(@NonNull View view) {
    presenter.removeHeaderView(view);
  }

  
  public int getHeaderCount() {
    return presenter.getHeaderCount();
  }

  
  public View getHeaderView(int index) {
    return presenter.getHeaderView(index);
  }

  
  @Nullable
  public ColorStateList getItemIconTintList() {
    return presenter.getItemTintList();
  }

  
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    presenter.setItemIconTintList(tint);
  }

  
  @Nullable
  public ColorStateList getItemTextColor() {
    return presenter.getItemTextColor();
  }

  
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    presenter.setItemTextColor(textColor);
  }

  
  @Nullable
  public Drawable getItemBackground() {
    return presenter.getItemBackground();
  }

  
  public void setItemBackgroundResource(@DrawableRes int resId) {
    setItemBackground(ContextCompat.getDrawable(getContext(), resId));
  }

  
  public void setItemBackground(@Nullable Drawable itemBackground) {
    presenter.setItemBackground(itemBackground);
  }

  
  @Dimension
  public int getItemHorizontalPadding() {
    return presenter.getItemHorizontalPadding();
  }

  
  public void setItemHorizontalPadding(@Dimension int padding) {
    presenter.setItemHorizontalPadding(padding);
  }

  
  public void setItemHorizontalPaddingResource(@DimenRes int paddingResource) {
    presenter.setItemHorizontalPadding(getResources().getDimensionPixelSize(paddingResource));
  }

  
  @Dimension
  public int getItemIconPadding() {
    return presenter.getItemIconPadding();
  }

  
  public void setItemIconPadding(@Dimension int padding) {
    presenter.setItemIconPadding(padding);
  }

  
  public void setItemIconPaddingResource(int paddingResource) {
    presenter.setItemIconPadding(getResources().getDimensionPixelSize(paddingResource));
  }

  
  public void setCheckedItem(@IdRes int id) {
    MenuItem item = menu.findItem(id);
    if (item != null) {
      presenter.setCheckedItem((MenuItemImpl) item);
    }
  }

  
  public void setCheckedItem(@NonNull MenuItem checkedItem) {
    MenuItem item = menu.findItem(checkedItem.getItemId());
    if (item != null) {
      presenter.setCheckedItem((MenuItemImpl) item);
    } else {
      throw new IllegalArgumentException(
          "Called setCheckedItem(MenuItem) with an item that is not in the current menu.");
    }
  }

  
  @Nullable
  public MenuItem getCheckedItem() {
    return presenter.getCheckedItem();
  }

  
  public void setItemTextAppearance(@StyleRes int resId) {
    presenter.setItemTextAppearance(resId);
  }

  
  public void setItemIconSize(@Dimension int iconSize) {
    presenter.setItemIconSize(iconSize);
  }

  
  public void setItemMaxLines(int itemMaxLines) {
    presenter.setItemMaxLines(itemMaxLines);
  }

  
  public int getItemMaxLines() {
    return presenter.getItemMaxLines();
  }

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  @Nullable
  private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(
        new int[][] {DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
        new int[] {
          baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
        });
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (Build.VERSION.SDK_INT < 16) {
      getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
    } else {
      getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }
  }

  
  private void setupInsetScrimsListener() {
    onGlobalLayoutListener = new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        getLocationOnScreen(tmpLocation);
        boolean isBehindStatusBar = tmpLocation[1] == 0;
        presenter.setBehindStatusBar(isBehindStatusBar);
        setDrawTopInsetForeground(isBehindStatusBar);

        Context context = getContext();
        if (context instanceof Activity && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          boolean isBehindSystemNav =
              ((Activity) context).findViewById(android.R.id.content).getHeight()
                  == getHeight();
          boolean hasNonZeroAlpha =
              Color.alpha(((Activity) context).getWindow().getNavigationBarColor()) != 0;

          setDrawBottomInsetForeground(isBehindSystemNav && hasNonZeroAlpha);
        }
      }
    };

    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            onGlobalLayoutListener);
  }

  
  public interface OnNavigationItemSelectedListener {

    
    public boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  
  public static class SavedState extends AbsSavedState {
    @Nullable public Bundle menuState;

    public SavedState(@NonNull Parcel in, @Nullable ClassLoader loader) {
      super(in, loader);
      menuState = in.readBundle(loader);
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeBundle(menuState);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
