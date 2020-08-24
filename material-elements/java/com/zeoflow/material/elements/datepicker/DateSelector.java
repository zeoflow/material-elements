
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import java.util.Collection;


@RestrictTo(Scope.LIBRARY_GROUP)
public interface DateSelector<S> extends Parcelable {

  
  @Nullable
  S getSelection();

  
  boolean isSelectionComplete();

  
  void setSelection(@NonNull S selection);

  
  void select(long selection);

  
  @NonNull
  Collection<Long> getSelectedDays();

  
  @NonNull
  Collection<Pair<Long, Long>> getSelectedRanges();

  @NonNull
  String getSelectionDisplayString(Context context);

  @StringRes
  int getDefaultTitleResId();

  @StyleRes
  int getDefaultThemeResId(Context context);

  @NonNull
  View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      @NonNull CalendarConstraints constraints,
      @NonNull OnSelectionChangedListener<S> listener);
}
