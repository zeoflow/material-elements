
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Calendar;


class MonthAdapter extends BaseAdapter {

  
  static final int MAXIMUM_WEEKS = UtcDates.getUtcCalendar().getMaximum(Calendar.WEEK_OF_MONTH);

  final Month month;
  
  final DateSelector<?> dateSelector;

  CalendarStyle calendarStyle;
  final CalendarConstraints calendarConstraints;

  MonthAdapter(Month month, DateSelector<?> dateSelector, CalendarConstraints calendarConstraints) {
    this.month = month;
    this.dateSelector = dateSelector;
    this.calendarConstraints = calendarConstraints;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  
  @Nullable
  @Override
  public Long getItem(int position) {
    if (position < month.daysFromStartOfWeekToFirstOfMonth() || position > lastPositionInMonth()) {
      return null;
    }
    return month.getDay(positionToDay(position));
  }

  @Override
  public long getItemId(int position) {
    return position / month.daysInWeek;
  }

  
  @Override
  public int getCount() {
    return month.daysInMonth + firstPositionInMonth();
  }

  @NonNull
  @Override
  public TextView getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    initializeStyles(parent.getContext());
    TextView day = (TextView) convertView;
    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
      day = (TextView) layoutInflater.inflate(R.layout.mtrl_calendar_day, parent, false);
    }
    int offsetPosition = position - firstPositionInMonth();
    if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
      day.setVisibility(View.GONE);
      day.setEnabled(false);
    } else {
      int dayNumber = offsetPosition + 1;
      
      day.setTag(month);
      day.setText(String.valueOf(dayNumber));
      long dayInMillis = month.getDay(dayNumber);
      if (month.year == Month.current().year) {
        day.setContentDescription(DateStrings.getMonthDayOfWeekDay(dayInMillis));
      } else {
        day.setContentDescription(DateStrings.getYearMonthDayOfWeekDay(dayInMillis));
      }
      day.setVisibility(View.VISIBLE);
      day.setEnabled(true);
    }

    Long date = getItem(position);
    if (date == null) {
      return day;
    }
    if (calendarConstraints.getDateValidator().isValid(date)) {
      day.setEnabled(true);
      for (long selectedDay : dateSelector.getSelectedDays()) {
        if (UtcDates.canonicalYearMonthDay(date) == UtcDates.canonicalYearMonthDay(selectedDay)) {
          calendarStyle.selectedDay.styleItem(day);
          return day;
        }
      }

      if (UtcDates.getTodayCalendar().getTimeInMillis() == date) {
        calendarStyle.todayDay.styleItem(day);
        return day;
      } else {
        calendarStyle.day.styleItem(day);
        return day;
      }
    } else {
      day.setEnabled(false);
      calendarStyle.invalidDay.styleItem(day);
      return day;
    }
  }

  private void initializeStyles(Context context) {
    if (calendarStyle == null) {
      calendarStyle = new CalendarStyle(context);
    }
  }

  
  int firstPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth();
  }

  
  int lastPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth() + month.daysInMonth - 1;
  }

  
  int positionToDay(int position) {
    return position - month.daysFromStartOfWeekToFirstOfMonth() + 1;
  }

  
  int dayToPosition(int day) {
    int offsetFromFirst = day - 1;
    return firstPositionInMonth() + offsetFromFirst;
  }

  
  boolean withinMonth(int position) {
    return position >= firstPositionInMonth() && position <= lastPositionInMonth();
  }

  
  boolean isFirstInRow(int position) {
    return position % month.daysInWeek == 0;
  }

  
  boolean isLastInRow(int position) {
    return (position + 1) % month.daysInWeek == 0;
  }
}
