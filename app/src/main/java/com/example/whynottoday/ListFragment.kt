package com.example.whynottoday

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.time.format.DateTimeFormatter
import java.util.Locale

class ListFragment : Fragment(), OnDateChangeListener {

    //사용 변수 - 캘린더
    private lateinit var manager: WeekCalendarManager
//    private lateinit var monthYearTextView: TextView
////    private lateinit var weekTextView : TextView
//    private lateinit var calendarRecyclerView: RecyclerView

    private lateinit var achieveCountTextView : TextView
    private lateinit var todoCountTextView : TextView
    //사용 변수 - 오늘 날짜 정보
    private lateinit var selectedDateTextView : TextView
    //private lateinit var achieveDensityBox : View

    //사용 변수 - 오늘 날짜 리스트
    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb : SQLiteDatabase
    private lateinit var excuseLayout : LinearLayout

    //사용 변수 - 스크롤 제어
    private lateinit var listScrollView : ScrollView
    private lateinit var navigationBar : ConstraintLayout
    var scroll : Int = 0
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //DB 세팅
        dbManager = DBManager(requireContext(), "WhyNotTodayDB.db", null, 1)
        sqlitedb = dbManager.readableDatabase

        //공통 UI 초기화
        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(requireContext() as AppCompatActivity)

        //위젯 초기화
        //calendarRecyclerView = view.findViewById<RecyclerView>(R.id.calendarRecyclerView)
        //monthYearTextView = view.findViewById<TextView>(R.id.monthYearTextView)
//        weekTextView = view.findViewById<TextView>(R.id.weekTextView)
        selectedDateTextView = view.findViewById<TextView>(R.id.selectedDateTextView)
        achieveCountTextView = view.findViewById<TextView>(R.id.achieveCountTextView)
        todoCountTextView = view.findViewById<TextView>(R.id.todoCountTextView)
        //achieveDensityBox = view.findViewById<View>(R.id.achieveDensityBox)
        excuseLayout = view.findViewById(R.id.excuseLayout)

        //달력 및 리스트 세팅
        manager = WeekCalendarManager(requireContext(), view, "red", this)
        manager.initCalendar()

        onDateChange() //달력 세팅 시 오늘 날짜 선택되도록 함

        //스크롤 방향 감지하여 하단 UI 제어
        listScrollView = view.findViewById<ScrollView>(R.id.listScrollView)
        navigationBar = view.findViewById<ConstraintLayout>(R.id.navigationBar)

