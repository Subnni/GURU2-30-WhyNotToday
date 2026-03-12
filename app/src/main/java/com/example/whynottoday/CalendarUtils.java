package com.example.whynottoday;



import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.w3c.dom.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public class CalendarUtils
{
    public static LocalDate selectedDate = LocalDate.now();

    public static String formattedDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    public static String formattedTime(LocalTime time)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        return time.format(formatter);
    }

    public static String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
        return date.format(formatter);
    }

    public static String weekFromDate(LocalDate date) {
        int week = date.get(java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH);
        return week + "주 차";
    }


    public static ArrayList<LocalDate> daysInMonthArray(LocalDate date)
    {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = CalendarUtils.selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
                daysInMonthArray.add(null);
            else
                daysInMonthArray.add(LocalDate.of(selectedDate.getYear(),selectedDate.getMonth(),i - dayOfWeek));
        }
        return  daysInMonthArray;
    }

    public static ArrayList<LocalDate> daysInWeekArray(LocalDate selectedDate)
    {
        ArrayList<LocalDate> days = new ArrayList<>();
        LocalDate current = sundayForDate(selectedDate);
        LocalDate endDate = current.plusWeeks(1);

        while (current.isBefore(endDate))
        {
            days.add(current);
            current = current.plusDays(1);
        }
        return days;
    }

    private static LocalDate sundayForDate(LocalDate current)
    {
        LocalDate oneWeekAgo = current.minusWeeks(1);

        while (current.isAfter(oneWeekAgo))
        {
            if(current.getDayOfWeek() == DayOfWeek.SUNDAY)
                return current;

            current = current.minusDays(1);
        }

        return null;
    }

    //박스 색상 및 농도 변경(추후 수정 필요..)
    public static void updateBoxColor(View view, int score, String theme) {
        //view.setBackgroundResource(R.drawable.rounded_box);
        ////view.setBackgroundResource(R.drawable.box_color);
        //
        ////1. 테두리 색상
        view.setActivated("red".equals(theme));
        //
        ////2. 배경 색상
        int color = ContextCompat.getColor(
                view.getContext(),
                theme.equals("red") ? R.color.red : R.color.blue_100
        );

        //view.setBackgroundTintList(ColorStateList.valueOf(color));

        int alpha = (int)(255 * (score / 100f));
        int argb = Color.argb(alpha, Color.red(color), Color.green(color),  Color.blue(color));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6 * view.getResources().getDisplayMetrics().density);
        int px = dpToPx(view.getContext(), 1.9F);
        drawable.setStroke(px, color);
        drawable.setColor(argb);
        view.setBackground(drawable);
    }

    private static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public static void updateTextColor(TextView view, TextView view2, TextView view3, String theme){
        int color = theme.equals("red") ? R.color.red : R.color.blue_100;
        view.setTextColor(ContextCompat.getColor(view.getContext(), color));
        view2.setTextColor(ContextCompat.getColor(view.getContext(), color));
        view3.setTextColor(ContextCompat.getColor(view.getContext(), color));
    }




}
