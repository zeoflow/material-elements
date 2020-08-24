

package com.zeoflow.material.elements.bottomnavigation;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.zeoflow.material.elements.badge.BadgeDrawable;
import com.zeoflow.material.elements.behavior.HideBottomViewOnScrollBehavior;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.internal.ViewUtils.RelativePadding;
import com.zeoflow.material.elements.resources.MaterialResources;
import com.zeoflow.material.elements.ripple.RippleUtils;
import com.zeoflow.material.elements.shape.MaterialShapeDrawable;
import com.zeoflow.material.elements.shape.MaterialShapeUtils;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class BottomNavigationView extends FrameLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_BottomNavigationView;
  private static final int MENU_PRESENTER_ID = 1;

  @NonNull private final MenuBuilder menu;
  @NonNull @VisibleForTesting final BottomNavigationMenuView menuView;
  private final BottomNavigationPresenter presenter = new BottomNavigationPresenter();
  @Nullable private ColorStateList itemRippleColor;
  private MenuInflater menuInflater;

  private OnNavigationItemSelectedListener selectedListener;
  private OnNavigationItemReselectedListener reselectedListener;

  public BottomNavigationView(@NonNull Context context) {
    this(context, null);
  }

  public BottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomNavigationStyle);
  }

  public BottomNavigationView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    
    this.menu = new BottomNavigationMenu(context);

    menuView = new BottomNavigationMenuView(context);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    menuView.setLayoutParams(params);

    presenter.setBottomNavigationMenuView(menuView);
    presenter.setId(MENU_PRESENTER_ID);
    menuView.setPresenter(presenter);
    this.menu.addMenuPresenter(presenter);
    presenter.initForMenu(getContext(), this.menu);

    
    TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.BottomNavigationView,
            defStyleAttr,
            R.style.Widget_Design_BottomNavigationView,
            R.styleable.BottomNavigationView_itemTextAppearanceInactive,
            R.styleable.BottomNavigationView_itemTextAppearanceActive);

    if (a.hasValue(R.styleable.BottomNavigationView_itemIconTint)) {
      menuView.setIconTintList(a.getColorStateList(R.styleable.BottomNavigationView_itemIconTint));
    } else {
      menuView.setIconTintList(
          menuView.createDefaultColorStateList(android.R.attr.textColorSecondary));
    }

    setItemIconSize(
        a.getDimensionPixelSize(
            R.styleable.BottomNavigationView_itemIconSize,
            getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_icon_size)));
    if (a.hasValue(R.styleable.BottomNavigationView_itemTextAppearanceInactive)) {
      setItemTextAppearanceInactive(
          a.getResourceId(R.styleable.BottomNavigationView_itemTextAppearanceInactive, 0));
    }
    if (a.hasValue(R.styleable.BottomNavigationView_itemTextAppearanceActive)) {
      setItemTextAppearanceActive(
          a.getResourceId(R.styleable.BottomNavigationView_itemTextAppearanceActive, 0));
    }

    if (a.hasValue(R.styleable.BottomNavigationView_itemTextColor)) {
      setItemTextColor(a.getColorStateList(R.styleable.BottomNavigationView_itemTextColor));
    }

    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      
      ViewCompat.setBackground(this, createMaterialShapeDrawableBackground(context));
    }

    if (a.hasValue(R.styleable.BottomNavigationView_elevation)) {
      ViewCompat.setElevation(
          this, a.getDimensionPixelSize(R.styleable.BottomNavigationView_elevation, 0));
    }

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.BottomNavigationView_backgroundTint);
    DrawableCompat.setTintList(getBackground().mutate(), backgroundTint);

    setLabelVisibilityMode(
        a.getInteger(
            R.styleable.BottomNavigationView_labelVisibilityMode,
            LabelVisibilityMode.LABEL_VISIBILITY_AUTO));
    setItemHorizontalTranslationEnabled(
        a.getBoolean(R.styleable.BottomNavigationView_itemHorizontalTranslationEnabled, true));

    int itemBackground = a.getResourceId(R.styleable.BottomNavigationView_itemBackground, 0);
    if (itemBackground != 0) {
      menuView.setItemBackgroundRes(itemBackground);
    } else {
      ColorStateList itemRippleColor =
          MaterialResources.getColorStateList(
              context, a, R.styleable.BottomNavigationView_itemRippleColor);
      setItemRippleColor(itemRippleColor);
    }

    if (a.hasValue(R.styleable.BottomNavigationView_menu)) {
      inflateMenu(a.getResourceId(R.styleable.BottomNavigationView_menu, 0));
    }
    a.recycle();

    addView(menuView, params);
    if (Build.VERSION.SDK_INT < 21) {
      addCompatibilityTopDivider(context);
    }

    this.menu.setCallback(
        new MenuBuilder.Callback() {
          @Override
          public boolean onMenuItemSelected(MenuBuilder menu, @NonNull MenuItem item) {
            if (reselectedListener != null && item.getItemId() == getSelectedItemId()) {
              reselectedListener.onNavigationItemReselected(item);
              return true; 
            }
            return selectedListener != null && !selectedListener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });

    applyWindowInsets();
  }

  private void applyWindowInsets() {
    ViewUtils.doOnApplyWindowInsets(
        this,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public androidx.core.view.WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull androidx.core.view.WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            initialPadding.bottom += insets.getSystemWindowInsetBottom();
            initialPadding.applyToView(view);
            return insets;
          }
        });
  }

  @NonNull
  private MaterialShapeDrawable createMaterialShapeDrawableBackground(Context context) {
    MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    Drawable originalBackground = getBackground();
    if (originalBackground instanceof ColorDrawable) {
      materialShapeDrawable.setFillColor(
          ColorStateList.valueOf(((ColorDrawable) originalBackground).getColor()));
    }
    materialShapeDrawable.initializeElevationOverlay(context);
    return materialShapeDrawable;
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

  
  public void setOnNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    selectedListener = listener;
  }

  
  public void setOnNavigationItemReselectedListener(
      @Nullable OnNavigationItemReselectedListener listener) {
    reselectedListener = listener;
  }

  
  @NonNull
  public Menu getMenu() {
    return menu;
  }

  
  public void inflateMenu(int resId) {
    presenter.setUpdateSuspended(true);
    getMenuInflater().inflate(resId, menu);
    presenter.setUpdateSuspended(false);
    presenter.updateMenuView(true);
  }

  
  public int getMaxItemCount() {
    return BottomNavigationMenu.MAX_ITEM_COUNT;
  }

  
  @Nullable
  public ColorStateList getItemIconTintList() {
    return menuView.getIconTintList();
  }

  
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    menuView.setIconTintList(tint);
  }

  
  public void setItemIconSize(@Dimension int iconSize) {
    menuView.setItemIconSize(iconSize);
  }

  
  public void setItemIconSizeRes(@DimenRes int iconSizeRes) {
    setItemIconSize(getResources().getDimensionPixelSize(iconSizeRes));
  }

  
  @Dimension
  public int getItemIconSize() {
    return menuView.getItemIconSize();
  }

  
  @Nullable
  public ColorStateList getItemTextColor() {
    return menuView.getItemTextColor();
  }

  
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    menuView.setItemTextColor(textColor);
  }

  
  @Deprecated
  @DrawableRes
  public int getItemBackgroundResource() {
    return menuView.getItemBackgroundRes();
  }

  
  public void setItemBackgroundResource(@DrawableRes int resId) {
    menuView.setItemBackgroundRes(resId);
    itemRippleColor = null;
  }

  
  @Nullable
  public Drawable getItemBackground() {
    return menuView.getItemBackground();
  }

  
  public void setItemBackground(@Nullable Drawable background) {
    menuView.setItemBackground(background);
    itemRippleColor = null;
  }

  
  @Nullable
  public ColorStateList getItemRippleColor() {
    return itemRippleColor;
  }

  
  public void setItemRippleColor(@Nullable ColorStateList itemRippleColor) {
    if (this.itemRippleColor == itemRippleColor) {
      
      if (itemRippleColor == null && menuView.getItemBackground() != null) {
        menuView.setItemBackground(null);
      }
      return;
    }

    this.itemRippleColor = itemRippleColor;
    if (itemRippleColor == null) {
      menuView.setItemBackground(null);
    } else {
      ColorStateList rippleDrawableColor =
          RippleUtils.convertToRippleDrawableColor(itemRippleColor);
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        menuView.setItemBackground(new RippleDrawable(rippleDrawableColor, null, null));
      } else {
        GradientDrawable rippleDrawable = new GradientDrawable();
        
        
        
        rippleDrawable.setCornerRadius(0.00001F);
        Drawable rippleDrawableCompat = DrawableCompat.wrap(rippleDrawable);
        DrawableCompat.setTintList(rippleDrawableCompat, rippleDrawableColor);
        menuView.setItemBackground(rippleDrawableCompat);
      }
    }
  }

  
  @IdRes
  public int getSelectedItemId() {
    return menuView.getSelectedItemId();
  }

  
  public void setSelectedItemId(@IdRes int itemId) {
    MenuItem item = menu.findItem(itemId);
    if (item != null) {
      if (!menu.performItemAction(item, presenter, 0)) {
        item.setChecked(true);
      }
    }
  }

  
  public void setLabelVisibilityMode(@LabelVisibilityMode int labelVisibilityMode) {
    if (menuView.getLabelVisibilityMode() != labelVisibilityMode) {
      menuView.setLabelVisibilityMode(labelVisibilityMode);
      presenter.updateMenuView(false);
    }
  }

  
  @LabelVisibilityMode
  public int getLabelVisibilityMode() {
    return menuView.getLabelVisibilityMode();
  }

  
  public void setItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    menuView.setItemTextAppearanceInactive(textAppearanceRes);
  }

  
  @StyleRes
  public int getItemTextAppearanceInactive() {
    return menuView.getItemTextAppearanceInactive();
  }

  
  public void setItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    menuView.setItemTextAppearanceActive(textAppearanceRes);
  }

  
  @StyleRes
  public int getItemTextAppearanceActive() {
    return menuView.getItemTextAppearanceActive();
  }

  
  public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
    if (menuView.isItemHorizontalTranslationEnabled() != itemHorizontalTranslationEnabled) {
      menuView.setItemHorizontalTranslationEnabled(itemHorizontalTranslationEnabled);
      presenter.updateMenuView(false);
    }
  }

  
  public boolean isItemHorizontalTranslationEnabled() {
    return menuView.isItemHorizontalTranslationEnabled();
  }

  
  @Nullable
  public BadgeDrawable getBadge(int menuItemId) {
    return menuView.getBadge(menuItemId);
  }

  
  public BadgeDrawable getOrCreateBadge(int menuItemId) {
    return menuView.getOrCreateBadge(menuItemId);
  }

  
  public void removeBadge(int menuItemId) {
    menuView.removeBadge(menuItemId);
  }

  
  public interface OnNavigationItemSelectedListener {

    
    boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  
  public interface OnNavigationItemReselectedListener {

    
    void onNavigationItemReselected(@NonNull MenuItem item);
  }

  private void addCompatibilityTopDivider(Context context) {
    View divider = new View(context);
    divider.setBackgroundColor(
        ContextCompat.getColor(context, R.color.design_bottom_navigation_shadow_color));
    FrameLayout.LayoutParams dividerParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_shadow_height));
    divider.setLayoutParams(dividerParams);
    addView(divider);
  }

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.menuPresenterState = new Bundle();
    menu.savePresenterStates(savedState.menuPresenterState);
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    menu.restorePresenterStates(savedState.menuPresenterState);
  }

  static class SavedState extends AbsSavedState {
    @Nullable Bundle menuPresenterState;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      if (loader == null) {
        loader = getClass().getClassLoader();
      }
      readFromParcel(source, loader);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeBundle(menuPresenterState);
    }

    private void readFromParcel(@NonNull Parcel in, ClassLoader loader) {
      menuPresenterState = in.readBundle(loader);
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
