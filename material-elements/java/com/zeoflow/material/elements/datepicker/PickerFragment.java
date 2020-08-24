
package com.zeoflow.material.elements.datepicker;

import androidx.fragment.app.Fragment;
import java.util.LinkedHashSet;

abstract class PickerFragment<S> extends Fragment {

  protected final LinkedHashSet<OnSelectionChangedListener<S>> onSelectionChangedListeners =
      new LinkedHashSet<>();

  abstract DateSelector<S> getDateSelector();

  
  boolean addOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
    return onSelectionChangedListeners.add(listener);
  }

  
  boolean removeOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
    return onSelectionChangedListeners.remove(listener);
  }

  
  void clearOnSelectionChangedListeners() {
    onSelectionChangedListeners.clear();
  }
}
