

package com.zeoflow.material.elements.bottomnavigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.SubMenuBuilder;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.zeoflow.material.elements.badge.BadgeDrawable;
import com.zeoflow.material.elements.badge.BadgeUtils;
import com.zeoflow.material.elements.internal.ParcelableSparseArray;


@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationPresenter implements MenuPresenter {
  private MenuBuilder menu;
  private BottomNavigationMenuView menuView;
  private boolean updateSuspended = false;
  private int id;

  public void setBottomNavigationMenuView(BottomNavigationMenuView menuView) {
    this.menuView = menuView;
  }

  @Override
  public void initForMenu(Context context, MenuBuilder menu) {
    this.menu = menu;
    menuView.initialize(this.menu);
  }

  @Override
  public MenuView getMenuView(ViewGroup root) {
    return menuView;
  }

  @Override
  public void updateMenuView(boolean cleared) {
    if (updateSuspended) {
      return;
    }
    if (cleared) {
      menuView.buildMenuView();
    } else {
      menuView.updateMenuView();
    }
  }

  @Override
  public void setCallback(Callback cb) {}

  @Override
  public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
    return false;
  }

  @Override
  public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {}

  @Override
  public boolean flagActionItems() {
    return false;
  }

  @Override
  public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  @Override
  public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return id;
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState();
    savedState.selectedItemId = menuView.getSelectedItemId();
    savedState.badgeSavedStates =
        BadgeUtils.createParcelableBadgeStates(menuView.getBadgeDrawables());
    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      menuView.tryRestoreSelectedItemId(((SavedState) state).selectedItemId);
      SparseArray<BadgeDrawable> badgeDrawables =
          BadgeUtils.createBadgeDrawablesFromSavedStates(
              menuView.getContext(), ((SavedState) state).badgeSavedStates);
      menuView.setBadgeDrawables(badgeDrawables);
    }
  }

  public void setUpdateSuspended(boolean updateSuspended) {
    this.updateSuspended = updateSuspended;
  }

  static class SavedState implements Parcelable {
    int selectedItemId;
    @Nullable ParcelableSparseArray badgeSavedStates;

    SavedState() {}

    SavedState(@NonNull Parcel in) {
      selectedItemId = in.readInt();
      badgeSavedStates = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      out.writeInt(selectedItemId);
      out.writeParcelable(badgeSavedStates,  0);
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
