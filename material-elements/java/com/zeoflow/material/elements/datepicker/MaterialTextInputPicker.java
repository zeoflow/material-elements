
package com.zeoflow.material.elements.datepicker;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialTextInputPicker<S> extends PickerFragment<S> {

  private static final String DATE_SELECTOR_KEY = "DATE_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";

  @Nullable private DateSelector<S> dateSelector;
  @Nullable private CalendarConstraints calendarConstraints;

  @NonNull
  static <T> MaterialTextInputPicker<T> newInstance(
      @NonNull DateSelector<T> dateSelector, @NonNull CalendarConstraints calendarConstraints) {
    MaterialTextInputPicker<T> materialCalendar = new MaterialTextInputPicker<>();
    Bundle args = new Bundle();
    args.putParcelable(DATE_SELECTOR_KEY, dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(DATE_SELECTOR_KEY, dateSelector);
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    dateSelector = activeBundle.getParcelable(DATE_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return dateSelector.onCreateTextInputView(
        layoutInflater,
        viewGroup,
        bundle,
        calendarConstraints,
        new OnSelectionChangedListener<S>() {
          @Override
          public void onSelectionChanged(S selection) {
            for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
              listener.onSelectionChanged(selection);
            }
          }

          @Override
          void onIncompleteSelectionChanged() {
            for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
              listener.onIncompleteSelectionChanged();
            }
          }
        });
  }

  @NonNull
  @Override
  public DateSelector<S> getDateSelector() {
    if (dateSelector == null) {
      throw new IllegalStateException(
          "dateSelector should not be null. Use MaterialTextInputPicker#newInstance() to create"
              + " this fragment with a DateSelector, and call this method after the fragment has"
              + " been created.");
    }
    return dateSelector;
  }
}
