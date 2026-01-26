package com.example.whynottoday;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        final LocalDate date = days.get(position);
        if(date == null){
            holder.dayOfMonth.setText("");
            holder.dayOfWeek.setText("");
        }
        else
        {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            holder.dayOfWeek.setText(getDayOfWeekName(date));
            if(holder.dayOfWeek.getText()=="일") {
                holder.dayOfWeek.setTextColor(Color.RED);
                holder.dayOfMonth.setTextColor(Color.RED);
            }
            if(holder.dayOfWeek.getText()=="토") {
                holder.dayOfWeek.setTextColor(Color.BLUE);
                holder.dayOfMonth.setTextColor(Color.BLUE);
            }
            //선택된 날짜의 배경색 변경
            if(date.equals(CalendarUtils.selectedDate))
                holder.parentView.setBackgroundColor(Color.parseColor("#D1E7FF"));

            //날짜별 핑계 농도 계산하여 박스 배경색 적용
            Context context = holder.itemView.getContext();
            if (sqlitedb == null) {
                DBManager dbManager = new DBManager(context, "WhyNotTodayDB.db", null, 1);
                sqlitedb = dbManager.getReadableDatabase();
            }
            Cursor cursor = null;
            String query = "SELECT count(*) FROM todoTBL " +
                    "WHERE date_time LIKE '" + date.toString() + "%' " +
                    "AND is_done = 0";
            cursor = sqlitedb.rawQuery(query, null);
            int incompleteTaskCount = 0;
            if (cursor.moveToFirst()) { incompleteTaskCount = cursor.getInt(0); }
            else { incompleteTaskCount = 0; }
            cursor.close();

            String query2 = "SELECT count(*) FROM todoTBL " +
                    "INNER JOIN excuseTBL ON todoTBL.todo_id = excuseTBL.todo_id " +
                    "WHERE todoTBL.date_time LIKE '" + date.toString() + "%'";
            cursor = sqlitedb.rawQuery(query2, null);
            int excuseCount = 0;
            if (cursor.moveToFirst()) { excuseCount = cursor.getInt(0); }
            else { excuseCount = 0; }
            cursor.close();

            int excuseRatio = 0;
            if(incompleteTaskCount==0) { excuseRatio = 0;}
            else {
                excuseRatio = (int)(((float)excuseCount /(float)incompleteTaskCount) * 100);
            }
            CalendarUtils.updateBoxColor(holder.roundedBox, excuseRatio);
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
