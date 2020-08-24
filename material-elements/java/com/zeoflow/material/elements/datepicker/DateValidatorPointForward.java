
package com.zeoflow.material.elements.datepicker;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.zeoflow.material.elements.datepicker.CalendarConstraints.DateValidator;
import java.util.Arrays;


public class DateValidatorPointForward implements DateValidator {

  private final long point;

  private DateValidatorPointForward(long point) {
    this.point = point;
  }

  
  @NonNull
  public static DateValidatorPointForward from(long point) {
    return new DateValidatorPointForward(point);
  }

  
  @NonNull
  public static DateValidatorPointForward now() {
    return from(UtcDates.getTodayCalendar().getTimeInMillis());
  }

  
  public static final Parcelable.Creator<DateValidatorPointForward> CREATOR =
      new Parcelable.Creator<DateValidatorPointForward>() {
        @NonNull
        @Override
        public DateValidatorPointForward createFromParcel(@NonNull Parcel source) {
          return new DateValidatorPointForward(source.readLong());
        }

        @NonNull
        @Override
        public DateValidatorPointForward[] newArray(int size) {
          return new DateValidatorPointForward[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date >= point;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeLong(point);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateValidatorPointForward)) {
      return false;
    }
    DateValidatorPointForward that = (DateValidatorPointForward) o;
    return point == that.point;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {point};
    return Arrays.hashCode(hashedFields);
  }
}
