
package com.zeoflow.material.elements.datepicker;

import static androidx.core.util.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.zeoflow.material.elements.datepicker.CalendarConstraints.DateValidator;
import java.util.List;


public final class CompositeDateValidator implements DateValidator {

  @NonNull private final List<DateValidator> validators;

  private CompositeDateValidator(@NonNull List<DateValidator> validators) {
    this.validators = validators;
  }

  
  @NonNull
  public static DateValidator allOf(@NonNull List<DateValidator> validators) {
    return new CompositeDateValidator(validators);
  }

  
  public static final Creator<CompositeDateValidator> CREATOR =
      new Creator<CompositeDateValidator>() {
        @NonNull
        @Override
        public CompositeDateValidator createFromParcel(@NonNull Parcel source) {
          @SuppressWarnings("unchecked")
          List<DateValidator> validators =
              source.readArrayList(DateValidator.class.getClassLoader());
          return new CompositeDateValidator(checkNotNull(validators));
        }

        @NonNull
        @Override
        public CompositeDateValidator[] newArray(int size) {
          return new CompositeDateValidator[size];
        }
      };

  
  @Override
  public boolean isValid(long date) {
    for (DateValidator validator : validators) {
      if (validator == null) {
        continue;
      }
      if (!validator.isValid(date)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeList(validators);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof CompositeDateValidator)) {
      return false;
    }

    CompositeDateValidator that = (CompositeDateValidator) o;

    return validators.equals(that.validators);
  }

  @Override
  public int hashCode() {
    return validators.hashCode();
  }
}
