

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.SubMenuBuilder;
import android.view.SubMenu;


@RestrictTo(LIBRARY_GROUP)
public class NavigationMenu extends MenuBuilder {

  public NavigationMenu(Context context) {
    super(context);
  }

  @NonNull
  @Override
  public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
    final MenuItemImpl item = (MenuItemImpl) addInternal(group, id, categoryOrder, title);
    final SubMenuBuilder subMenu = new NavigationSubMenu(getContext(), this, item);
    item.setSubMenu(subMenu);
    return subMenu;
  }
}
