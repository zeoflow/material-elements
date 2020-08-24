

package com.zeoflow.material.elements.datepicker;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.zeoflow.material.elements.datepicker.CalendarConstraints.DateValidator;
import java.util.Arrays;


public class DateValidatorPointBackward implements DateValidator {

  private final long point;

  private DateValidatorPointBackward(long point) {
    this.point = point;
  }

  
  @NonNull
  public static DateValidatorPointBackward before(long point) {
    return new DateValidatorPointBackward(point);
  }

  
  @NonNull
  public static DateValidatorPointBackward now() {
    return before(UtcDates.getTodayCalendar().getTimeInMillis());
  }

  
  public static final Parcelable.Creator<DateValidatorPointBackward> CREATOR =
      new Parcelable.Creator<DateValidatorPointBackward>() {
        @NonNull
        @Override
        public DateValidatorPointBackward createFromParcel(@NonNull Parcel source) {
          return new DateValidatorPointBackward(source.readLong());
        }

        @NonNull
        @Override
        public DateValidatorPointBackward[] newArray(int size) {
          return new DateValidatorPointBackward[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date <= point;
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
    if (!(o instanceof DateValidatorPointBackward)) {
      return false;
    }

    DateValidatorPointBackward that = (DateValidatorPointBackward) o;
    return point == that.point;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {point};
    return Arrays.hashCode(hashedFields);
  }
}
