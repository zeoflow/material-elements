
package com.zeoflow.material.elements.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import com.zeoflow.material.elements.resources.MaterialAttributes;
import com.zeoflow.material.elements.resources.MaterialResources;


final class CalendarStyle {

  
  @NonNull final CalendarItemStyle day;
  
  @NonNull final CalendarItemStyle selectedDay;
  
  @NonNull final CalendarItemStyle todayDay;

  
  @NonNull final CalendarItemStyle year;
  
  @NonNull final CalendarItemStyle selectedYear;
  
  @NonNull final CalendarItemStyle todayYear;

  @NonNull final CalendarItemStyle invalidDay;

  
  @NonNull final Paint rangeFill;

  CalendarStyle(@NonNull Context context) {
    int calendarStyle =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.materialCalendarStyle, MaterialCalendar.class.getCanonicalName());
    TypedArray calendarAttributes =
        context.obtainStyledAttributes(calendarStyle, R.styleable.MaterialCalendar);

    day =
        CalendarItemStyle.create(
            context, calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayStyle, 0));
    invalidDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayInvalidStyle, 0));
    selectedDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_daySelectedStyle, 0));
    todayDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayTodayStyle, 0));
    ColorStateList rangeFillColorList =
        MaterialResources.getColorStateList(
            context, calendarAttributes, R.styleable.MaterialCalendar_rangeFillColor);

    year =
        CalendarItemStyle.create(
            context, calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearStyle, 0));
    selectedYear =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearSelectedStyle, 0));
    todayYear =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearTodayStyle, 0));

    rangeFill = new Paint();
    rangeFill.setColor(rangeFillColorList.getDefaultColor());

    calendarAttributes.recycle();
  }
}
