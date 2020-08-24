
package com.zeoflow.material.elements.datepicker;

import androidx.annotation.Nullable;
import java.util.Calendar;
import java.util.TimeZone;


class TimeSource {

  private static final TimeSource SYSTEM_TIME_SOURCE = new TimeSource(null, null);

  @Nullable private final Long fixedTimeMs;

  @Nullable private final TimeZone fixedTimeZone;

  private TimeSource(@Nullable final Long fixedTimeMs, @Nullable final TimeZone fixedTimeZone) {
    this.fixedTimeMs = fixedTimeMs;
    this.fixedTimeZone = fixedTimeZone;
  }

  
  static TimeSource system() {
    return SYSTEM_TIME_SOURCE;
  }

  
  static TimeSource fixed(long epochMs, @Nullable TimeZone timeZone) {
    return new TimeSource(epochMs, timeZone);
  }

  
  static TimeSource fixed(long epochMs) {
    return new TimeSource(epochMs, null);
  }

  
  Calendar now() {
    return now(fixedTimeZone);
  }

  
  Calendar now(@Nullable TimeZone timeZone) {
    Calendar calendar = timeZone == null ? Calendar.getInstance() : Calendar.getInstance(timeZone);
    if (fixedTimeMs != null) {
      calendar.setTimeInMillis(fixedTimeMs);
    }

    return calendar;
  }
}
