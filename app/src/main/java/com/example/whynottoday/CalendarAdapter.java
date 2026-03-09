package com.example.whynottoday;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    private SQLiteDatabase sqlitedb;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener)
    {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    //리사이클러뷰 - 각 뷰 생성 클래스
    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        if(days.size() > 15) //month view
//            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
//        else // week view
//            layoutParams.height = (int)parent.getHeight();

        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        Context context = holder.itemView.getContext();
        final LocalDate date = days.get(position);
        if(date == null){
            holder.dayOfMonth.setText("");
            holder.dayOfWeek.setText("");
        }
        else
        {
            //일주일 날짜 표시
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));



            //today 글자 색/배경 색 변경
            LocalDate today = LocalDate.now();
            if (date.equals(today)) {
//                holder.parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_20)));
                holder.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.dayOfWeek.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray)));
            } else{
                holder.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.gray));
                holder.dayOfWeek.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.transparent)));
            }

            //토, 일요일 글자 색 변경
            //단, today가 토/일일 시 글자 색 변경 X
            holder.dayOfWeek.setText(getDayOfWeekName(date));
            if("일".equals(holder.dayOfWeek.getText().toString())
                    && (today.getDayOfWeek() != java.time.DayOfWeek.SUNDAY)) {
                holder.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
            if("토".equals(holder.dayOfWeek.getText().toString())
                    && (today.getDayOfWeek() != java.time.DayOfWeek.SATURDAY)) {
                holder.dayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.blue_100));
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(context, R.color.blue_100));
            }

            //선택된 날짜의 배경색 변경
            holder.parentView.setSelected(date.equals(CalendarUtils.selectedDate));

            //날짜별 핑계 농도 계산하여 박스 배경색 적용
            if (sqlitedb == null) {
                DBManager dbManager = new DBManager(context, "WhyNotTodayDB.db", null, 1);
                sqlitedb = dbManager.getReadableDatabase();
            }
            Cursor cursor = null;

            String query = "SELECT count(*) FROM todoTBL " +
                    "WHERE date_time LIKE '" + date.toString() + "%' ";
            cursor = sqlitedb.rawQuery(query, null);
            int taskCount = 0;
            if (cursor.moveToFirst()) { taskCount = cursor.getInt(0); }
            else { taskCount = 0; }
            cursor.close();

            String query2 = "SELECT count(*) FROM todoTBL " +
                    "WHERE date_time LIKE '" + date.toString() + "%' " +
                    "AND is_done = 1";
            cursor = sqlitedb.rawQuery(query2, null);
            int achievedTaskCount = 0;
            if (cursor.moveToFirst()) { achievedTaskCount = cursor.getInt(0); }
            else { achievedTaskCount = 0; }
            cursor.close();

            int achieveRatio = 0;
            if(taskCount==0) { achieveRatio = 0;}
            else {
                achieveRatio = (int)(((float)achievedTaskCount /(float)taskCount) * 100);
            }
            CalendarUtils.updateBoxColor(holder.roundedBox, achieveRatio);
            holder.achieveCountTextView.setText(String.valueOf(achievedTaskCount));
            holder.todoCountTextView.setText(String.valueOf(taskCount));

        }
    }

    @Override
    public int getItemCount()
    {
        return days.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, LocalDate date);
    }

    //요일 한글로 변환
    private String getDayOfWeekName(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SUNDAY:    return "일";
            case MONDAY:    return "월";
            case TUESDAY:   return "화";
            case WEDNESDAY: return "수";
            case THURSDAY:  return "목";
            case FRIDAY:    return "금";
            case SATURDAY:  return "토";
            default:        return "";
        }
    }
}
