

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.annotation.RestrictTo;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;


@RestrictTo(LIBRARY_GROUP)
public class NavigationMenuView extends RecyclerView implements MenuView {

  public NavigationMenuView(Context context) {
    this(context, null);
  }

  public NavigationMenuView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NavigationMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
  }

  @Override
  public void initialize(MenuBuilder menu) {}

  @Override
  public int getWindowAnimations() {
    return 0;
  }
}
