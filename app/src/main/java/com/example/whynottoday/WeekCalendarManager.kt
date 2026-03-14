package com.example.whynottoday

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whynottoday.CalendarUtils.daysInWeekArray
import com.example.whynottoday.CalendarUtils.monthYearFromDate
import java.time.LocalDate


//날짜 변경 시 호출할 함수가 정의된 인터페이스
interface OnDateChangeListener{
    fun onDateChange()
}

class WeekCalendarManager(
    private val context: Context,
    private val view: View,
    private val theme: String,
    private val listener : OnDateChangeListener
) : CalendarAdapter.OnItemListener { //상속

    init {
        Companion.themes = theme
    }
    // static 영역
    companion object {
        @JvmStatic
        private var themes: String = "blue" // 기본값

        @JvmStatic
        fun getColor(): String {
            return themes
        }
    }
    private lateinit var monthYearTextView: TextView
    private lateinit var calendarRecyclerView: RecyclerView

    public fun initCalendar(){
        //위젯 초기화
        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)

        //캘린더 세팅
        setWeekView()
        //이전, 다음 주 이동 버튼 세팅
        setPrevNextButtonAction()
    }
    private fun setWeekView() {

        //년, 월, 주차
        monthYearTextView.setText(monthYearFromDate(CalendarUtils.selectedDate))
//        weekTextView.setText(weekFromDate(CalendarUtils.selectedDate))

        //일주일
        val days: ArrayList<LocalDate?>? = daysInWeekArray(CalendarUtils.selectedDate)
        val calendarAdapter = CalendarAdapter(days, this)
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(context, 7)
        calendarRecyclerView.setLayoutManager(layoutManager)
        calendarRecyclerView.setAdapter(calendarAdapter)
    }

    public override fun onItemClick(position: Int, date: LocalDate?) {
        CalendarUtils.selectedDate = date
        setWeekView()
        listener.onDateChange() //프래그먼트에 날짜 변경 전달
    }

    public fun setPrevNextButtonAction(){
        //이전, 다음 주 버튼 제어
        view.findViewById<ImageButton>(R.id.previousWeekAction).setOnClickListener {
            previousWeekAction(it)
        }
        view.findViewById<ImageButton>(R.id.nextWeekAction).setOnClickListener {
            nextWeekAction(it)
        }
    }

    public fun previousWeekAction(view: View?) {
        val current = CalendarUtils.selectedDate

        if (current.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            //일요일 선택되어 있을 시 하루 전(토요일)로 이동
            CalendarUtils.selectedDate = current.with(java.time.DayOfWeek.SATURDAY)
        } else {
            //그 외 요일 선택되어 있을 시 일주일 전 토요일로 이동
            CalendarUtils.selectedDate = current.minusWeeks(1).with(java.time.DayOfWeek.SATURDAY)
        }
        initCalendar()
    }

    fun nextWeekAction(view: View?) {
        val current = CalendarUtils.selectedDate

        if (current.dayOfWeek == java.time.DayOfWeek.SATURDAY) {
            //툐요일 선택되어 있을 시 하루 뒤(일요일)로 이동
            CalendarUtils.selectedDate = current.with(java.time.DayOfWeek.SUNDAY)
        } else {
            // 그 외 요일 선택되어 있을 시 무조건 다음 주 일요일로 이동
            CalendarUtils.selectedDate = current.plusWeeks(1).with(java.time.DayOfWeek.SUNDAY)
        }
        initCalendar()
    }



}