
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;


class UtcDates {

  static final String UTC = "UTC";

  static AtomicReference<TimeSource> timeSourceRef = new AtomicReference<>();

  static void setTimeSource(@Nullable TimeSource timeSource) {
    timeSourceRef.set(timeSource);
  }

  static TimeSource getTimeSource() {
    TimeSource timeSource = timeSourceRef.get();
    return timeSource == null ? TimeSource.system() : timeSource;
  }

  private UtcDates() {}

  private static TimeZone getTimeZone() {
    return TimeZone.getTimeZone(UTC);
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.util.TimeZone getUtcAndroidTimeZone() {
    return android.icu.util.TimeZone.getTimeZone(UTC);
  }

  
  static Calendar getTodayCalendar() {
    Calendar today = getTimeSource().now();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
    today.setTimeZone(getTimeZone());
    return today;
  }

  
  static Calendar getUtcCalendar() {
    return getUtcCalendarOf(null);
  }

  
  static Calendar getUtcCalendarOf(@Nullable Calendar rawCalendar) {
    Calendar utc = Calendar.getInstance(getTimeZone());
    if (rawCalendar == null) {
      utc.clear();
    } else {
      utc.setTimeInMillis(rawCalendar.getTimeInMillis());
    }
    return utc;
  }

  
  static Calendar getDayCopy(Calendar rawCalendar) {
    Calendar rawCalendarInUtc = getUtcCalendarOf(rawCalendar);
    Calendar utcCalendar = getUtcCalendar();
    utcCalendar.set(
        rawCalendarInUtc.get(Calendar.YEAR),
        rawCalendarInUtc.get(Calendar.MONTH),
        rawCalendarInUtc.get(Calendar.DAY_OF_MONTH));
    return utcCalendar;
  }

  
  static long canonicalYearMonthDay(long rawDate) {
    Calendar rawCalendar = getUtcCalendar();
    rawCalendar.setTimeInMillis(rawDate);
    Calendar sanitizedStartItem = getDayCopy(rawCalendar);
    return sanitizedStartItem.getTimeInMillis();
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.text.DateFormat getAndroidFormat(String pattern, Locale locale) {
    android.icu.text.DateFormat format =
        android.icu.text.DateFormat.getInstanceForSkeleton(pattern, locale);
    format.setTimeZone(getUtcAndroidTimeZone());
    return format;
  }

  private static DateFormat getFormat(int style, Locale locale) {
    DateFormat format = DateFormat.getDateInstance(style, locale);
    format.setTimeZone(getTimeZone());
    return format;
  }

  static SimpleDateFormat getTextInputFormat() {
    String pattern =
        ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()))
            .toLocalizedPattern()
            .replaceAll("\\s+", "");
    SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
    format.setTimeZone(UtcDates.getTimeZone());
    format.setLenient(false);
    return format;
  }

  static String getTextInputHint(Resources res, SimpleDateFormat format) {
    String formatHint = format.toLocalizedPattern();
    String yearChar = res.getString(R.string.mtrl_picker_text_input_year_abbr);
    String monthChar = res.getString(R.string.mtrl_picker_text_input_month_abbr);
    String dayChar = res.getString(R.string.mtrl_picker_text_input_day_abbr);

    return formatHint.replaceAll("d", dayChar).replaceAll("M", monthChar).replaceAll("y", yearChar);
  }

  static SimpleDateFormat getSimpleFormat(String pattern) {
    return getSimpleFormat(pattern, Locale.getDefault());
  }

  private static SimpleDateFormat getSimpleFormat(String pattern, Locale locale) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
    format.setTimeZone(getTimeZone());
    return format;
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getYearAbbrMonthDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_ABBR_MONTH_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getAbbrMonthDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.ABBR_MONTH_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getAbbrMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.ABBR_MONTH_WEEKDAY_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getYearAbbrMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_ABBR_MONTH_WEEKDAY_DAY, locale);
  }

  static DateFormat getMediumFormat() {
    return getMediumFormat(Locale.getDefault());
  }

  static DateFormat getMediumFormat(Locale locale) {
    return getFormat(DateFormat.MEDIUM, locale);
  }

  static DateFormat getMediumNoYear() {
    return getMediumNoYear(Locale.getDefault());
  }

  static DateFormat getMediumNoYear(Locale locale) {
    SimpleDateFormat format = (SimpleDateFormat) getMediumFormat(locale);
    format.applyPattern(removeYearFromDateFormatPattern(format.toPattern()));
    return format;
  }

  static DateFormat getFullFormat() {
    return getFullFormat(Locale.getDefault());
  }

  static DateFormat getFullFormat(Locale locale) {
    return getFormat(DateFormat.FULL, locale);
  }

  static SimpleDateFormat getYearMonthFormat() {
    return getYearMonthFormat(Locale.getDefault());
  }

  private static SimpleDateFormat getYearMonthFormat(Locale locale) {
    return getSimpleFormat("LLLL, yyyy", locale);
  }

  @NonNull
  private static String removeYearFromDateFormatPattern(@NonNull String pattern) {
    String yearCharacters = "yY";

    int yearPosition = findCharactersInDateFormatPattern(pattern, yearCharacters, 1, 0);

    if (yearPosition >= pattern.length()) {
      
      return pattern;
    }

    String monthDayCharacters = "EMd";
    int yearEndPosition =
        findCharactersInDateFormatPattern(pattern, monthDayCharacters, 1, yearPosition);

    if (yearEndPosition < pattern.length()) {
      monthDayCharacters += ",";
    }

    int yearStartPosition =
        findCharactersInDateFormatPattern(pattern, monthDayCharacters, -1, yearPosition);
    yearStartPosition++;

    String yearPattern = pattern.substring(yearStartPosition, yearEndPosition);
    return pattern.replace(yearPattern, " ").trim();
  }

  private static int findCharactersInDateFormatPattern(
      @NonNull String pattern,
      @NonNull String characterSequence,
      int increment,
      int initialPosition) {
    int position = initialPosition;

    
    while ((position >= 0 && position < pattern.length())
        && characterSequence.indexOf(pattern.charAt(position)) == -1) {

      
      if (pattern.charAt(position) == '\'') {
        position += increment;
        while ((position >= 0 && position < pattern.length()) && pattern.charAt(position) != '\'') {
          position += increment;
        }
      }

      position += increment;
    }

    return position;
  }
}
