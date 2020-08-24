

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.SubMenuBuilder;


@RestrictTo(LIBRARY_GROUP)
public class NavigationSubMenu extends SubMenuBuilder {

  public NavigationSubMenu(Context context, NavigationMenu menu, MenuItemImpl item) {
    super(context, menu, item);
  }

  @Override
  public void onItemsChanged(boolean structureChanged) {
    super.onItemsChanged(structureChanged);
    ((MenuBuilder) getParentMenu()).onItemsChanged(structureChanged);
  }
}
