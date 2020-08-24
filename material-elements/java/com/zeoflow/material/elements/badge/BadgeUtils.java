

package com.zeoflow.material.elements.badge;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import com.zeoflow.material.elements.badge.BadgeDrawable.SavedState;
import com.zeoflow.material.elements.internal.ParcelableSparseArray;


@RestrictTo(Scope.LIBRARY)
public class BadgeUtils {

  public static final boolean USE_COMPAT_PARENT = VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2;

  private BadgeUtils() {
    
  }

  
  public static void updateBadgeBounds(
      @NonNull Rect rect, float centerX, float centerY, float halfWidth, float halfHeight) {
    rect.set(
        (int) (centerX - halfWidth),
        (int) (centerY - halfHeight),
        (int) (centerX + halfWidth),
        (int) (centerY + halfHeight));
  }

  
  public static void attachBadgeDrawable(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    setBadgeDrawableBounds(badgeDrawable, anchor, compatBadgeParent);
    if (USE_COMPAT_PARENT) {
      compatBadgeParent.setForeground(badgeDrawable);
    } else {
      anchor.getOverlay().add(badgeDrawable);
    }
  }

  
  public static void detachBadgeDrawable(
      @Nullable BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    if (badgeDrawable == null) {
      return;
    }
    if (USE_COMPAT_PARENT) {
      compatBadgeParent.setForeground(null);
    } else {
      anchor.getOverlay().remove(badgeDrawable);
    }
  }

  
  public static void setBadgeDrawableBounds(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    Rect badgeBounds = new Rect();
    View badgeParent = USE_COMPAT_PARENT ? compatBadgeParent : anchor;
    badgeParent.getDrawingRect(badgeBounds);
    badgeDrawable.setBounds(badgeBounds);
    badgeDrawable.updateBadgeCoordinates(anchor, compatBadgeParent);
  }

  
  @NonNull
  public static ParcelableSparseArray createParcelableBadgeStates(
      @NonNull SparseArray<BadgeDrawable> badgeDrawables) {
    ParcelableSparseArray badgeStates = new ParcelableSparseArray();
    for (int i = 0; i < badgeDrawables.size(); i++) {
      int key = badgeDrawables.keyAt(i);
      BadgeDrawable badgeDrawable = badgeDrawables.valueAt(i);
      if (badgeDrawable == null) {
        throw new IllegalArgumentException("badgeDrawable cannot be null");
      }
      badgeStates.put(key, badgeDrawable.getSavedState());
    }
    return badgeStates;
  }

  
  @NonNull
  public static SparseArray<BadgeDrawable> createBadgeDrawablesFromSavedStates(
      Context context, @NonNull ParcelableSparseArray badgeStates) {
    SparseArray<BadgeDrawable> badgeDrawables = new SparseArray<>(badgeStates.size());
    for (int i = 0; i < badgeStates.size(); i++) {
      int key = badgeStates.keyAt(i);
      BadgeDrawable.SavedState savedState = (SavedState) badgeStates.valueAt(i);
      if (savedState == null) {
        throw new IllegalArgumentException("BadgeDrawable's savedState cannot be null");
      }
      BadgeDrawable badgeDrawable = BadgeDrawable.createFromSavedState(context, savedState);
      badgeDrawables.put(key, badgeDrawable);
    }
    return badgeDrawables;
  }
}
