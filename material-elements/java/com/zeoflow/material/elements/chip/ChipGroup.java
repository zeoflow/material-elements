

package com.zeoflow.material.elements.chip;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.BoolRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import com.zeoflow.material.elements.internal.FlowLayout;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.util.ArrayList;
import java.util.List;


public class ChipGroup extends FlowLayout
{

  
  public interface OnCheckedChangeListener {
    
    public void onCheckedChanged(ChipGroup group, @IdRes int checkedId);
  }

  
  public static class LayoutParams extends MarginLayoutParams {
    public LayoutParams(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }
  }

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_ChipGroup;

  @Dimension private int chipSpacingHorizontal;
  @Dimension private int chipSpacingVertical;
  private boolean singleSelection;
  private boolean selectionRequired;

  @Nullable private OnCheckedChangeListener onCheckedChangeListener;

  private final CheckedStateTracker checkedStateTracker = new CheckedStateTracker();

  @NonNull
  private PassThroughHierarchyChangeListener passThroughListener =
      new PassThroughHierarchyChangeListener();

  @IdRes private int checkedId = View.NO_ID;
  private boolean protectFromCheckedChange = false;

  public ChipGroup(Context context) {
    this(context, null);
  }

  public ChipGroup(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.chipGroupStyle);
  }

  public ChipGroup(Context context, AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.ChipGroup,
            defStyleAttr,
            DEF_STYLE_RES);

    int chipSpacing = a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacing, 0);
    setChipSpacingHorizontal(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingHorizontal, chipSpacing));
    setChipSpacingVertical(
        a.getDimensionPixelOffset(R.styleable.ChipGroup_chipSpacingVertical, chipSpacing));
    setSingleLine(a.getBoolean(R.styleable.ChipGroup_singleLine, false));
    setSingleSelection(a.getBoolean(R.styleable.ChipGroup_singleSelection, false));
    setSelectionRequired(a.getBoolean(R.styleable.ChipGroup_selectionRequired, false));
    int checkedChip = a.getResourceId(R.styleable.ChipGroup_checkedChip, View.NO_ID);
    if (checkedChip != View.NO_ID) {
      checkedId = checkedChip;
    }

    a.recycle();
    super.setOnHierarchyChangeListener(passThroughListener);

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    
    int columnCount = isSingleLine() ? getChipCount() : -1;
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
             getRowCount(),
             columnCount,
             false,
             isSingleSelection()
                ? CollectionInfoCompat.SELECTION_MODE_SINGLE
                : CollectionInfoCompat.SELECTION_MODE_MULTIPLE));
  }

  @NonNull
  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new ChipGroup.LayoutParams(getContext(), attrs);
  }

  @NonNull
  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    return new ChipGroup.LayoutParams(lp);
  }

  @NonNull
  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new ChipGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p) && (p instanceof ChipGroup.LayoutParams);
  }

  @Override
  public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
    
    passThroughListener.onHierarchyChangeListener = listener;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    
    if (checkedId != View.NO_ID) {
      setCheckedStateForView(checkedId, true);
      setCheckedId(checkedId);
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (child instanceof Chip) {
      final Chip chip = (Chip) child;
      if (chip.isChecked()) {
        if (checkedId != View.NO_ID && singleSelection) {
          setCheckedStateForView(checkedId, false);
        }
        setCheckedId(chip.getId());
      }
    }

    super.addView(child, index, params);
  }

  
  @Deprecated
  public void setDividerDrawableHorizontal(Drawable divider) {
    throw new UnsupportedOperationException(
        "Changing divider drawables have no effect. ChipGroup do not use divider drawables as "
            + "spacing.");
  }

  
  @Deprecated
  public void setDividerDrawableVertical(@Nullable Drawable divider) {
    throw new UnsupportedOperationException(
        "Changing divider drawables have no effect. ChipGroup do not use divider drawables as "
            + "spacing.");
  }

  
  @Deprecated
  public void setShowDividerHorizontal(int dividerMode) {
    throw new UnsupportedOperationException(
        "Changing divider modes has no effect. ChipGroup do not use divider drawables as spacing.");
  }

  
  @Deprecated
  public void setShowDividerVertical(int dividerMode) {
    throw new UnsupportedOperationException(
        "Changing divider modes has no effect. ChipGroup do not use divider drawables as spacing.");
  }

  
  @Deprecated
  public void setFlexWrap(int flexWrap) {
    throw new UnsupportedOperationException(
        "Changing flex wrap not allowed. ChipGroup exposes a singleLine attribute instead.");
  }

  
  public void check(@IdRes int id) {
    if (id == checkedId) {
      return;
    }

    if (checkedId != View.NO_ID && singleSelection) {
      setCheckedStateForView(checkedId, false);
    }

    if (id != View.NO_ID) {
      setCheckedStateForView(id, true);
    }

    setCheckedId(id);
  }
  
  @IdRes
  public int getCheckedChipId() {
    return singleSelection ? checkedId : View.NO_ID;
  }

  
  @NonNull
  public List<Integer> getCheckedChipIds() {
    ArrayList<Integer> checkedIds = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof Chip) {
        if (((Chip) child).isChecked()) {
          checkedIds.add(child.getId());
          if (singleSelection) {
            return checkedIds;
          }
        }
      }
    }

    return checkedIds;
  }

  
  public void clearCheck() {
    protectFromCheckedChange = true;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof Chip) {
        ((Chip) child).setChecked(false);
      }
    }
    protectFromCheckedChange = false;

    setCheckedId(View.NO_ID);
  }

  
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    onCheckedChangeListener = listener;
  }

  private void setCheckedId(int checkedId) {
    setCheckedId(checkedId, true);
  }

  private void setCheckedId(int checkedId, boolean fromUser) {
    this.checkedId = checkedId;

    if (onCheckedChangeListener != null && singleSelection && fromUser) {
      onCheckedChangeListener.onCheckedChanged(this, checkedId);
    }
  }

  private void setCheckedStateForView(@IdRes int viewId, boolean checked) {
    View checkedView = findViewById(viewId);
    if (checkedView instanceof Chip) {
      protectFromCheckedChange = true;
      ((Chip) checkedView).setChecked(checked);
      protectFromCheckedChange = false;
    }
  }

  private int getChipCount() {
    int count = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) instanceof Chip) {
        count++;
      }
    }
    return count;
  }

  
  int getIndexOfChip(@Nullable View child) {
    if (!(child instanceof Chip)) {
      return -1;
    }
    int index = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) instanceof Chip) {
        Chip chip = (Chip) getChildAt(i);
        if (chip == child) {
          return index;
        }
        index++;
      }
    }
    return -1;
  }

  
  public void setChipSpacing(@Dimension int chipSpacing) {
    setChipSpacingHorizontal(chipSpacing);
    setChipSpacingVertical(chipSpacing);
  }

  
  public void setChipSpacingResource(@DimenRes int id) {
    setChipSpacing(getResources().getDimensionPixelOffset(id));
  }

  
  @Dimension
  public int getChipSpacingHorizontal() {
    return chipSpacingHorizontal;
  }

  
  public void setChipSpacingHorizontal(@Dimension int chipSpacingHorizontal) {
    if (this.chipSpacingHorizontal != chipSpacingHorizontal) {
      this.chipSpacingHorizontal = chipSpacingHorizontal;
      setItemSpacing(chipSpacingHorizontal);
      requestLayout();
    }
  }

  
  public void setChipSpacingHorizontalResource(@DimenRes int id) {
    setChipSpacingHorizontal(getResources().getDimensionPixelOffset(id));
  }

  
  @Dimension
  public int getChipSpacingVertical() {
    return chipSpacingVertical;
  }

  
  public void setChipSpacingVertical(@Dimension int chipSpacingVertical) {
    if (this.chipSpacingVertical != chipSpacingVertical) {
      this.chipSpacingVertical = chipSpacingVertical;
      setLineSpacing(chipSpacingVertical);
      requestLayout();
    }
  }

  
  public void setChipSpacingVerticalResource(@DimenRes int id) {
    setChipSpacingVertical(getResources().getDimensionPixelOffset(id));
  }

  
  @SuppressWarnings("RedundantOverride")
  @Override
  public boolean isSingleLine() {
    return super.isSingleLine();
  }

  
  @SuppressWarnings("RedundantOverride")
  @Override
  public void setSingleLine(boolean singleLine) {
    super.setSingleLine(singleLine);
  }

  
  public void setSingleLine(@BoolRes int id) {
    setSingleLine(getResources().getBoolean(id));
  }

  
  public boolean isSingleSelection() {
    return singleSelection;
  }

  
  public void setSingleSelection(boolean singleSelection) {
    if (this.singleSelection != singleSelection) {
      this.singleSelection = singleSelection;

      clearCheck();
    }
  }

  
  public void setSingleSelection(@BoolRes int id) {
    setSingleSelection(getResources().getBoolean(id));
  }

  
  public void setSelectionRequired(boolean selectionRequired) {
    this.selectionRequired = selectionRequired;
  }

  
  public boolean isSelectionRequired() {
    return selectionRequired;
  }

  private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
      
      if (protectFromCheckedChange) {
        return;
      }

      List<Integer> checkedChipIds = getCheckedChipIds();
      if (checkedChipIds.isEmpty() && selectionRequired) {
        setCheckedStateForView(buttonView.getId(), true);
        setCheckedId(buttonView.getId(), false);
        return;
      }

      int id = buttonView.getId();

      if (isChecked) {
        if (checkedId != View.NO_ID && checkedId != id && singleSelection) {
          setCheckedStateForView(checkedId, false);
        }
        setCheckedId(id);
      } else if (checkedId == id) {
        setCheckedId(View.NO_ID);
      }
    }
  }

  
  private class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {
    private OnHierarchyChangeListener onHierarchyChangeListener;

    @Override
    public void onChildViewAdded(View parent, View child) {
      if (parent == ChipGroup.this && child instanceof Chip) {
        int id = child.getId();
        
        if (id == View.NO_ID) {
          if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            id = View.generateViewId();
          } else {
            id = child.hashCode();
          }
          child.setId(id);
        }
        ((Chip) child).setOnCheckedChangeListenerInternal(checkedStateTracker);
      }

      if (onHierarchyChangeListener != null) {
        onHierarchyChangeListener.onChildViewAdded(parent, child);
      }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
      if (parent == ChipGroup.this && child instanceof Chip) {
        ((Chip) child).setOnCheckedChangeListenerInternal(null);
      }

      if (onHierarchyChangeListener != null) {
        onHierarchyChangeListener.onChildViewRemoved(parent, child);
      }
    }
  }
}
