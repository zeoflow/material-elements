
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import com.zeoflow.material.elements.textfield.TextInputLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

abstract class DateFormatTextWatcher implements TextWatcher {

  private final String formatHint;
  private final DateFormat dateFormat;
  @NonNull private final TextInputLayout textInputLayout;
  private final CalendarConstraints constraints;
  private final String outOfRange;

  DateFormatTextWatcher(
      String formatHint,
      DateFormat dateFormat,
      @NonNull TextInputLayout textInputLayout,
      CalendarConstraints constraints) {
    this.formatHint = formatHint;
    this.dateFormat = dateFormat;
    this.textInputLayout = textInputLayout;
    this.constraints = constraints;
    this.outOfRange = textInputLayout.getContext().getString(R.string.mtrl_picker_out_of_range);
  }

  abstract void onValidDate(@Nullable Long day);

  void onInvalidDate() {}

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
    if (TextUtils.isEmpty(s)) {
      textInputLayout.setError(null);
      onValidDate(null);
      return;
    }

    try {
      Date date = dateFormat.parse(s.toString());

      textInputLayout.setError(null);
      long milliseconds = date.getTime();
      if (constraints.getDateValidator().isValid(milliseconds)
          && constraints.isWithinBounds(milliseconds)) {
        onValidDate(date.getTime());
      } else {
        textInputLayout.setError(
            String.format(outOfRange, DateStrings.getDateString(milliseconds)));
        onInvalidDate();
      }
    } catch (ParseException e) {
      String invalidFormat =
          textInputLayout.getContext().getString(R.string.mtrl_picker_invalid_format);
      String useLine =
          String.format(
              textInputLayout.getContext().getString(R.string.mtrl_picker_invalid_format_use),
              formatHint);
      String exampleLine =
          String.format(
              textInputLayout.getContext().getString(R.string.mtrl_picker_invalid_format_example),
              dateFormat.format(new Date(UtcDates.getTodayCalendar().getTimeInMillis())));
      textInputLayout.setError(invalidFormat + "\n" + useLine + "\n" + exampleLine);
      onInvalidDate();
    }
  }

  @Override
  public void afterTextChanged(Editable s) {}
}
