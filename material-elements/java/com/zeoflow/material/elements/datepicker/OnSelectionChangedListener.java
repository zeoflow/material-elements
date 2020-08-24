
package com.zeoflow.material.elements.datepicker;

abstract class OnSelectionChangedListener<S> {

  abstract void onSelectionChanged(S selection);

  void onIncompleteSelectionChanged() {}
}
