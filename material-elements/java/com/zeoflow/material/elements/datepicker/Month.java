
package com.zeoflow.material.elements.datepicker;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;


final class Month implements Comparable<Month>, Parcelable {

  
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    Calendar.JANUARY,
    Calendar.FEBRUARY,
    Calendar.MARCH,
    Calendar.APRIL,
    Calendar.MAY,
    Calendar.JUNE,
    Calendar.JULY,
    Calendar.AUGUST,
    Calendar.SEPTEMBER,
    Calendar.OCTOBER,
    Calendar.NOVEMBER,
    Calendar.DECEMBER
  })
  @interface Months {}

  @NonNull private final Calendar firstOfMonth;
  @NonNull private final String longName;
  @Months final int month;
  final int year;
  final int daysInWeek;
  final int daysInMonth;
  final long timeInMillis;

  private Month(@NonNull Calendar rawCalendar) {
    rawCalendar.set(Calendar.DAY_OF_MONTH, 1);
    firstOfMonth = UtcDates.getDayCopy(rawCalendar);
    month = firstOfMonth.get(Calendar.MONTH);
    year = firstOfMonth.get(Calendar.YEAR);
    daysInWeek = firstOfMonth.getMaximum(Calendar.DAY_OF_WEEK);
    daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
    longName = UtcDates.getYearMonthFormat().format(firstOfMonth.getTime());
    timeInMillis = firstOfMonth.getTimeInMillis();
  }

  
  @NonNull
  static Month create(long timeInMillis) {
    Calendar calendar = UtcDates.getUtcCalendar();
    calendar.setTimeInMillis(timeInMillis);
    return new Month(calendar);
  }

  
  @NonNull
  static Month create(int year, @Months int month) {
    Calendar calendar = UtcDates.getUtcCalendar();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    return new Month(calendar);
  }

  
  @NonNull
  static Month current() {
    return new Month(UtcDates.getTodayCalendar());
  }

  int daysFromStartOfWeekToFirstOfMonth() {
    int difference = firstOfMonth.get(Calendar.DAY_OF_WEEK) - firstOfMonth.getFirstDayOfWeek();
    if (difference < 0) {
      difference = difference + daysInWeek;
    }
    return difference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Month)) {
      return false;
    }
    Month that = (Month) o;
    return month == that.month && year == that.year;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {month, year};
    return Arrays.hashCode(hashedFields);
  }

  @Override
  public int compareTo(@NonNull Month other) {
    return firstOfMonth.compareTo(other.firstOfMonth);
  }

  
  int monthsUntil(@NonNull Month other) {
    if (firstOfMonth instanceof GregorianCalendar) {
      return (other.year - year) * 12 + (other.month - month);
    } else {
      throw new IllegalArgumentException("Only Gregorian calendars are supported.");
    }
  }

  long getStableId() {
    return firstOfMonth.getTimeInMillis();
  }

  
  long getDay(int day) {
    Calendar dayCalendar = UtcDates.getDayCopy(firstOfMonth);
    dayCalendar.set(Calendar.DAY_OF_MONTH, day);
    return dayCalendar.getTimeInMillis();
  }

  
  @NonNull
  Month monthsLater(int months) {
    Calendar laterMonth = UtcDates.getDayCopy(firstOfMonth);
    laterMonth.add(Calendar.MONTH, months);
    return new Month(laterMonth);
  }

  
  @NonNull
  String getLongName() {
    return longName;
  }

  

  
  public static final Parcelable.Creator<Month> CREATOR =
      new Parcelable.Creator<Month>() {
        @NonNull
        @Override
        public Month createFromParcel(@NonNull Parcel source) {
          int year = source.readInt();
          int month = source.readInt();
          return Month.create(year, month);
        }

        @NonNull
        @Override
        public Month[] newArray(int size) {
          return new Month[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeInt(year);
    dest.writeInt(month);
  }
}
