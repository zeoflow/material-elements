

package com.zeoflow.material.elements.textfield;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.ListPopupWindow;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filterable;
import android.widget.ListAdapter;
import com.zeoflow.material.elements.internal.ManufacturerUtils;
import com.zeoflow.material.elements.internal.ThemeEnforcement;
import com.zeoflow.material.elements.theme.overlay.MaterialThemeOverlay;


public class MaterialAutoCompleteTextView extends AppCompatAutoCompleteTextView {

  private static final int MAX_ITEMS_MEASURED = 15;

  @NonNull private final ListPopupWindow modalListPopup;
  @Nullable private final AccessibilityManager accessibilityManager;
  @NonNull private final Rect tempRect = new Rect();

  public MaterialAutoCompleteTextView(@NonNull Context context) {
    this(context, null);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet) {
    this(context, attributeSet, R.attr.autoCompleteTextViewStyle);
  }

  public MaterialAutoCompleteTextView(
      @NonNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attributeSet, defStyleAttr, 0), attributeSet, defStyleAttr);
    
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attributeSet,
            R.styleable.MaterialAutoCompleteTextView,
            defStyleAttr,
            R.style.Widget_AppCompat_AutoCompleteTextView);

    
    
    if (attributes.hasValue(R.styleable.MaterialAutoCompleteTextView_android_inputType)) {
      int inputType =
          attributes.getInt(
              R.styleable.MaterialAutoCompleteTextView_android_inputType, InputType.TYPE_NULL);
      if (inputType == InputType.TYPE_NULL) {
        setKeyListener(null);
      }
    }

    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    modalListPopup = new ListPopupWindow(context);
    modalListPopup.setModal(true);
    modalListPopup.setAnchorView(this);
    modalListPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
    modalListPopup.setAdapter(getAdapter());
    modalListPopup.setOnItemClickListener(
        new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View selectedView, int position, long id) {
            Object selectedItem =
                position < 0 ? modalListPopup.getSelectedItem() : getAdapter().getItem(position);

            updateText(selectedItem);

            OnItemClickListener userOnitemClickListener = getOnItemClickListener();
            if (userOnitemClickListener != null) {
              if (selectedView == null || position < 0) {
                selectedView = modalListPopup.getSelectedView();
                position = modalListPopup.getSelectedItemPosition();
                id = modalListPopup.getSelectedItemId();
              }
              userOnitemClickListener.onItemClick(
                  modalListPopup.getListView(), selectedView, position, id);
            }

            modalListPopup.dismiss();
          }
        });

    attributes.recycle();
  }

  @Override
  public void showDropDown() {
    if (getInputType() == EditorInfo.TYPE_NULL
        && accessibilityManager != null
        && accessibilityManager.isTouchExplorationEnabled()) {
      modalListPopup.show();
    } else {
      super.showDropDown();
    }
  }

  @Override
  public <T extends ListAdapter & Filterable> void setAdapter(@Nullable T adapter) {
    super.setAdapter(adapter);
    modalListPopup.setAdapter(getAdapter());
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    
    
    
    TextInputLayout layout = findTextInputLayoutAncestor();
    if (layout != null
        && layout.isProvidingHint()
        && super.getHint() == null
        && ManufacturerUtils.isMeizuDevice()) {
      setHint("");
    }
  }

  @Nullable
  @Override
  public CharSequence getHint() {
    
    
    TextInputLayout textInputLayout = findTextInputLayoutAncestor();
    if (textInputLayout != null && textInputLayout.isProvidingHint()) {
      return textInputLayout.getHint();
    }
    return super.getHint();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    
    
    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
      final int measuredWidth = getMeasuredWidth();
      setMeasuredDimension(
          Math.min(
              Math.max(measuredWidth, measureContentWidth()),
              MeasureSpec.getSize(widthMeasureSpec)),
          getMeasuredHeight());
    }
  }

  private int measureContentWidth() {
    ListAdapter adapter = getAdapter();
    TextInputLayout textInputLayout = findTextInputLayoutAncestor();
    if (adapter == null || textInputLayout == null) {
      return 0;
    }

    int width = 0;
    View itemView = null;
    int itemType = 0;
    final int widthMeasureSpec =
        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
    final int heightMeasureSpec =
        MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

    
    int start = Math.max(0, modalListPopup.getSelectedItemPosition());
    final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
    start = Math.max(0, end - MAX_ITEMS_MEASURED);
    for (int i = start; i < end; i++) {
      final int positionType = adapter.getItemViewType(i);
      if (positionType != itemType) {
        itemType = positionType;
        itemView = null;
      }
      itemView = adapter.getView(i, itemView, textInputLayout);
      if (itemView.getLayoutParams() == null) {
        itemView.setLayoutParams(new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT));
      }
      itemView.measure(widthMeasureSpec, heightMeasureSpec);
      width = Math.max(width, itemView.getMeasuredWidth());
    }
    
    Drawable background = modalListPopup.getBackground();
    if (background != null) {
      background.getPadding(tempRect);
      width += tempRect.left + tempRect.right;
    }
    
    int iconWidth = textInputLayout.getEndIconView().getMeasuredWidth();
    width += iconWidth;

    return width;
  }

  @Nullable
  private TextInputLayout findTextInputLayoutAncestor() {
    ViewParent parent = getParent();
    while (parent != null) {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T extends ListAdapter & Filterable> void updateText(Object selectedItem) {
    if (VERSION.SDK_INT >= 17) {
      setText(convertSelectionToString(selectedItem), false);
    } else {
      ListAdapter adapter = getAdapter();
      setAdapter(null);
      setText(convertSelectionToString(selectedItem));
      setAdapter((T) adapter);
    }
  }
}
