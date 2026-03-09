package com.example.whynottoday;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private final ArrayList<LocalDate> days;
    public final View parentView;
    public final View roundedBox;
    public final TextView achieveCountTextView;
    public final TextView todoCountTextView;
    public final TextView dayOfMonth;
    public final TextView dayOfWeek;
    private final CalendarAdapter.OnItemListener onItemListener;

    //calendar_cell.xml의 레이아웃을 전달받아 하위 뷰 연결
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days)
    {
        super(itemView);
        parentView = itemView.findViewById(R.id.parentView);
        roundedBox = itemView.findViewById(R.id.dayStatusBox);

        //성취 및 할일 개수
        achieveCountTextView = itemView.findViewById(R.id.achieveCountTextView);
        todoCountTextView = itemView.findViewById(R.id.todoCountTextView);

        //날짜 및 요일
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        dayOfWeek = itemView.findViewById(R.id.dayWeekText);

        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
        this.days = days;
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
    }
}
