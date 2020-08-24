
package com.zeoflow.material.elements.datepicker;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Calendar;


public final class CalendarConstraints implements Parcelable {

  @NonNull private final Month start;
  @NonNull private final Month end;
  @NonNull private final Month openAt;
  private final DateValidator validator;

  private final int yearSpan;
  private final int monthSpan;

  
  public interface DateValidator extends Parcelable {

    
    boolean isValid(long date);
  }

  private CalendarConstraints(
      @NonNull Month start, @NonNull Month end, @NonNull Month openAt, DateValidator validator) {
    this.start = start;
    this.end = end;
    this.openAt = openAt;
    this.validator = validator;
    if (start.compareTo(openAt) > 0) {
      throw new IllegalArgumentException("start Month cannot be after current Month");
    }
    if (openAt.compareTo(end) > 0) {
      throw new IllegalArgumentException("current Month cannot be after end Month");
    }
    monthSpan = start.monthsUntil(end) + 1;
    yearSpan = end.year - start.year + 1;
  }

  boolean isWithinBounds(long date) {
    return start.getDay(1) <= date && date <= end.getDay(end.daysInMonth);
  }

  
  public DateValidator getDateValidator() {
    return validator;
  }

  
  @NonNull
  Month getStart() {
    return start;
  }

  
  @NonNull
  Month getEnd() {
    return end;
  }

  
  @NonNull
  Month getOpenAt() {
    return openAt;
  }

  
  int getMonthSpan() {
    return monthSpan;
  }

  
  int getYearSpan() {
    return yearSpan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarConstraints)) {
      return false;
    }
    CalendarConstraints that = (CalendarConstraints) o;
    return start.equals(that.start)
        && end.equals(that.end)
        && openAt.equals(that.openAt)
        && validator.equals(that.validator);
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {start, end, openAt, validator};
    return Arrays.hashCode(hashedFields);
  }

  

  
  public static final Parcelable.Creator<CalendarConstraints> CREATOR =
      new Parcelable.Creator<CalendarConstraints>() {
        @NonNull
        @Override
        public CalendarConstraints createFromParcel(@NonNull Parcel source) {
          Month start = source.readParcelable(Month.class.getClassLoader());
          Month end = source.readParcelable(Month.class.getClassLoader());
          Month openAt = source.readParcelable(Month.class.getClassLoader());
          DateValidator validator = source.readParcelable(DateValidator.class.getClassLoader());
          return new CalendarConstraints(start, end, openAt, validator);
        }

        @NonNull
        @Override
        public CalendarConstraints[] newArray(int size) {
          return new CalendarConstraints[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(start,  0);
    dest.writeParcelable(end,  0);
    dest.writeParcelable(openAt,  0);
    dest.writeParcelable(validator,  0);
  }

  
  Month clamp(Month month) {
    if (month.compareTo(start) < 0) {
      return start;
    }

    if (month.compareTo(end) > 0) {
      return end;
    }

    return month;
  }

  
  public static final class Builder {

    
    static final long DEFAULT_START =
        UtcDates.canonicalYearMonthDay(Month.create(1900, Calendar.JANUARY).timeInMillis);
    
    static final long DEFAULT_END =
        UtcDates.canonicalYearMonthDay(Month.create(2100, Calendar.DECEMBER).timeInMillis);

    private static final String DEEP_COPY_VALIDATOR_KEY = "DEEP_COPY_VALIDATOR_KEY";

    private long start = DEFAULT_START;
    private long end = DEFAULT_END;
    private Long openAt;
    private DateValidator validator = DateValidatorPointForward.from(Long.MIN_VALUE);

    public Builder() {}

    Builder(@NonNull CalendarConstraints clone) {
      start = clone.start.timeInMillis;
      end = clone.end.timeInMillis;
      openAt = clone.openAt.timeInMillis;
      validator = clone.validator;
    }

    
    @NonNull
    public Builder setStart(long month) {
      start = month;
      return this;
    }

    
    @NonNull
    public Builder setEnd(long month) {
      end = month;
      return this;
    }

    
    @NonNull
    public Builder setOpenAt(long month) {
      openAt = month;
      return this;
    }

    
    @NonNull
    public Builder setValidator(DateValidator validator) {
      this.validator = validator;
      return this;
    }

    
    @NonNull
    public CalendarConstraints build() {
      if (openAt == null) {
        long today = MaterialDatePicker.thisMonthInUtcMilliseconds();
        openAt = start <= today && today <= end ? today : start;
      }
      Bundle deepCopyBundle = new Bundle();
      deepCopyBundle.putParcelable(DEEP_COPY_VALIDATOR_KEY, validator);
      return new CalendarConstraints(
          Month.create(start),
          Month.create(end),
          Month.create(openAt),
          (DateValidator) deepCopyBundle.getParcelable(DEEP_COPY_VALIDATOR_KEY));
    }
  }
}
