

package com.zeoflow.material.elements.bottomnavigation;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import android.view.MenuItem;
import android.view.SubMenu;


@RestrictTo(LIBRARY_GROUP)
public final class BottomNavigationMenu extends MenuBuilder {
  public static final int MAX_ITEM_COUNT = 5;

  public BottomNavigationMenu(Context context) {
    super(context);
  }

  @NonNull
  @Override
  public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
    throw new UnsupportedOperationException("BottomNavigationView does not support submenus");
  }

  @Override
  protected MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
    if (size() + 1 > MAX_ITEM_COUNT) {
      throw new IllegalArgumentException(
          "Maximum number of items supported by BottomNavigationView is "
              + MAX_ITEM_COUNT
              + ". Limit can be checked with BottomNavigationView#getMaxItemCount()");
    }
    stopDispatchingItemsChanged();
    final MenuItem item = super.addInternal(group, id, categoryOrder, title);
    if (item instanceof MenuItemImpl) {
      ((MenuItemImpl) item).setExclusiveCheckable(true);
    }
    startDispatchingItemsChanged();
    return item;
  }
}