        listScrollView.getViewTreeObserver().addOnScrollChangedListener {
            val scrollY = listScrollView.scrollY
            //최소 10픽셀 이상 스크롤
            if (scrollY > scroll && scrollY > 10) {
                // 스크롤 다운 시 UI 숨기기
                hideNavigation(navigationBar)
            } else if (scrollY < scroll) {
                // 스크롤 업 시 UI 보이기
                showNavigation(navigationBar)
            }
            scroll = scrollY
        }
    }

    private fun hideNavigation(view: View) {
        view.animate()
            .translationY(view.height.toFloat())
            .setDuration(300)
            .start()
    }

    private fun showNavigation(view: View) {
        view.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }


    //추가, 수정, 삭제 후 다시 그리기
    override fun onResume() {
        super.onResume()
        onDateChange() //프래그먼트 이동해도 날짜 바뀌지 않도록
    }

    override fun onDateChange() {
        setSelectedDateAdapter() //선택한 날짜, 성취 비율 세팅
        setExcuseLayoutAdapter() //핑계 리스트 세팅
    }

    //선택된 날의 날짜, 핑계 비율 출력
    private fun setSelectedDateAdapter(){
        //날짜
        val selectedDate = CalendarUtils.selectedDate
        val format = DateTimeFormatter.ofPattern("MM. dd (E)", Locale.KOREAN)
        selectedDateTextView.text = selectedDate.format(format)

        //할일 대비 성취 개수 및 비율(achieve/todo)
        //var cursor : Cursor
        //var query = "SELECT count(*) FROM todoTBL " +
        //        "WHERE date_time LIKE '$selectedDate%' "
        //cursor = sqlitedb.rawQuery(query, null)
        //val todoTaskCount = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        //cursor.close()
        //
        //var query2 = "SELECT count(*) FROM todoTBL " +
        //        "WHERE date_time LIKE '$selectedDate%' " +
        //        "AND is_done = 1"
        //cursor = sqlitedb.rawQuery(query2, null)
        //val achieveTaskCount = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        //cursor.close()

        //val excuseRatio = if(todoTaskCount==0) 0 else {
        //    ((achieveTaskCount.toFloat()/todoTaskCount.toFloat()) * 100).toInt()
        //}
        //achieveCountTextView.text = "${minOf(excuseRatio, 100)}%"
        //CalendarUtils.updateBoxColor(achieveDensityBox, excuseRatio)
    }

    //선택된 날의 핑계 리스트 출력
    private fun setExcuseLayoutAdapter() {

        excuseLayout.removeAllViews() //기존 뷰 제거

        val selectedDate = CalendarUtils.selectedDate
        var cursor : Cursor
        var query = "SELECT * FROM todoTBL " +
                "INNER JOIN excuseTBL ON todoTBL.todo_id = excuseTBL.todo_id " +
                "WHERE todoTBL.date_time LIKE '$selectedDate%' AND todoTBL.is_done == 0"
        cursor = sqlitedb.rawQuery(query, null)
        var num : Int = 0

        if (cursor.count == 0) {
            val noneExcuseTextView = TextView(requireContext())
            noneExcuseTextView.text = "오늘은 핑계 없이 갓생을 사셨군요! ✨"
            noneExcuseTextView.setTextColor(Color.GRAY)
            noneExcuseTextView.gravity = Gravity.CENTER
            noneExcuseTextView.setPadding(0, 60, 0, 0)
            excuseLayout.addView(noneExcuseTextView)
        } else {
            while(cursor.moveToNext()){
//                Log.d("DB_DEBUG", num.toString())
                var todoId = cursor.getInt(cursor.getColumnIndexOrThrow("todo_id"))
                var str_excuse = cursor.getString(cursor.getColumnIndexOrThrow("excuse_reason")).toString()
                var str_todo = cursor.getString(cursor.getColumnIndexOrThrow("todo_name")).toString()
                var isImportant = cursor.getInt(cursor.getColumnIndexOrThrow("is_important"))
                var str_time = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).toString()

                var str_timeFormatted = try{
                    val time = str_time.trim().split(" ")[1]
                    val splitedTime = time.split(":")
                    val hour = splitedTime[0].toInt()
                    var minute = splitedTime[1].toInt()

                    //meridiem = am/pm 통칭
                    val meridiem = if(hour < 12) "오전" else "오후"
                    val hour2 = if(hour>12) hour - 12 else hour

                    "$meridiem $hour2:${String.format("%02d", minute)}"
                }catch (e : Exception){
                    str_time
                }

                //핑계 아이템 생성
                var excuseItem : LinearLayout = LinearLayout(requireContext())
                excuseItem.orientation = LinearLayout.VERTICAL

                excuseItem.id = num
                excuseItem.setTag(str_excuse)
                excuseItem.setBackgroundResource(R.drawable.excuse_item_box)
                excuseItem.elevation=3F
                val param3 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param3.setMargins(0,0,0,30)
                excuseItem.layoutParams = param3

                excuseItem.setOnClickListener {
                    val intent = Intent(requireContext(), AddExcuseActivity::class.java)
                    intent.putExtra("TODO_ID", todoId) // ID 전달
                    startActivity(intent)
                }

                var innerLayout : LinearLayout = LinearLayout(requireContext())
                innerLayout.orientation= LinearLayout.VERTICAL
                innerLayout.setPadding(50,50,50,50)

                var todoLayout : LinearLayout = LinearLayout(requireContext())
                todoLayout.orientation= LinearLayout.HORIZONTAL
                todoLayout.gravity = Gravity.CENTER_VERTICAL
                val param1 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                param1.setMargins(0,0,0,30)
                todoLayout.layoutParams = param1

                var todoLeftLayout : LinearLayout = LinearLayout(requireContext())
                todoLeftLayout.orientation= LinearLayout.VERTICAL
                todoLayout.addView(todoLeftLayout)

                //중요 표시 생성
                var isImportantTextView : ImageView = ImageView(requireContext())
                isImportantTextView.setImageResource(R.drawable.star_image)
                isImportantTextView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_100))
                isImportantTextView.visibility = if (isImportant == 1) View.VISIBLE else View.INVISIBLE
                isImportantTextView.setPadding(0,0,10,0)
                todoLayout.addView(isImportantTextView)

                val leftParam = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                todoLeftLayout.layoutParams = leftParam

                val rightParam = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isImportantTextView.layoutParams = rightParam

                //시간 텍스트뷰 생성
                var timeTextView : TextView = TextView(requireContext())
                timeTextView.text = str_timeFormatted
                timeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgray))
                timeTextView.textSize=10F
                todoLeftLayout.addView(timeTextView)

                //할일 텍스트뷰 생성
                var todoTextView : TextView = TextView(requireContext())
                todoTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                todoTextView.text = str_todo
                todoTextView.textSize = 15F
                val textColor = if (isImportant == 1) R.color.blue_100 else R.color.gray
                todoTextView.setTextColor(ContextCompat.getColor(requireContext(), textColor))
                todoTextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.paperlogy_semibold)
                todoLeftLayout.addView(todoTextView)

                innerLayout.addView(todoLayout)

                //구분선 생성
                val horizontalLineView = View(requireContext())
                val param2 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                )
                param2.setMargins(0, 10, 0, 10)
                horizontalLineView.layoutParams = param2
                horizontalLineView.setBackgroundColor(Color.LTGRAY)
                innerLayout.addView(horizontalLineView)

                //핑계 텍스트뷰 생성
                var excuseTextView : TextView = TextView(requireContext())
                excuseTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                excuseTextView.text = str_excuse
                excuseTextView.textSize = 13F
                excuseTextView.setPadding(0,10,0,0)
                innerLayout.addView(excuseTextView)

                excuseItem.addView((innerLayout))
                excuseLayout.addView(excuseItem)
                num++
            }
        }
        cursor.close()
    }
}