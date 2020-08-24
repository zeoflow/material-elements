
package com.zeoflow.material.elements.datepicker;

import android.icu.text.DateFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


class DateStrings {

  private DateStrings() {}

  static String getYearMonthDay(long timeInMillis) {
    return getYearMonthDay(timeInMillis, Locale.getDefault());
  }

  
  static String getYearMonthDay(long timeInMillis, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return UtcDates.getYearAbbrMonthDayFormat(locale).format(new Date(timeInMillis));
    }
    return UtcDates.getMediumFormat(locale).format(new Date(timeInMillis));
  }

  static String getMonthDay(long timeInMillis) {
    return getMonthDay(timeInMillis, Locale.getDefault());
  }

  
  static String getMonthDay(long timeInMillis, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return UtcDates.getAbbrMonthDayFormat(locale).format(new Date(timeInMillis));
    }
    return UtcDates.getMediumNoYear(locale).format(new Date(timeInMillis));
  }

  static String getMonthDayOfWeekDay(long timeInMillis) {
    return getMonthDayOfWeekDay(timeInMillis, Locale.getDefault());
  }

  static String getMonthDayOfWeekDay(long timeInMillis, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return UtcDates.getAbbrMonthWeekdayDayFormat(locale).format(new Date(timeInMillis));
    }
    return UtcDates.getFullFormat(locale).format(new Date(timeInMillis));
  }

  static String getYearMonthDayOfWeekDay(long timeInMillis) {
    return getYearMonthDayOfWeekDay(timeInMillis, Locale.getDefault());
  }

  static String getYearMonthDayOfWeekDay(long timeInMillis, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return UtcDates.getYearAbbrMonthWeekdayDayFormat(locale).format(new Date(timeInMillis));
    }
    return UtcDates.getFullFormat(locale).format(new Date(timeInMillis));
  }

  static String getDateString(long timeInMillis) {
    return getDateString(timeInMillis, null);
  }

  
  static String getDateString(long timeInMillis, @Nullable SimpleDateFormat userDefinedDateFormat) {
    Calendar currentCalendar = UtcDates.getTodayCalendar();
    Calendar calendarDate = UtcDates.getUtcCalendar();
    calendarDate.setTimeInMillis(timeInMillis);

    if (userDefinedDateFormat != null) {
      Date date = new Date(timeInMillis);
      return userDefinedDateFormat.format(date);
    } else if (currentCalendar.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR)) {
      return getMonthDay(timeInMillis);
    }
    return getYearMonthDay(timeInMillis);
  }

  static Pair<String, String> getDateRangeString(@Nullable Long start, @Nullable Long end) {
    return getDateRangeString(start, end, null);
  }

  
  static Pair<String, String> getDateRangeString(
      @Nullable Long start, @Nullable Long end, @Nullable SimpleDateFormat userDefinedDateFormat) {
    if (start == null && end == null) {
      return Pair.create(null, null);
    } else if (start == null) {
      return Pair.create(null, getDateString(end, userDefinedDateFormat));
    } else if (end == null) {
      return Pair.create(getDateString(start, userDefinedDateFormat), null);
    }

    Calendar currentCalendar = UtcDates.getTodayCalendar();
    Calendar startCalendar = UtcDates.getUtcCalendar();
    startCalendar.setTimeInMillis(start);
    Calendar endCalendar = UtcDates.getUtcCalendar();
    endCalendar.setTimeInMillis(end);

    if (userDefinedDateFormat != null) {
      Date startDate = new Date(start);
      Date endDate = new Date(end);
      return Pair.create(
          userDefinedDateFormat.format(startDate), userDefinedDateFormat.format(endDate));
    } else if (startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR)) {
      if (startCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
        return Pair.create(
            getMonthDay(start, Locale.getDefault()), getMonthDay(end, Locale.getDefault()));
      } else {
        return Pair.create(
            getMonthDay(start, Locale.getDefault()), getYearMonthDay(end, Locale.getDefault()));
      }
    }
    return Pair.create(
        getYearMonthDay(start, Locale.getDefault()), getYearMonthDay(end, Locale.getDefault()));
  }
}
