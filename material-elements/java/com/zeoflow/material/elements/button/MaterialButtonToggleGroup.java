

package com.zeoflow.material.elements.button;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import androidx.annotation.BoolRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import com.zeoflow.material.elements.button.MaterialButton.OnPressedChangeListener;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.internal.ViewUtils;
import com.zeoflow.material.elements.shape.AbsoluteCornerSize;
import com.zeoflow.material.elements.shape.CornerSize;
import com.zeoflow.material.elements.shape.ShapeAppearanceModel;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class MaterialButtonToggleGroup extends LinearLayout {

  
  public interface OnButtonCheckedListener {
    
    void onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId, boolean isChecked);
  }

  private static final String LOG_TAG = MaterialButtonToggleGroup.class.getSimpleName();
  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_MaterialButtonToggleGroup;

  private final List<CornerData> originalCornerData = new ArrayList<>();

  private final CheckedStateTracker checkedStateTracker = new CheckedStateTracker();
  private final PressedStateTracker pressedStateTracker = new PressedStateTracker();
  private final LinkedHashSet<OnButtonCheckedListener> onButtonCheckedListeners =
      new LinkedHashSet<>();
  private final Comparator<MaterialButton> childOrderComparator =
      new Comparator<MaterialButton>() {
        @Override
        public int compare(MaterialButton v1, MaterialButton v2) {
          int checked = Boolean.valueOf(v1.isChecked()).compareTo(v2.isChecked());
          if (checked != 0) {
            return checked;
          }

          int stateful = Boolean.valueOf(v1.isPressed()).compareTo(v2.isPressed());
          if (stateful != 0) {
            return stateful;
          }

          
          return Integer.valueOf(indexOfChild(v1)).compareTo(indexOfChild(v2));
        }
      };

  private Integer[] childOrder;
  private boolean skipCheckedStateTracker = false;
  private boolean singleSelection;
  private boolean selectionRequired;

  @IdRes private int checkedId;

  public MaterialButtonToggleGroup(@NonNull Context context) {
    this(context, null);
  }

  public MaterialButtonToggleGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonToggleGroupStyle);
  }

  public MaterialButtonToggleGroup(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    
    context = getContext();
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialButtonToggleGroup, defStyleAttr, DEF_STYLE_RES);

    setSingleSelection(
        attributes.getBoolean(R.styleable.MaterialButtonToggleGroup_singleSelection, false));
    checkedId =
        attributes.getResourceId(R.styleable.MaterialButtonToggleGroup_checkedButton, View.NO_ID);
    selectionRequired =
        attributes.getBoolean(R.styleable.MaterialButtonToggleGroup_selectionRequired, false);
    setChildrenDrawingOrderEnabled(true);
    attributes.recycle();

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    
    if (checkedId != View.NO_ID) {
      checkForced(checkedId);
    }
  }

  @Override
  protected void dispatchDraw(@NonNull Canvas canvas) {
    updateChildOrder();
    super.dispatchDraw(canvas);
  }

  
  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (!(child instanceof MaterialButton)) {
      Log.e(LOG_TAG, "Child views must be of type MaterialButton.");
      return;
    }

    super.addView(child, index, params);
    MaterialButton buttonChild = (MaterialButton) child;
    setGeneratedIdIfNeeded(buttonChild);
    
    setupButtonChild(buttonChild);

    
    if (buttonChild.isChecked()) {
      updateCheckedStates(buttonChild.getId(), true);
      setCheckedId(buttonChild.getId());
    }

    
    ShapeAppearanceModel shapeAppearanceModel = buttonChild.getShapeAppearanceModel();
    originalCornerData.add(
        new CornerData(
            shapeAppearanceModel.getTopLeftCornerSize(),
            shapeAppearanceModel.getBottomLeftCornerSize(),
            shapeAppearanceModel.getTopRightCornerSize(),
            shapeAppearanceModel.getBottomRightCornerSize()));

    ViewCompat.setAccessibilityDelegate(
        buttonChild,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setCollectionItemInfo(
                CollectionItemInfoCompat.obtain(
                     0,
                     1,
                     getIndexWithinVisibleButtons(host),
                     1,
                     false,
                     ((MaterialButton) host).isChecked()));
          }
        });
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);

    if (child instanceof MaterialButton) {
      ((MaterialButton) child).removeOnCheckedChangeListener(checkedStateTracker);
      ((MaterialButton) child).setOnPressedChangeListenerInternal(null);
    }

    int indexOfChild = indexOfChild(child);
    if (indexOfChild >= 0) {
      originalCornerData.remove(indexOfChild);
    }

    updateChildShapes();
    adjustChildMarginsAndUpdateLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updateChildShapes();
    adjustChildMarginsAndUpdateLayout();

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @NonNull
  @Override
  public CharSequence getAccessibilityClassName() {
    return MaterialButtonToggleGroup.class.getName();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
             1,
             getVisibleButtonCount(),
             false,
             isSingleSelection()
                ? CollectionInfoCompat.SELECTION_MODE_SINGLE
                : CollectionInfoCompat.SELECTION_MODE_MULTIPLE));
  }

  
  public void check(@IdRes int id) {
    if (id == checkedId) {
      return;
    }

    checkForced(id);
  }

  
  public void uncheck(@IdRes int id) {
    setCheckedStateForView(id, false);
    updateCheckedStates(id, false);
    checkedId = View.NO_ID;
    dispatchOnButtonChecked(id, false);
  }

  
  public void clearChecked() {
    skipCheckedStateTracker = true;
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton child = getChildButton(i);
      child.setChecked(false);

      dispatchOnButtonChecked(child.getId(), false);
    }
    skipCheckedStateTracker = false;

    setCheckedId(View.NO_ID);
  }

  
  @IdRes
  public int getCheckedButtonId() {
    return singleSelection ? checkedId : View.NO_ID;
  }

  
  @NonNull
  public List<Integer> getCheckedButtonIds() {
    List<Integer> checkedIds = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      MaterialButton child = getChildButton(i);
      if (child.isChecked()) {
        checkedIds.add(child.getId());
      }
    }

    return checkedIds;
  }

  
  public void addOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
    onButtonCheckedListeners.add(listener);
  }

  
  public void removeOnButtonCheckedListener(@NonNull OnButtonCheckedListener listener) {
    onButtonCheckedListeners.remove(listener);
  }

  
  public void clearOnButtonCheckedListeners() {
    onButtonCheckedListeners.clear();
  }

  
  public boolean isSingleSelection() {
    return singleSelection;
  }

  
  public void setSingleSelection(boolean singleSelection) {
    if (this.singleSelection != singleSelection) {
      this.singleSelection = singleSelection;
      clearChecked();
    }
  }

  
  public void setSelectionRequired(boolean selectionRequired) {
    this.selectionRequired = selectionRequired;
  }

  
  public boolean isSelectionRequired() {
    return selectionRequired;
  }

  
  public void setSingleSelection(@BoolRes int id) {
    setSingleSelection(getResources().getBoolean(id));
  }

  private void setCheckedStateForView(@IdRes int viewId, boolean checked) {
    View checkedView = findViewById(viewId);
    if (checkedView instanceof MaterialButton) {
      skipCheckedStateTracker = true;
      ((MaterialButton) checkedView).setChecked(checked);
      skipCheckedStateTracker = false;
    }
  }

  private void setCheckedId(int checkedId) {
    this.checkedId = checkedId;

    dispatchOnButtonChecked(checkedId, true);
  }

  
  private void adjustChildMarginsAndUpdateLayout() {
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    if (firstVisibleChildIndex == -1) {
      return;
    }

    for (int i = firstVisibleChildIndex + 1; i < getChildCount(); i++) {
      
      MaterialButton currentButton = getChildButton(i);
      MaterialButton previousButton = getChildButton(i - 1);

      
      int smallestStrokeWidth =
          Math.min(currentButton.getStrokeWidth(), previousButton.getStrokeWidth());

      LayoutParams params = buildLayoutParams(currentButton);
      if (getOrientation() == HORIZONTAL) {
        MarginLayoutParamsCompat.setMarginEnd(params, 0);
        MarginLayoutParamsCompat.setMarginStart(params, -smallestStrokeWidth);
      } else {
        params.bottomMargin = 0;
        params.topMargin = -smallestStrokeWidth;
      }

      currentButton.setLayoutParams(params);
    }

    resetChildMargins(firstVisibleChildIndex);
  }

  private MaterialButton getChildButton(int index) {
    return (MaterialButton) getChildAt(index);
  }

  private void resetChildMargins(int childIndex) {
    if (getChildCount() == 0 || childIndex == -1) {
      return;
    }

    MaterialButton currentButton = getChildButton(childIndex);
    LayoutParams params = (LayoutParams) currentButton.getLayoutParams();
    if (getOrientation() == VERTICAL) {
      params.topMargin = 0;
      params.bottomMargin = 0;
      return;
    }

    MarginLayoutParamsCompat.setMarginEnd(params, 0);
    MarginLayoutParamsCompat.setMarginStart(params, 0);
    params.leftMargin = 0;
    params.rightMargin = 0;
  }

  
  @VisibleForTesting
  void updateChildShapes() {
    int childCount = getChildCount();
    int firstVisibleChildIndex = getFirstVisibleChildIndex();
    int lastVisibleChildIndex = getLastVisibleChildIndex();
    for (int i = 0; i < childCount; i++) {
      MaterialButton button = getChildButton(i);
      if (button.getVisibility() == GONE) {
        continue;
      }

      ShapeAppearanceModel.Builder builder = button.getShapeAppearanceModel().toBuilder();
      CornerData newCornerData = getNewCornerData(i, firstVisibleChildIndex, lastVisibleChildIndex);
      updateBuilderWithCornerData(builder, newCornerData);

      button.setShapeAppearanceModel(builder.build());
    }
  }

  private int getFirstVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      if (isChildVisible(i)) {
        return i;
      }
    }

    return -1;
  }

  private int getLastVisibleChildIndex() {
    int childCount = getChildCount();
    for (int i = childCount - 1; i >= 0; i--) {
      if (isChildVisible(i)) {
        return i;
      }
    }

    return -1;
  }

  private boolean isChildVisible(int i) {
    View child = getChildAt(i);
    return child.getVisibility() != View.GONE;
  }

  private int getVisibleButtonCount() {
    int count = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) instanceof MaterialButton && isChildVisible(i)) {
        count++;
      }
    }
    return count;
  }

  private int getIndexWithinVisibleButtons(@Nullable View child) {
    if (!(child instanceof MaterialButton)) {
      return -1;
    }
    int index = 0;
    for (int i = 0; i < getChildCount(); i++) {
      if (this.getChildAt(i) == child) {
        return index;
      }
      if (this.getChildAt(i) instanceof MaterialButton && isChildVisible(i)) {
        index++;
      }
    }
    return -1;
  }

  @Nullable
  private CornerData getNewCornerData(
      int index, int firstVisibleChildIndex, int lastVisibleChildIndex) {
    CornerData cornerData = originalCornerData.get(index);

    
    if (firstVisibleChildIndex == lastVisibleChildIndex) {
      return cornerData;
    }

    boolean isHorizontal = getOrientation() == HORIZONTAL;
    if (index == firstVisibleChildIndex) {
      return isHorizontal ? CornerData.start(cornerData, this) : CornerData.top(cornerData);
    }

    if (index == lastVisibleChildIndex) {
      return isHorizontal ? CornerData.end(cornerData, this) : CornerData.bottom(cornerData);
    }

    return null;
  }

  private static void updateBuilderWithCornerData(
      ShapeAppearanceModel.Builder shapeAppearanceModelBuilder, @Nullable CornerData cornerData) {
    if (cornerData == null) {
      shapeAppearanceModelBuilder.setAllCornerSizes(0);
      return;
    }

    shapeAppearanceModelBuilder
        .setTopLeftCornerSize(cornerData.topLeft)
        .setBottomLeftCornerSize(cornerData.bottomLeft)
        .setTopRightCornerSize(cornerData.topRight)
        .setBottomRightCornerSize(cornerData.bottomRight);
  }

  
  private boolean updateCheckedStates(int childId, boolean childIsChecked) {
    List<Integer> checkedButtonIds = getCheckedButtonIds();
    if (selectionRequired && checkedButtonIds.isEmpty()) {
      
      setCheckedStateForView(childId, true);
      checkedId = childId;
      return false;
    }

    
    if (childIsChecked && singleSelection) {
      checkedButtonIds.remove((Integer) childId);
      for (int buttonId : checkedButtonIds) {
        setCheckedStateForView(buttonId, false);
        dispatchOnButtonChecked(buttonId, false);
      }
    }
    return true;
  }

  private void dispatchOnButtonChecked(@IdRes int buttonId, boolean checked) {
    for (OnButtonCheckedListener listener : onButtonCheckedListeners) {
      listener.onButtonChecked(this, buttonId, checked);
    }
  }

  private void checkForced(int checkedId) {
    setCheckedStateForView(checkedId, true);
    updateCheckedStates(checkedId, true);
    setCheckedId(checkedId);
  }

  private void setGeneratedIdIfNeeded(@NonNull MaterialButton materialButton) {
    
    if (materialButton.getId() == View.NO_ID) {
      materialButton.setId(ViewCompat.generateViewId());
    }
  }

  
  private void setupButtonChild(@NonNull MaterialButton buttonChild) {
    buttonChild.setMaxLines(1);
    buttonChild.setEllipsize(TruncateAt.END);
    buttonChild.setCheckable(true);

    buttonChild.addOnCheckedChangeListener(checkedStateTracker);
    buttonChild.setOnPressedChangeListenerInternal(pressedStateTracker);

    
    buttonChild.setShouldDrawSurfaceColorStroke(true);
  }

  @NonNull
  private LinearLayout.LayoutParams buildLayoutParams(@NonNull View child) {
    ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      return (LayoutParams) layoutParams;
    }

    return new LayoutParams(layoutParams.width, layoutParams.height);
  }

  
  @Override
  protected int getChildDrawingOrder(int childCount, int i) {
    if (childOrder == null || i >= childOrder.length) {
      Log.w(LOG_TAG, "Child order wasn't updated");
      return i;
    }

    return childOrder[i];
  }

  private void updateChildOrder() {
    final SortedMap<MaterialButton, Integer> viewToIndexMap = new TreeMap<>(childOrderComparator);
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      viewToIndexMap.put(getChildButton(i), i);
    }

    childOrder = viewToIndexMap.values().toArray(new Integer[0]);
  }

  private class CheckedStateTracker implements MaterialButton.OnCheckedChangeListener {
    @Override
    public void onCheckedChanged(@NonNull MaterialButton button, boolean isChecked) {
      
      if (skipCheckedStateTracker) {
        return;
      }

      if (singleSelection) {
        checkedId = isChecked ? button.getId() : View.NO_ID;
      }

      boolean buttonCheckedStateChanged = updateCheckedStates(button.getId(), isChecked);
      if (buttonCheckedStateChanged) {
        
        
        dispatchOnButtonChecked(button.getId(), button.isChecked());
      }
      invalidate();
    }
  }

  private class PressedStateTracker implements OnPressedChangeListener {

    @Override
    public void onPressedChanged(@NonNull MaterialButton button, boolean isPressed) {
      invalidate();
    }
  }

  private static class CornerData {

    private static final CornerSize noCorner = new AbsoluteCornerSize(0);

    CornerSize topLeft;
    CornerSize topRight;
    CornerSize bottomRight;
    CornerSize bottomLeft;

    CornerData(
        CornerSize topLeft, CornerSize bottomLeft, CornerSize topRight, CornerSize bottomRight) {
      this.topLeft = topLeft;
      this.topRight = topRight;
      this.bottomRight = bottomRight;
      this.bottomLeft = bottomLeft;
    }

    
    public static CornerData start(CornerData orig, View view) {
      return ViewUtils.isLayoutRtl(view) ? right(orig) : left(orig);
    }

    
    public static CornerData end(CornerData orig, View view) {
      return ViewUtils.isLayoutRtl(view) ? left(orig) : right(orig);
    }

    
    public static CornerData left(CornerData orig) {
      return new CornerData(orig.topLeft, orig.bottomLeft, noCorner, noCorner);
    }

    
    public static CornerData right(CornerData orig) {
      return new CornerData(noCorner, noCorner, orig.topRight, orig.bottomRight);
    }

    
    public static CornerData top(CornerData orig) {
      return new CornerData(orig.topLeft, noCorner, orig.topRight, noCorner);
    }

    
    public static CornerData bottom(CornerData orig) {
      return new CornerData(noCorner, orig.bottomLeft, noCorner, orig.bottomRight);
    }
  }
}
